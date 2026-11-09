import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { UsuarioService } from '../../../core/services/usuario.service';
import { PerfilUsuario } from '../../../core/models/auth.models';
import {
  UsuarioRequest,
  UsuarioResponse,
  UsuarioUpdateRequest,
} from '../../../core/models/usuario.models';
import { ApiError } from '../../../core/models/common.models';
import { ROTULO_PERFIL } from '../../../core/utils/rotulos';

export interface UsuarioFormDialogData {
  usuario?: UsuarioResponse;
}

@Component({
  selector: 'app-usuario-form-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
  ],
  templateUrl: './usuario-form-dialog.component.html',
  styleUrl: './usuario-form-dialog.component.scss',
})
export class UsuarioFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly usuarioService = inject(UsuarioService);
  private readonly dialogRef = inject(
    MatDialogRef<UsuarioFormDialogComponent, boolean>,
  );
  readonly dados = inject<UsuarioFormDialogData>(MAT_DIALOG_DATA);

  readonly perfis: PerfilUsuario[] = [
    'ALUNO',
    'PROFESSOR',
    'SECRETARIA',
    'COORDENADOR',
    'ADMIN',
  ];
  readonly rotuloPerfil = ROTULO_PERFIL;
  readonly ehEdicao = !!this.dados.usuario;
  readonly salvando = signal(false);
  readonly erroGeral = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    nome: [this.dados.usuario?.nome ?? '', [Validators.required, Validators.maxLength(120)]],
    documento: [this.dados.usuario?.documento ?? '', [Validators.required, Validators.maxLength(30)]],
    email: [this.dados.usuario?.email ?? '', [Validators.required, Validators.email]],
    senha: ['', this.ehEdicao ? [Validators.minLength(6)] : [Validators.required, Validators.minLength(6)]],
    perfil: [this.dados.usuario?.perfil ?? ('ALUNO' as PerfilUsuario), [Validators.required]],
    numeroMatricula: [this.dados.usuario?.numeroMatricula ?? ''],
    registroFuncional: [this.dados.usuario?.registroFuncional ?? ''],
    ativo: [this.dados.usuario?.ativo ?? true],
  });

  constructor() {
    if (this.ehEdicao) {
      this.form.controls.perfil.disable();
    }
    this.atualizarValidadores();
    this.form.controls.perfil.valueChanges.subscribe(() =>
      this.atualizarValidadores(),
    );
  }

  salvar(): void {
    this.erroGeral.set(null);
    this.form.markAllAsTouched();
    if (this.form.invalid || this.salvando()) {
      return;
    }

    const valores = this.form.getRawValue();
    this.salvando.set(true);

    if (this.ehEdicao && this.dados.usuario) {
      const request: UsuarioUpdateRequest = {
        nome: valores.nome,
        documento: valores.documento,
        email: valores.email,
        senha: valores.senha || null,
        ativo: valores.ativo,
        numeroMatricula: valores.perfil === 'ALUNO' ? valores.numeroMatricula : null,
        registroFuncional:
          valores.perfil === 'PROFESSOR' ? valores.registroFuncional : null,
      };
      this.usuarioService.atualizar(this.dados.usuario.id, request).subscribe({
        next: () => this.dialogRef.close(true),
        error: (erro: unknown) => this.tratarErro(erro),
      });
      return;
    }

    const request: UsuarioRequest = {
      nome: valores.nome,
      documento: valores.documento,
      email: valores.email,
      senha: valores.senha,
      perfil: valores.perfil,
      numeroMatricula: valores.perfil === 'ALUNO' ? valores.numeroMatricula : null,
      registroFuncional:
        valores.perfil === 'PROFESSOR' ? valores.registroFuncional : null,
      ativo: valores.ativo,
    };
    this.usuarioService.criar(request).subscribe({
      next: () => this.dialogRef.close(true),
      error: (erro: unknown) => this.tratarErro(erro),
    });
  }

  erroServidor(campo: string): string | null {
    const controle = this.form.get(campo);
    const erro = controle?.errors?.['server'];
    return typeof erro === 'string' ? erro : null;
  }

  fechar(): void {
    this.dialogRef.close(false);
  }

  private atualizarValidadores(): void {
    const perfil = this.form.controls.perfil.value;
    const matricula = this.form.controls.numeroMatricula;
    const registro = this.form.controls.registroFuncional;

    if (perfil === 'ALUNO') {
      matricula.setValidators([Validators.required]);
      registro.clearValidators();
    } else if (perfil === 'PROFESSOR') {
      registro.setValidators([Validators.required]);
      matricula.clearValidators();
    } else {
      matricula.clearValidators();
      registro.clearValidators();
    }
    matricula.updateValueAndValidity();
    registro.updateValueAndValidity();
  }

  private tratarErro(erro: unknown): void {
    this.salvando.set(false);
    if (!(erro instanceof HttpErrorResponse)) {
      this.erroGeral.set('Não foi possível salvar o usuário.');
      return;
    }

    const corpo = erro.error as ApiError | null;
    if (erro.status === 400 && corpo?.campos) {
      for (const [campo, mensagem] of Object.entries(corpo.campos)) {
        const controle = this.form.get(campo);
        if (controle) {
          controle.setErrors({ server: mensagem });
          controle.markAsTouched();
        }
      }
      this.erroGeral.set(corpo.mensagem || 'Dados inválidos.');
      return;
    }

    if (erro.status === 409) {
      const mensagem = corpo?.mensagem ?? 'Dados já cadastrados.';
      this.erroGeral.set(mensagem);
      if (mensagem.toLowerCase().includes('e-mail')) {
        this.form.controls.email.setErrors({ server: mensagem });
      } else if (mensagem.toLowerCase().includes('documento')) {
        this.form.controls.documento.setErrors({ server: mensagem });
      } else if (mensagem.toLowerCase().includes('matricula')) {
        this.form.controls.numeroMatricula.setErrors({ server: mensagem });
      } else if (mensagem.toLowerCase().includes('registro')) {
        this.form.controls.registroFuncional.setErrors({ server: mensagem });
      }
      return;
    }

    this.erroGeral.set(
      corpo?.mensagem ?? 'Não foi possível salvar o usuário.',
    );
  }
}
