import { DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  FormControl,
  ReactiveFormsModule,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { AuthService } from '../../../core/auth/auth.service';
import { AcademicoService } from '../../../core/services/academico.service';
import { ProfessorService } from '../../../core/services/professor.service';
import { RelatorioService } from '../../../core/services/relatorio.service';
import {
  AlunoTurma,
  StatusMatricula,
  Turma,
} from '../../../core/models/academico.models';
import { BoletimAluno } from '../../../core/models/avaliacao.models';
import {
  BoletimRelatorio,
  HistoricoAluno,
  RelatorioTurma,
} from '../../../core/models/relatorio.models';
import {
  ROTULO_STATUS_MATRICULA,
  corStatusMatricula,
} from '../../../core/utils/rotulos';
import { EmptyStateComponent } from '../../../shared/components/empty-state.component';
import { FrequenciaBadgeComponent } from '../../../shared/components/frequencia-badge.component';
import { PageHeaderComponent } from '../../../shared/components/page-header.component';
import { StatCardComponent } from '../../../shared/components/stat-card.component';
import { PercentualPipe } from '../../../shared/pipes/percentual.pipe';

type TipoRelatorio = 'TURMA' | 'BOLETIM' | 'HISTORICO';

@Component({
  selector: 'app-relatorios',
  standalone: true,
  imports: [
    DecimalPipe,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatProgressBarModule,
    MatSelectModule,
    MatTableModule,
    PageHeaderComponent,
    EmptyStateComponent,
    StatCardComponent,
    FrequenciaBadgeComponent,
    PercentualPipe,
  ],
  templateUrl: './relatorios.component.html',
  styleUrl: './relatorios.component.scss',
})
export class RelatoriosComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly academicoService = inject(AcademicoService);
  private readonly professorService = inject(ProfessorService);
  private readonly relatorioService = inject(RelatorioService);
  private readonly snackBar = inject(MatSnackBar);

  readonly tipos = computed<{ valor: TipoRelatorio; rotulo: string }[]>(() => {
    const opcoes: { valor: TipoRelatorio; rotulo: string }[] = [
      { valor: 'TURMA', rotulo: 'Lista da turma' },
      { valor: 'BOLETIM', rotulo: 'Boletim do aluno' },
    ];
    if (this.auth.perfil() !== 'PROFESSOR') {
      opcoes.push({ valor: 'HISTORICO', rotulo: 'Histórico do aluno' });
    }
    return opcoes;
  });

  readonly tipo = new FormControl<TipoRelatorio>('TURMA', {
    nonNullable: true,
  });
  readonly periodoFiltro = new FormControl('', { nonNullable: true });
  readonly disciplinaFiltro = new FormControl<number | ''>('', {
    nonNullable: true,
  });
  readonly turmaSelecionada = new FormControl<number | null>(null);
  readonly alunoSelecionado = new FormControl<number | null>(null);
  readonly periodoSelecionado = toSignal(this.periodoFiltro.valueChanges, {
    initialValue: this.periodoFiltro.value,
  });
  readonly disciplinaSelecionada = toSignal(this.disciplinaFiltro.valueChanges, {
    initialValue: this.disciplinaFiltro.value,
  });

  readonly turmas = signal<Turma[]>([]);
  readonly alunosTurma = signal<AlunoTurma[]>([]);
  readonly relatorio = signal<RelatorioTurma | null>(null);
  readonly boletim = signal<BoletimRelatorio | null>(null);
  readonly historico = signal<HistoricoAluno | null>(null);
  readonly carregando = signal(true);
  readonly carregandoRelatorio = signal(false);
  readonly carregandoAlunos = signal(false);
  readonly exportando = signal(false);
  readonly erro = signal<string | null>(null);

  readonly colunasTurma = ['nome', 'matricula', 'media', 'frequencia', 'situacao'];
  readonly colunasHistorico = [
    'periodo',
    'disciplina',
    'professor',
    'media',
    'frequencia',
    'situacao',
  ];
  readonly colunasAvaliacoes = ['descricao', 'peso', 'nota'];

  readonly periodos = computed(() =>
    Array.from(new Set(this.turmas().map((turma) => turma.periodoLetivo))).sort().reverse(),
  );
  readonly disciplinas = computed(() => {
    const mapa = new Map<number, string>();
    for (const turma of this.turmas()) {
      mapa.set(turma.disciplina.id, turma.disciplina.nome);
    }
    return Array.from(mapa.entries())
      .map(([id, nome]) => ({ id, nome }))
      .sort((a, b) => a.nome.localeCompare(b.nome));
  });
  readonly turmasFiltradas = computed(() =>
    this.turmas().filter((turma) => {
      const periodo = this.periodoSelecionado();
      const disciplina = this.disciplinaSelecionada();
      return (
        (!periodo || turma.periodoLetivo === periodo) &&
        (!disciplina || turma.disciplina.id === disciplina)
      );
    }),
  );

  ngOnInit(): void {
    const turmas$ =
      this.auth.perfil() === 'PROFESSOR'
        ? this.professorService.minhasTurmas()
        : this.academicoService.listarTurmas();
    turmas$.subscribe({
      next: (turmas) => {
        this.turmas.set(turmas);
        this.carregando.set(false);
        if (turmas.length === 1) {
          this.turmaSelecionada.setValue(turmas[0].id);
        }
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar as turmas.');
      },
    });

    this.tipo.valueChanges.subscribe(() => this.limparResultados());
    this.turmaSelecionada.valueChanges.subscribe(() => {
      this.limparResultados();
      this.carregarTurmaSelecionada();
    });
    this.periodoFiltro.valueChanges.subscribe(() => {
      this.turmaSelecionada.setValue(null);
      this.limparResultados();
    });
    this.disciplinaFiltro.valueChanges.subscribe(() => {
      this.turmaSelecionada.setValue(null);
      this.limparResultados();
    });
  }

  carregarTurmaSelecionada(): void {
    const turmaId = this.turmaSelecionada.value;
    if (!turmaId) {
      this.alunosTurma.set([]);
      return;
    }

    this.carregandoAlunos.set(true);
    this.academicoService.listarAlunosTurma(turmaId).subscribe({
      next: (alunos) => {
        this.alunosTurma.set(alunos);
        this.carregandoAlunos.set(false);
      },
      error: () => {
        this.alunosTurma.set([]);
        this.carregandoAlunos.set(false);
      },
    });

    if (this.tipo.value === 'TURMA') {
      this.carregarRelatorioTurma(turmaId);
    }
  }

  carregarRelatorioTurma(turmaId: number): void {
    this.carregandoRelatorio.set(true);
    this.erro.set(null);
    this.relatorioService.relatorioTurma(turmaId).subscribe({
      next: (relatorio) => {
        this.relatorio.set(relatorio);
        this.carregandoRelatorio.set(false);
      },
      error: () => {
        this.carregandoRelatorio.set(false);
        this.erro.set('Não foi possível gerar o relatório da turma.');
      },
    });
  }

  carregarBoletim(): void {
    const aluno = this.alunoSelecionado.value;
    if (!aluno) {
      return;
    }
    const matricula = this.alunosTurma().find((item) => item.alunoId === aluno);
    if (!matricula) {
      return;
    }
    this.carregandoRelatorio.set(true);
    this.erro.set(null);
    this.relatorioService.boletim(matricula.matriculaId).subscribe({
      next: (boletim) => {
        this.boletim.set(boletim);
        this.carregandoRelatorio.set(false);
      },
      error: () => {
        this.carregandoRelatorio.set(false);
        this.erro.set('Não foi possível gerar o boletim.');
      },
    });
  }

  carregarHistorico(): void {
    const aluno = this.alunoSelecionado.value;
    if (!aluno) {
      return;
    }
    this.carregandoRelatorio.set(true);
    this.erro.set(null);
    this.relatorioService.historico(aluno).subscribe({
      next: (historico) => {
        this.historico.set(historico);
        this.carregandoRelatorio.set(false);
      },
      error: () => {
        this.carregandoRelatorio.set(false);
        this.erro.set('Não foi possível gerar o histórico.');
      },
    });
  }

  gerar(): void {
    const turmaId = this.turmaSelecionada.value;
    if (!turmaId) {
      this.snackBar.open('Selecione uma turma.', 'Fechar', { duration: 3500 });
      return;
    }
    if (this.tipo.value === 'TURMA') {
      this.carregarRelatorioTurma(turmaId);
      return;
    }
    if (!this.alunoSelecionado.value) {
      this.snackBar.open('Selecione um aluno.', 'Fechar', { duration: 3500 });
      return;
    }
    if (this.tipo.value === 'BOLETIM') {
      this.carregarBoletim();
    } else {
      this.carregarHistorico();
    }
  }

  exportar(): void {
    if (this.tipo.value === 'TURMA' && this.turmaSelecionada.value) {
      this.exportando.set(true);
      this.relatorioService
        .relatorioTurmaCsv(this.turmaSelecionada.value)
        .subscribe({
          next: (csv) => {
            this.exportando.set(false);
            this.baixarCsv(csv, `relatorio-turma-${this.turmaSelecionada.value}.csv`);
          },
          error: () => {
            this.exportando.set(false);
            this.snackBar.open('Não foi possível exportar o relatório.', 'Fechar', {
              duration: 5000,
            });
          },
        });
      return;
    }

    const historico = this.historico();
    if (this.tipo.value === 'HISTORICO' && historico) {
      this.exportando.set(true);
      this.relatorioService.historicoCsv(historico.alunoId).subscribe({
        next: (csv) => {
          this.exportando.set(false);
          this.baixarCsv(csv, `historico-${historico.numeroMatricula}.csv`);
        },
        error: () => {
          this.exportando.set(false);
          this.snackBar.open('Não foi possível exportar o histórico.', 'Fechar', {
            duration: 5000,
          });
        },
      });
      return;
    }

    this.imprimir();
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

  textoNotaNecessaria(boletim: BoletimAluno): string {
    if (boletim.notaNecessaria === null) {
      return boletim.media !== null && boletim.media >= 7
        ? 'Média atingida'
        : '—';
    }
    return boletim.notaNecessaria > 10
      ? 'Não é mais possível'
      : new Intl.NumberFormat('pt-BR', {
          minimumFractionDigits: 2,
          maximumFractionDigits: 2,
        }).format(boletim.notaNecessaria);
  }

  private limparResultados(): void {
    this.relatorio.set(null);
    this.boletim.set(null);
    this.historico.set(null);
    this.alunoSelecionado.setValue(null);
  }

  private baixarCsv(conteudo: string, nomeArquivo: string): void {
    const blob = new Blob([`\ufeff${conteudo}`], {
      type: 'text/csv;charset=utf-8;',
    });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = nomeArquivo;
    link.click();
    URL.revokeObjectURL(url);
  }
}
