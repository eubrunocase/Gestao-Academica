import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ErroMatricula } from '../../../core/models/common.models';

export interface MatriculaErroDialogData {
  erro: ErroMatricula;
}

@Component({
  selector: 'app-matricula-erro-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <h2 mat-dialog-title>{{ titulo() }}</h2>
    <mat-dialog-content>
      <div class="alert" [class]="'alert ' + classeAlerta()" role="alert">
        <mat-icon aria-hidden="true">{{ icone() }}</mat-icon>
        <span>{{ dados.erro.mensagem }}</span>
      </div>

      @if (dados.erro.motivo === 'PREREQUISITO') {
        <p>Conclua as disciplinas abaixo antes de se matricular nesta turma:</p>
        <ul class="lista-pendentes">
          @for (disciplina of dados.erro.disciplinasPendentes; track disciplina.id) {
            <li>
              <mat-icon aria-hidden="true">menu_book</mat-icon>
              <span>{{ disciplina.nome }}</span>
            </li>
          }
        </ul>
      }

      @if (dados.erro.motivo === 'SEM_VAGA' && dados.erro.turmasAlternativas.length > 0) {
        <p>Esta turma está lotada. Há vagas nestas turmas alternativas:</p>
        <ul class="lista-alternativas">
          @for (alternativa of dados.erro.turmasAlternativas; track alternativa.id) {
            <li>
              <div>
                <strong>{{ alternativa.disciplina }}</strong>
                <span class="muted small">
                  {{ alternativa.professor }} · {{ alternativa.periodoLetivo }} ·
                  {{ alternativa.vagasDisponiveis }} vaga(s)
                </span>
              </div>
              <button mat-flat-button color="primary" type="button" (click)="escolher(alternativa.id)">
                Matricular
              </button>
            </li>
          }
        </ul>
      }

      @if (dados.erro.motivo === 'FORA_DO_PRAZO') {
        <p>O período de matrícula está encerrado. Procure a Secretaria Acadêmica para orientações.</p>
      }

      @if (dados.erro.motivo === 'TURMA_ENCERRADA') {
        <p>Esta turma foi encerrada e não aceita novas matrículas.</p>
      }

      @if (dados.erro.motivo === 'JA_MATRICULADO') {
        <p>Você já possui uma matrícula ativa nesta turma.</p>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button type="button" (click)="fechar()">Fechar</button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      .alert {
        margin-bottom: 16px;
      }

      ul {
        margin: 8px 0 0;
        padding: 0;
        list-style: none;
        display: grid;
        gap: 10px;
      }

      .lista-pendentes li,
      .lista-alternativas li {
        display: flex;
        align-items: center;
        gap: 10px;
        justify-content: space-between;
        border: 1px solid var(--ga-border);
        border-radius: 10px;
        padding: 10px 12px;
      }

      .lista-pendentes li {
        justify-content: flex-start;
      }

      .lista-alternativas li > div {
        display: flex;
        flex-direction: column;
      }

      .lista-pendentes mat-icon {
        color: var(--ga-warn);
      }

      p {
        margin: 8px 0;
      }
    `,
  ],
})
export class MatriculaErroDialogComponent {
  readonly dados = inject<MatriculaErroDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(
    MatDialogRef<MatriculaErroDialogComponent, number | null>,
  );

  titulo(): string {
    switch (this.dados.erro.motivo) {
      case 'PREREQUISITO':
        return 'Pré-requisitos pendentes';
      case 'SEM_VAGA':
        return 'Turma lotada';
      case 'FORA_DO_PRAZO':
        return 'Fora do prazo de matrícula';
      case 'TURMA_ENCERRADA':
        return 'Turma encerrada';
      case 'JA_MATRICULADO':
        return 'Matrícula já existente';
      default:
        return 'Não foi possível matricular';
    }
  }

  classeAlerta(): string {
    return this.dados.erro.motivo === 'SEM_VAGA' || this.dados.erro.motivo === 'PREREQUISITO'
      ? 'alert-warning'
      : 'alert-danger';
  }

  icone(): string {
    switch (this.dados.erro.motivo) {
      case 'SEM_VAGA':
        return 'groups';
      case 'PREREQUISITO':
        return 'menu_book';
      case 'FORA_DO_PRAZO':
        return 'event_busy';
      default:
        return 'block';
    }
  }

  escolher(turmaId: number): void {
    this.dialogRef.close(turmaId);
  }

  fechar(): void {
    this.dialogRef.close(null);
  }
}
