import { DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  Component,
  OnInit,
  computed,
  inject,
  input,
  signal,
} from '@angular/core';
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
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { RouterLink } from '@angular/router';
import { AcademicoService } from '../../../core/services/academico.service';
import { AvisoService } from '../../../core/services/aviso.service';
import { RelatorioService } from '../../../core/services/relatorio.service';
import {
  AlunoTurma,
  StatusMatricula,
  Turma,
} from '../../../core/models/academico.models';
import { Aviso } from '../../../core/models/comunicacao.models';
import { RelatorioTurma } from '../../../core/models/relatorio.models';
import {
  ROTULO_STATUS_MATRICULA,
  corStatusMatricula,
} from '../../../core/utils/rotulos';
import { EmptyStateComponent } from '../../../shared/components/empty-state.component';
import { PageHeaderComponent } from '../../../shared/components/page-header.component';
import { StatCardComponent } from '../../../shared/components/stat-card.component';
import { DataBrPipe } from '../../../shared/pipes/data-br.pipe';
import { PercentualPipe } from '../../../shared/pipes/percentual.pipe';
import { LancarNotasComponent } from './lancar-notas.component';
import { RegistrarFrequenciaComponent } from './registrar-frequencia.component';

@Component({
  selector: 'app-turma-detalhe',
  standalone: true,
  imports: [
    DecimalPipe,
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatTableModule,
    MatTabsModule,
    PageHeaderComponent,
    EmptyStateComponent,
    StatCardComponent,
    DataBrPipe,
    PercentualPipe,
    LancarNotasComponent,
    RegistrarFrequenciaComponent,
  ],
  templateUrl: './turma-detalhe.component.html',
  styleUrl: './turma-detalhe.component.scss',
})
export class TurmaDetalheComponent implements OnInit {
  private readonly academicoService = inject(AcademicoService);
  private readonly avisoService = inject(AvisoService);
  private readonly relatorioService = inject(RelatorioService);
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);

  readonly id = input.required<string>();
  readonly turmaId = computed(() => Number(this.id()));

  readonly turma = signal<Turma | null>(null);
  readonly alunos = signal<AlunoTurma[]>([]);
  readonly avisos = signal<Aviso[]>([]);
  readonly relatorio = signal<RelatorioTurma | null>(null);
  readonly aba = signal(0);
  readonly carregandoTurma = signal(true);
  readonly carregandoAlunos = signal(false);
  readonly carregandoAvisos = signal(false);
  readonly carregandoRelatorio = signal(false);
  readonly salvandoAviso = signal(false);
  readonly exportando = signal(false);
  readonly erro = signal<string | null>(null);

  readonly colunasAlunos = ['nome', 'matricula', 'email', 'status'];
  readonly colunasRelatorio = ['nome', 'matricula', 'media', 'frequencia', 'situacao'];

  readonly formAviso = this.fb.nonNullable.group({
    titulo: ['', [Validators.required, Validators.maxLength(160)]],
    mensagem: ['', [Validators.required, Validators.maxLength(2000)]],
  });

  ngOnInit(): void {
    this.carregarTurma();
    this.carregarAlunos();
  }

  carregarTurma(): void {
    this.carregandoTurma.set(true);
    this.erro.set(null);
    this.academicoService.buscarTurma(this.turmaId()).subscribe({
      next: (turma) => {
        this.turma.set(turma);
        this.carregandoTurma.set(false);
      },
      error: () => {
        this.carregandoTurma.set(false);
        this.erro.set('Não foi possível carregar os dados da turma.');
      },
    });
  }

  carregarAlunos(): void {
    this.carregandoAlunos.set(true);
    this.academicoService.listarAlunosTurma(this.turmaId()).subscribe({
      next: (alunos) => {
        this.alunos.set(alunos);
        this.carregandoAlunos.set(false);
      },
      error: () => {
        this.carregandoAlunos.set(false);
      },
    });
  }

  carregarAvisos(): void {
    this.carregandoAvisos.set(true);
    this.avisoService.listar().subscribe({
      next: (avisos) => {
        this.avisos.set(
          avisos.filter((aviso) => aviso.turmaId === this.turmaId()),
        );
        this.carregandoAvisos.set(false);
      },
      error: () => {
        this.carregandoAvisos.set(false);
      },
    });
  }

  carregarRelatorio(): void {
    this.carregandoRelatorio.set(true);
    this.relatorioService.relatorioTurma(this.turmaId()).subscribe({
      next: (relatorio) => {
        this.relatorio.set(relatorio);
        this.carregandoRelatorio.set(false);
      },
      error: () => {
        this.carregandoRelatorio.set(false);
      },
    });
  }

  aoMudarAba(indice: number): void {
    this.aba.set(indice);
    if (indice === 0) {
      this.carregarAlunos();
    }
    if (indice === 3) {
      this.carregarAvisos();
    }
    if (indice === 4) {
      this.carregarRelatorio();
    }
  }

  criarAviso(): void {
    this.formAviso.markAllAsTouched();
    if (this.formAviso.invalid || this.salvandoAviso()) {
      return;
    }
    this.salvandoAviso.set(true);
    const { titulo, mensagem } = this.formAviso.getRawValue();
    this.avisoService
      .criar({
        titulo,
        mensagem,
        escopo: 'TURMA',
        turmaId: this.turmaId(),
      })
      .subscribe({
        next: () => {
          this.salvandoAviso.set(false);
          this.formAviso.reset({ titulo: '', mensagem: '' });
          this.snackBar.open('Aviso publicado para a turma.', 'Fechar', {
            duration: 3500,
          });
          this.carregarAvisos();
        },
        error: (erro: unknown) => {
          this.salvandoAviso.set(false);
          this.mostrarErro(erro, 'Não foi possível publicar o aviso.');
        },
      });
  }

  exportarRelatorio(): void {
    this.exportando.set(true);
    this.relatorioService.relatorioTurmaCsv(this.turmaId()).subscribe({
      next: (csv) => {
        this.exportando.set(false);
        const blob = new Blob([`\ufeff${csv}`], {
          type: 'text/csv;charset=utf-8;',
        });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `relatorio-turma-${this.turmaId()}.csv`;
        link.click();
        URL.revokeObjectURL(url);
      },
      error: () => {
        this.exportando.set(false);
        this.snackBar.open(
          'Não foi possível exportar o relatório.',
          'Fechar',
          { duration: 5000 },
        );
      },
    });
  }

  imprimir(): void {
    window.print();
  }

  rotuloStatus(status: StatusMatricula): string {
    return ROTULO_STATUS_MATRICULA[status];
  }

  classeStatus(status: StatusMatricula): string {
    return corStatusMatricula(status);
  }

  private mostrarErro(erro: unknown, fallback: string): void {
    const mensagem =
      erro instanceof HttpErrorResponse &&
      erro.error &&
      typeof erro.error === 'object' &&
      'mensagem' in erro.error
        ? String((erro.error as { mensagem: unknown }).mensagem)
        : fallback;
    this.snackBar.open(mensagem, 'Fechar', { duration: 5000 });
  }
}
