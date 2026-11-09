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
  Curso,
  Disciplina,
  DisciplinaRequest,
} from '../../../core/models/academico.models';
import { ApiError } from '../../../core/models/common.models';

export interface DisciplinaFormDialogData {
  disciplina?: Disciplina;
  cursos: Curso[];
  disciplinas: Disciplina[];
}

@Component({
  selector: 'app-disciplina-form-dialog',
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
  templateUrl: './disciplina-form-dialog.component.html',
  styleUrl: './disciplina-form-dialog.component.scss',
})
export class DisciplinaFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly academicoService = inject(AcademicoService);
  private readonly dialogRef = inject(
    MatDialogRef<DisciplinaFormDialogComponent, boolean>,
  );
  readonly dados = inject<DisciplinaFormDialogData>(MAT_DIALOG_DATA);
  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    nome: [this.dados.disciplina?.nome ?? '', [Validators.required, Validators.maxLength(120)]],
    cargaHoraria: [this.dados.disciplina?.cargaHoraria ?? 60, [Validators.required, Validators.min(1)]],
    cursoId: [this.dados.disciplina?.curso.id ?? this.dados.cursos[0]?.id ?? 0, [Validators.required, Validators.min(1)]],
    prerequisitoIds: [
      this.dados.disciplina?.prerequisitos.map((item) => item.id) ?? [],
    ],
  });

  opcoesPrerequisito(): Disciplina[] {
    return this.dados.disciplinas.filter(
      (disciplina) => disciplina.id !== this.dados.disciplina?.id,
    );
  }

  salvar(): void {
    this.erro.set(null);
    this.form.markAllAsTouched();
    if (this.form.invalid || this.salvando()) {
      return;
    }
    this.salvando.set(true);
    const valores = this.form.getRawValue();
    const request: DisciplinaRequest = {
      nome: valores.nome,
      cargaHoraria: Number(valores.cargaHoraria),
      cursoId: Number(valores.cursoId),
      prerequisitoIds: valores.prerequisitoIds,
    };
    const operacao = this.dados.disciplina
      ? this.academicoService.atualizarDisciplina(this.dados.disciplina.id, request)
      : this.academicoService.criarDisciplina(request);
    operacao.subscribe({
      next: () => this.dialogRef.close(true),
      error: (erro: unknown) => {
        this.salvando.set(false);
        const corpo =
          erro instanceof HttpErrorResponse ? (erro.error as ApiError | null) : null;
        this.erro.set(corpo?.mensagem ?? 'Não foi possível salvar a disciplina.');
      },
    });
  }

  fechar(): void {
    this.dialogRef.close(false);
  }
}
