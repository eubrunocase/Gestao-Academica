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
import { AcademicoService } from '../../../core/services/academico.service';
import {
  Disciplina,
  Turma,
  TurmaRequest,
} from '../../../core/models/academico.models';
import { ApiError } from '../../../core/models/common.models';

export interface ProfessorOpcao {
  id: number;
  nome: string;
}

export interface TurmaFormDialogData {
  turma?: Turma;
  disciplinas: Disciplina[];
  professores: ProfessorOpcao[];
  periodos: string[];
}

@Component({
  selector: 'app-turma-form-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './turma-form-dialog.component.html',
  styleUrl: './turma-form-dialog.component.scss',
})
export class TurmaFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly academicoService = inject(AcademicoService);
  private readonly dialogRef = inject(
    MatDialogRef<TurmaFormDialogComponent, boolean>,
  );
  readonly dados = inject<TurmaFormDialogData>(MAT_DIALOG_DATA);
  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    disciplinaId: [
      this.dados.turma?.disciplina.id ?? this.dados.disciplinas[0]?.id ?? 0,
      [Validators.required, Validators.min(1)],
    ],
    professorId: [
      this.dados.turma?.professor.id ?? this.dados.professores[0]?.id ?? 0,
      [Validators.required, Validators.min(1)],
    ],
    periodoLetivo: [
      this.dados.turma?.periodoLetivo ?? this.dados.periodos[0] ?? '',
      [Validators.required],
    ],
    limiteVagas: [
      this.dados.turma?.limiteVagas ?? 40,
      [Validators.required, Validators.min(1)],
    ],
  });

  salvar(): void {
    this.erro.set(null);
    this.form.markAllAsTouched();
    if (this.form.invalid || this.salvando()) {
      return;
    }
    this.salvando.set(true);
    const valores = this.form.getRawValue();
    const request: TurmaRequest = {
      disciplinaId: Number(valores.disciplinaId),
      professorId: Number(valores.professorId),
      periodoLetivo: valores.periodoLetivo,
      limiteVagas: Number(valores.limiteVagas),
    };
    const operacao = this.dados.turma
      ? this.academicoService.atualizarTurma(this.dados.turma.id, request)
      : this.academicoService.criarTurma(request);
    operacao.subscribe({
      next: () => this.dialogRef.close(true),
      error: (erro: unknown) => {
        this.salvando.set(false);
        const corpo =
          erro instanceof HttpErrorResponse ? (erro.error as ApiError | null) : null;
        this.erro.set(corpo?.mensagem ?? 'Não foi possível salvar a turma.');
      },
    });
  }

  fechar(): void {
    this.dialogRef.close(false);
  }
}
