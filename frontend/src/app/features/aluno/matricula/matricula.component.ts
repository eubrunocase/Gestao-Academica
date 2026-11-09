import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { AlunoService } from '../../../core/services/aluno.service';
import {
  Matricula,
  StatusMatricula,
  TurmaDisponivel,
} from '../../../core/models/academico.models';
import {
  ErroMatricula,
} from '../../../core/models/common.models';
import {
  ROTULO_STATUS_MATRICULA,
  corStatusMatricula,
} from '../../../core/utils/rotulos';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state.component';
import { PageHeaderComponent } from '../../../shared/components/page-header.component';
import { DataBrPipe } from '../../../shared/pipes/data-br.pipe';
import {
  MatriculaErroDialogComponent,
  MatriculaErroDialogData,
} from './matricula-erro-dialog.component';

@Component({
  selector: 'app-matricula',
  standalone: true,
  imports: [
    MatButtonModule,
    MatCardModule,
    MatDialogModule,
    MatIconModule,
    MatProgressBarModule,
    MatTableModule,
    PageHeaderComponent,
    EmptyStateComponent,
    DataBrPipe,
  ],
  templateUrl: './matricula.component.html',
  styleUrl: './matricula.component.scss',
})
export class MatriculaComponent implements OnInit {
  private readonly alunoService = inject(AlunoService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly turmas = signal<TurmaDisponivel[]>([]);
  readonly matriculas = signal<Matricula[]>([]);
  readonly carregando = signal(true);
  readonly processando = signal<number | null>(null);
  readonly erro = signal<string | null>(null);

  readonly colunasTurmas = ['disciplina', 'professor', 'periodo', 'vagas', 'acao'];
  readonly colunasMatriculas = ['disciplina', 'periodo', 'data', 'status', 'acao'];

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    forkJoin({
      turmas: this.alunoService.turmasDisponiveis(),
      matriculas: this.alunoService.minhasMatriculas(),
    }).subscribe({
      next: ({ turmas, matriculas }) => {
        this.turmas.set(turmas);
        this.matriculas.set(matriculas);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar as turmas e matrículas.');
      },
    });
  }

  matricular(turmaId: number): void {
    if (this.processando() !== null) {
      return;
    }
    this.processando.set(turmaId);
    this.alunoService.matricular({ turmaId }).subscribe({
      next: (matricula) => {
        this.processando.set(null);
        this.snackBar.open(
          `Matrícula realizada em ${matricula.disciplina}.`,
          'Fechar',
          { duration: 4000 },
        );
        this.carregar();
      },
      error: (erro: unknown) => {
        this.processando.set(null);
        this.tratarErro(erro);
      },
    });
  }

  cancelar(matricula: Matricula): void {
    const referencia = this.dialog.open<ConfirmDialogComponent, unknown, boolean>(
      ConfirmDialogComponent,
      {
        data: {
          titulo: 'Cancelar matrícula',
          mensagem: `Deseja cancelar a matrícula em ${matricula.disciplina}? A vaga será liberada para outro estudante.`,
          confirmar: 'Cancelar matrícula',
          cancelar: 'Manter matrícula',
          destrutivo: true,
        },
        width: '440px',
      },
    );

    referencia.afterClosed().subscribe((confirmado) => {
      if (!confirmado) {
        return;
      }
      this.alunoService.cancelarMatricula(matricula.id).subscribe({
        next: () => {
          this.snackBar.open('Matrícula cancelada.', 'Fechar', {
            duration: 3500,
          });
          this.carregar();
        },
      });
    });
  }

  rotuloStatus(status: StatusMatricula): string {
    return ROTULO_STATUS_MATRICULA[status];
  }

  classeStatus(status: StatusMatricula): string {
    return corStatusMatricula(status);
  }

  private tratarErro(erro: unknown): void {
    if (erro instanceof HttpErrorResponse && erro.status === 422) {
      const corpo = erro.error as ErroMatricula | null;
      if (corpo?.motivo) {
        const referencia = this.dialog.open<
          MatriculaErroDialogComponent,
          MatriculaErroDialogData,
          number | null
        >(MatriculaErroDialogComponent, {
          data: { erro: corpo },
          width: '520px',
        });
        referencia.afterClosed().subscribe((alternativaId) => {
          if (alternativaId) {
            this.matricular(alternativaId);
          }
        });
        return;
      }
    }

    const mensagem =
      erro instanceof HttpErrorResponse &&
      erro.error &&
      typeof erro.error === 'object' &&
      'mensagem' in erro.error
        ? String((erro.error as { mensagem: unknown }).mensagem)
        : 'Não foi possível concluir a matrícula.';
    this.snackBar.open(mensagem, 'Fechar', { duration: 5000 });
  }
}
