import { DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { BaseChartDirective } from 'ng2-charts';
import {
  ChartData,
  ChartOptions,
} from 'chart.js';
import { AcademicoService } from '../../../core/services/academico.service';
import { RelatorioService } from '../../../core/services/relatorio.service';
import { Turma } from '../../../core/models/academico.models';
import {
  IndicadorDisciplina,
  IndicadorTurma,
  PontoAluno,
} from '../../../core/models/relatorio.models';
import { EmptyStateComponent } from '../../../shared/components/empty-state.component';
import { FrequenciaBadgeComponent } from '../../../shared/components/frequencia-badge.component';
import { PageHeaderComponent } from '../../../shared/components/page-header.component';
import { StatCardComponent } from '../../../shared/components/stat-card.component';
import { PercentualPipe } from '../../../shared/pipes/percentual.pipe';

@Component({
  selector: 'app-indicadores',
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
    BaseChartDirective,
    PageHeaderComponent,
    EmptyStateComponent,
    StatCardComponent,
    FrequenciaBadgeComponent,
    PercentualPipe,
  ],
  templateUrl: './indicadores.component.html',
  styleUrl: './indicadores.component.scss',
})
export class IndicadoresComponent implements OnInit {
  private readonly academicoService = inject(AcademicoService);
  private readonly relatorioService = inject(RelatorioService);

  readonly turmaSelecionada = new FormControl<number | null>(null);
  readonly disciplinas = signal<IndicadorDisciplina[]>([]);
  readonly turmas = signal<Turma[]>([]);
  readonly indicadorTurma = signal<IndicadorTurma | null>(null);
  readonly carregando = signal(true);
  readonly carregandoTurma = signal(false);
  readonly erro = signal<string | null>(null);
  readonly colunasRisco = ['nome', 'media', 'frequencia'];

  readonly barOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      y: { beginAtZero: true, max: 10, title: { display: true, text: 'Média' } },
    },
  };

  readonly pizzaOptions: ChartOptions<'doughnut'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { position: 'bottom' } },
  };

  readonly scatterOptions: ChartOptions<'scatter'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        callbacks: {
          label: (contexto) => {
            const ponto = contexto.raw as { x: number; y: number };
            return `Frequência ${ponto.x.toFixed(1)}% · Média ${ponto.y.toFixed(2)}`;
          },
        },
      },
    },
    scales: {
      x: {
        min: 0,
        max: 100,
        title: { display: true, text: 'Frequência (%)' },
      },
      y: {
        min: 0,
        max: 10,
        title: { display: true, text: 'Média' },
      },
    },
  };

  readonly evolucaoOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      y: { beginAtZero: true, max: 10, title: { display: true, text: 'Média' } },
    },
  };

  readonly mediaGeral = computed(() => {
    const valores = this.disciplinas()
      .map((item) => item.media)
      .filter((valor): valor is number => valor !== null);
    return valores.length
      ? Math.round((valores.reduce((a, b) => a + b, 0) / valores.length) * 100) / 100
      : null;
  });
  readonly taxaAprovacao = computed(() => {
    const valores = this.disciplinas()
      .map((item) => item.taxaAprovacao)
      .filter((valor): valor is number => valor !== null);
    return valores.length
      ? Math.round((valores.reduce((a, b) => a + b, 0) / valores.length) * 100) / 100
      : null;
  });
  readonly emAndamento = computed(() =>
    this.disciplinas().reduce((total, item) => total + item.emAndamento, 0),
  );

  readonly barData = computed<ChartData<'bar'>>(() => ({
    labels: this.disciplinas().map((item) => item.disciplina),
    datasets: [
      {
        label: 'Média por disciplina',
        data: this.disciplinas().map((item) => item.media ?? 0),
        backgroundColor: '#0b3d91',
        borderRadius: 6,
        maxBarThickness: 44,
      },
    ],
  }));

  readonly pizzaData = computed<ChartData<'doughnut'>>(() => {
    const aprovados = this.disciplinas().reduce(
      (total, item) => total + item.aprovados,
      0,
    );
    const reprovadosNota = this.disciplinas().reduce(
      (total, item) => total + item.reprovadosNota,
      0,
    );
    const reprovadosFalta = this.disciplinas().reduce(
      (total, item) => total + item.reprovadosFalta,
      0,
    );
    return {
      labels: ['Aprovados', 'Reprovados por nota', 'Reprovados por falta'],
      datasets: [
        {
          data: [aprovados, reprovadosNota, reprovadosFalta],
          backgroundColor: ['#1b6b3a', '#b3261e', '#c47f00'],
          borderWidth: 0,
        },
      ],
    };
  });

  readonly scatterData = computed<ChartData<'scatter'>>(() => {
    const pontos = (this.indicadorTurma()?.alunos ?? [])
      .filter(
        (aluno) => aluno.media !== null && aluno.frequencia !== null,
      )
      .map((aluno) => ({
        x: aluno.frequencia as number,
        y: aluno.media as number,
      }));
    return {
      datasets: [
        {
          label: 'Alunos',
          data: pontos,
          backgroundColor: '#0f7b6c',
          pointRadius: 6,
          pointHoverRadius: 8,
        },
      ],
    };
  });

  readonly evolucaoData = computed<ChartData<'bar'>>(() => ({
    labels: (this.indicadorTurma()?.evolucao ?? []).map(
      (item) => item.descricao,
    ),
    datasets: [
      {
        label: 'Média da turma por avaliação',
        data: (this.indicadorTurma()?.evolucao ?? []).map(
          (item) => item.media ?? 0,
        ),
        backgroundColor: '#0f5fa8',
        borderRadius: 6,
        maxBarThickness: 44,
      },
    ],
  }));

  readonly alunosRisco = computed<PontoAluno[]>(() =>
    (this.indicadorTurma()?.alunos ?? []).filter(
      (aluno) =>
        (aluno.media !== null && aluno.media < 7) ||
        (aluno.frequencia !== null && aluno.frequencia < 75),
    ),
  );

  ngOnInit(): void {
    this.carregarDisciplinas();
    this.academicoService.listarTurmas().subscribe({
      next: (turmas) => {
        this.turmas.set(turmas);
        if (turmas.length > 0) {
          this.turmaSelecionada.setValue(turmas[0].id);
        }
      },
    });
    this.turmaSelecionada.valueChanges.subscribe((turmaId) => {
      if (turmaId) {
        this.carregarTurma(turmaId);
      }
    });
  }

  carregarDisciplinas(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.relatorioService.indicadoresDisciplinas().subscribe({
      next: (disciplinas) => {
        this.disciplinas.set(disciplinas);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar os indicadores.');
      },
    });
  }

  carregarTurma(turmaId: number): void {
    this.carregandoTurma.set(true);
    this.relatorioService.indicadoresTurma(turmaId).subscribe({
      next: (indicador) => {
        this.indicadorTurma.set(indicador);
        this.carregandoTurma.set(false);
      },
      error: () => {
        this.carregandoTurma.set(false);
      },
    });
  }

  recarregar(): void {
    this.carregarDisciplinas();
    const turmaId = this.turmaSelecionada.value;
    if (turmaId) {
      this.carregarTurma(turmaId);
    }
  }
}
