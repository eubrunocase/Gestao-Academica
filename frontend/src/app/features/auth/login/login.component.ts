import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { ApiError } from '../../../core/models/common.models';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatDividerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required]],
  });

  readonly carregando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly bloqueada = signal(false);
  readonly ocultarSenha = signal(true);

  constructor() {
    if (this.auth.estaLogado()) {
      void this.router.navigateByUrl(this.auth.home(), { replaceUrl: true });
    }
  }

  entrar(): void {
    this.erro.set(null);
    this.bloqueada.set(false);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.carregando.set(true);
    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.carregando.set(false);
        const redirect = this.route.snapshot.queryParamMap.get('redirect');
        void this.router.navigateByUrl(redirect || this.auth.home(), {
          replaceUrl: true,
        });
      },
      error: (erro: unknown) => {
        this.carregando.set(false);
        if (!(erro instanceof HttpErrorResponse)) {
          this.erro.set('Não foi possível entrar. Tente novamente.');
          return;
        }
        if (erro.status === 423 || erro.status === 429) {
          this.bloqueada.set(true);
          this.erro.set(
            'Conta temporariamente bloqueada. Tente novamente mais tarde.',
          );
          return;
        }
        if (erro.status === 401) {
          this.erro.set('Credenciais inválidas.');
          return;
        }
        const corpo = erro.error as ApiError | null;
        this.erro.set(
          corpo?.mensagem ?? 'Não foi possível entrar. Tente novamente.',
        );
      },
    });
  }

  preencherDemo(email: string): void {
    this.form.setValue({ email, senha: 'Demo@123' });
  }
}
