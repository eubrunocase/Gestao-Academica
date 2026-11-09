import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { forkJoin } from 'rxjs';
import { AcademicoService } from '../../../core/services/academico.service';
import { Curso, Disciplina } from '../../../core/models/academico.models';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state.component';
import { PageHeaderComponent } from '../../../shared/components/page-header.component';
import {
  DisciplinaFormDialogComponent,
  DisciplinaFormDialogData,
} from './disciplina-form-dialog.component';

@Component({
  selector: 'app-disciplinas',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatTableModule,
    PageHeaderComponent,
    EmptyStateComponent,
  ],
  templateUrl: './disciplinas.component.html',
  styleUrl: './disciplinas.component.scss',
})
export class DisciplinasComponent implements OnInit {
  private readonly academicoService = inject(AcademicoService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly busca = new FormControl('', { nonNullable: true });
  readonly buscaTermo = toSignal(this.busca.valueChanges, {
    initialValue: this.busca.value,
  });
  readonly cursos = signal<Curso[]>([]);
  readonly disciplinas = signal<Disciplina[]>([]);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly pagina = signal(0);
  readonly tamanho = signal(10);
  readonly colunas = ['nome', 'curso', 'carga', 'prerequisitos', 'acoes'];

  readonly filtradas = computed(() => {
    const termo = this.buscaTermo().trim().toLowerCase();
    return termo
      ? this.disciplinas().filter(
          (disciplina) =>
            disciplina.nome.toLowerCase().includes(termo) ||
            disciplina.curso.nome.toLowerCase().includes(termo),
        )
      : this.disciplinas();
  });
  readonly paginadas = computed(() =>
    this.filtradas().slice(
      this.pagina() * this.tamanho(),
      (this.pagina() + 1) * this.tamanho(),
    ),
  );

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    forkJoin({
      cursos: this.academicoService.listarCursos(),
      disciplinas: this.academicoService.listarDisciplinas(),
    }).subscribe({
      next: ({ cursos, disciplinas }) => {
        this.cursos.set(cursos);
        this.disciplinas.set(disciplinas);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar as disciplinas.');
      },
    });
  }

  buscar(): void {
    this.pagina.set(0);
  }

  aoPaginar(evento: PageEvent): void {
    this.pagina.set(evento.pageIndex);
    this.tamanho.set(evento.pageSize);
  }

  novo(): void {
    if (this.cursos().length === 0) {
      this.snackBar.open(
        'Cadastre um curso antes de criar disciplinas.',
        'Fechar',
        { duration: 4000 },
      );
      return;
    }
    this.abrirDialogo({ cursos: this.cursos(), disciplinas: this.disciplinas() });
  }

  editar(disciplina: Disciplina): void {
    this.abrirDialogo({
      disciplina,
      cursos: this.cursos(),
      disciplinas: this.disciplinas(),
    });
  }

  excluir(disciplina: Disciplina): void {
    const referencia = this.dialog.open<
      ConfirmDialogComponent,
      unknown,
      boolean
    >(ConfirmDialogComponent, {
      data: {
        titulo: 'Excluir disciplina',
        mensagem: `Excluir "${disciplina.nome}"? Disciplinas com matrículas ou turmas vinculadas não podem ser excluídas.`,
        confirmar: 'Excluir',
        destrutivo: true,
      },
      width: '460px',
    });
    referencia.afterClosed().subscribe((confirmado) => {
      if (!confirmado) {
        return;
      }
      this.academicoService.excluirDisciplina(disciplina.id).subscribe({
        next: () => {
          this.snackBar.open('Disciplina excluída.', 'Fechar', {
            duration: 3000,
          });
          this.carregar();
        },
      });
    });
  }

  private abrirDialogo(dados: DisciplinaFormDialogData): void {
    const referencia = this.dialog.open<
      DisciplinaFormDialogComponent,
      DisciplinaFormDialogData,
      boolean
    >(DisciplinaFormDialogComponent, {
      width: '640px',
      maxWidth: '96vw',
      data: dados,
    });
    referencia.afterClosed().subscribe((salvou) => {
      if (salvou) {
        this.snackBar.open('Disciplina salva.', 'Fechar', { duration: 3000 });
        this.carregar();
      }
    });
  }
}
