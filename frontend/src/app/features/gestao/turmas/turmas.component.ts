import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { AcademicoService } from '../../../core/services/academico.service';
import { UsuarioService } from '../../../core/services/usuario.service';
import {
  CalendarioAcademico,
  Disciplina,
  Turma,
} from '../../../core/models/academico.models';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state.component';
import { PageHeaderComponent } from '../../../shared/components/page-header.component';
import {
  ProfessorOpcao,
  TurmaFormDialogComponent,
  TurmaFormDialogData,
} from './turma-form-dialog.component';

@Component({
  selector: 'app-turmas',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
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
  templateUrl: './turmas.component.html',
  styleUrl: './turmas.component.scss',
})
export class TurmasComponent implements OnInit {
  private readonly academicoService = inject(AcademicoService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly auth = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly busca = new FormControl('', { nonNullable: true });
  readonly buscaTermo = toSignal(this.busca.valueChanges, {
    initialValue: this.busca.value,
  });
  readonly turmas = signal<Turma[]>([]);
  readonly disciplinas = signal<Disciplina[]>([]);
  readonly calendarios = signal<CalendarioAcademico[]>([]);
  readonly professores = signal<ProfessorOpcao[]>([]);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly pagina = signal(0);
  readonly tamanho = signal(10);
  readonly colunas = ['disciplina', 'professor', 'periodo', 'vagas', 'status', 'acoes'];

  readonly periodos = computed(() => {
    const doCalendario = this.calendarios().map((item) => item.periodoLetivo);
    const dasTurmas = this.turmas().map((item) => item.periodoLetivo);
    return Array.from(new Set([...doCalendario, ...dasTurmas])).sort().reverse();
  });

  readonly filtradas = computed(() => {
    const termo = this.buscaTermo().trim().toLowerCase();
    if (!termo) {
      return this.turmas();
    }
    return this.turmas().filter(
      (turma) =>
        turma.disciplina.nome.toLowerCase().includes(termo) ||
        turma.professor.nome.toLowerCase().includes(termo) ||
        turma.periodoLetivo.toLowerCase().includes(termo),
    );
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
      turmas: this.academicoService.listarTurmas(),
      disciplinas: this.academicoService.listarDisciplinas(),
      calendarios: this.academicoService.listarCalendarios(),
    }).subscribe({
      next: ({ turmas, disciplinas, calendarios }) => {
        this.turmas.set(turmas);
        this.disciplinas.set(disciplinas);
        this.calendarios.set(calendarios);
        this.carregarProfessores(turmas);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar as turmas.');
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
    if (this.disciplinas().length === 0 || this.periodos().length === 0) {
      this.snackBar.open(
        'É necessário ter disciplinas e períodos letivos cadastrados.',
        'Fechar',
        { duration: 4500 },
      );
      return;
    }
    this.abrirDialogo({
      disciplinas: this.disciplinas(),
      professores: this.professores(),
      periodos: this.periodos(),
    });
  }

  editar(turma: Turma): void {
    this.abrirDialogo({
      turma,
      disciplinas: this.disciplinas(),
      professores: this.professores(),
      periodos: this.periodos(),
    });
  }

  encerrar(turma: Turma): void {
    const referencia = this.dialog.open<
      ConfirmDialogComponent,
      unknown,
      boolean
    >(ConfirmDialogComponent, {
      data: {
        titulo: 'Encerrar turma',
        mensagem: `Encerrar ${turma.disciplina.nome} (${turma.periodoLetivo})? Novas matrículas e alterações de nota ficarão bloqueadas.`,
        confirmar: 'Encerrar turma',
        destrutivo: true,
      },
      width: '460px',
    });
    referencia.afterClosed().subscribe((confirmado) => {
      if (!confirmado) {
        return;
      }
      this.academicoService.encerrarTurma(turma.id).subscribe({
        next: () => {
          this.snackBar.open('Turma encerrada.', 'Fechar', {
            duration: 3500,
          });
          this.carregar();
        },
      });
    });
  }

  excluir(turma: Turma): void {
    const referencia = this.dialog.open<
      ConfirmDialogComponent,
      unknown,
      boolean
    >(ConfirmDialogComponent, {
      data: {
        titulo: 'Excluir turma',
        mensagem: `Excluir a turma de ${turma.disciplina.nome} (${turma.periodoLetivo})? Turmas com matrículas não podem ser excluídas.`,
        confirmar: 'Excluir',
        destrutivo: true,
      },
      width: '460px',
    });
    referencia.afterClosed().subscribe((confirmado) => {
      if (!confirmado) {
        return;
      }
      this.academicoService.excluirTurma(turma.id).subscribe({
        next: () => {
          this.snackBar.open('Turma excluída.', 'Fechar', { duration: 3000 });
          this.carregar();
        },
      });
    });
  }

  private carregarProfessores(turmas: Turma[]): void {
    const perfil = this.auth.perfil();
    if (perfil === 'ADMIN' || perfil === 'SECRETARIA') {
      this.usuarioService.listar({ perfil: 'PROFESSOR', size: 100 }).subscribe({
        next: (resposta) => {
          this.professores.set(
            resposta.content.map((usuario) => ({
              id: usuario.id,
              nome: usuario.nome,
            })),
          );
        },
        error: () => this.derivarProfessores(turmas),
      });
      return;
    }
    this.derivarProfessores(turmas);
  }

  private derivarProfessores(turmas: Turma[]): void {
    const mapa = new Map<number, string>();
    for (const turma of turmas) {
      mapa.set(turma.professor.id, turma.professor.nome);
    }
    this.professores.set(
      Array.from(mapa.entries()).map(([id, nome]) => ({ id, nome })),
    );
  }

  private abrirDialogo(dados: TurmaFormDialogData): void {
    const referencia = this.dialog.open<
      TurmaFormDialogComponent,
      TurmaFormDialogData,
      boolean
    >(TurmaFormDialogComponent, {
      width: '640px',
      maxWidth: '96vw',
      data: dados,
    });
    referencia.afterClosed().subscribe((salvou) => {
      if (salvou) {
        this.snackBar.open('Turma salva.', 'Fechar', { duration: 3000 });
        this.carregar();
      }
    });
  }
}
