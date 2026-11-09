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
import { AcademicoService } from '../../../core/services/academico.service';
import { Curso } from '../../../core/models/academico.models';
import { ApiError } from '../../../core/models/common.models';

export interface CursoFormDialogData {
  curso?: Curso;
}

@Component({
  selector: 'app-curso-form-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
  ],
  template: `
    <h2 mat-dialog-title>{{ dados.curso ? 'Editar curso' : 'Novo curso' }}</h2>
    <mat-dialog-content>
      @if (erro()) {
        <div class="alert alert-danger" role="alert">
          <mat-icon aria-hidden="true">error</mat-icon>
          <span>{{ erro() }}</span>
        </div>
      }
      <form [formGroup]="form" (ngSubmit)="salvar()">
        <mat-form-field appearance="outline">
          <mat-label>Nome do curso</mat-label>
          <input matInput formControlName="nome" maxlength="120" />
          @if (form.controls.nome.hasError('required')) {
            <mat-error>Informe o nome do curso.</mat-error>
          }
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button type="button" (click)="fechar()">Cancelar</button>
      <button mat-flat-button color="primary" type="button" [disabled]="salvando()" (click)="salvar()">
        {{ salvando() ? 'Salvando…' : 'Salvar' }}
      </button>
    </mat-dialog-actions>
  `,
})
export class CursoFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly academicoService = inject(AcademicoService);
  private readonly dialogRef = inject(
    MatDialogRef<CursoFormDialogComponent, boolean>,
  );
  readonly dados = inject<CursoFormDialogData>(MAT_DIALOG_DATA);
  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    nome: [this.dados.curso?.nome ?? '', [Validators.required, Validators.maxLength(120)]],
  });

  salvar(): void {
    this.erro.set(null);
    this.form.markAllAsTouched();
    if (this.form.invalid || this.salvando()) {
      return;
    }
    this.salvando.set(true);
    const request = this.form.getRawValue();
    const operacao = this.dados.curso
      ? this.academicoService.atualizarCurso(this.dados.curso.id, request)
      : this.academicoService.criarCurso(request);
    operacao.subscribe({
      next: () => this.dialogRef.close(true),
      error: (erro: unknown) => {
        this.salvando.set(false);
        const corpo =
          erro instanceof HttpErrorResponse ? (erro.error as ApiError | null) : null;
        this.erro.set(corpo?.mensagem ?? 'Não foi possível salvar o curso.');
      },
    });
  }

  fechar(): void {
    this.dialogRef.close(false);
  }
}
