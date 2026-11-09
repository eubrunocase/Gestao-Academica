import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { ActivatedRoute } from '@angular/router';
import { DenunciaService } from '../../../core/services/denuncia.service';
import { Denuncia } from '../../../core/models/denuncia.models';
import { ApiError } from '../../../core/models/common.models';
import {
  ROTULO_STATUS_DENUNCIA,
  corStatusDenuncia,
} from '../../../core/utils/rotulos';
import { EmptyStateComponent } from '../../../shared/components/empty-state.component';
import { PageHeaderComponent } from '../../../shared/components/page-header.component';
import { DataBrPipe } from '../../../shared/pipes/data-br.pipe';

@Component({
  selector: 'app-acompanhar',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    PageHeaderComponent,
    EmptyStateComponent,
    DataBrPipe,
  ],
  templateUrl: './acompanhar.component.html',
  styleUrl: './acompanhar.component.scss',
})
export class AcompanharComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly denunciaService = inject(DenunciaService);
  private readonly route = inject(ActivatedRoute);

  readonly form = this.fb.nonNullable.group({
    protocolo: ['', [Validators.required]],
  });
  readonly denuncia = signal<Denuncia | null>(null);
  readonly carregando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly rotuloStatus = ROTULO_STATUS_DENUNCIA;

  ngOnInit(): void {
    const protocolo = this.route.snapshot.queryParamMap.get('protocolo');
    if (protocolo) {
      this.form.setValue({ protocolo });
      this.consultar();
    }
  }

  consultar(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.carregando()) {
      return;
    }
    this.erro.set(null);
    this.denuncia.set(null);
    this.carregando.set(true);
    this.denunciaService
      .consultarPorProtocolo(this.form.controls.protocolo.value.trim())
      .subscribe({
        next: (denuncia) => {
          this.denuncia.set(denuncia);
          this.carregando.set(false);
        },
        error: (erro: unknown) => {
          this.carregando.set(false);
          const corpo =
            erro instanceof HttpErrorResponse
              ? (erro.error as ApiError | null)
              : null;
          this.erro.set(
            corpo?.mensagem ??
              'Protocolo não encontrado. Verifique o código e tente novamente.',
          );
        },
      });
  }

  classeStatus(denuncia: Denuncia): string {
    return corStatusDenuncia(denuncia.status);
  }
}
