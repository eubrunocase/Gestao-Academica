import { DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { AlunoService } from '../../../core/services/aluno.service';
import { RelatorioService } from '../../../core/services/relatorio.service';
import { StatusMatricula } from '../../../core/models/academico.models';
import { HistoricoAluno } from '../../../core/models/relatorio.models';
import {
  ROTULO_STATUS_MATRICULA,
  corStatusMatricula,
} from '../../../core/utils/rotulos';
import { EmptyStateComponent } from '../../../shared/components/empty-state.component';
import { PageHeaderComponent } from '../../../shared/components/page-header.component';
import { StatCardComponent } from '../../../shared/components/stat-card.component';
import { PercentualPipe } from '../../../shared/pipes/percentual.pipe';

@Component({
  selector: 'app-historico',
  standalone: true,
  imports: [
    DecimalPipe,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressBarModule,
    MatTableModule,
    PageHeaderComponent,
    StatCardComponent,
    EmptyStateComponent,
    PercentualPipe,
  ],
  templateUrl: './historico.component.html',
  styleUrl: './historico.component.scss',
})
export class HistoricoComponent implements OnInit {
  private readonly alunoService = inject(AlunoService);
  private readonly relatorioService = inject(RelatorioService);
  private readonly snackBar = inject(MatSnackBar);

  readonly historico = signal<HistoricoAluno | null>(null);
  readonly carregando = signal(true);
  readonly exportando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly colunas = [
    'periodo',
    'disciplina',
    'curso',
    'carga',
    'professor',
    'media',
    'frequencia',
    'situacao',
  ];

  readonly aprovadas = computed(
    () =>
      this.historico()?.disciplinas.filter((item) => item.status === 'APROVADO')
        .length ?? 0,
  );
  readonly reprovadas = computed(
    () =>
      this.historico()?.disciplinas.filter(
        (item) => item.status === 'REPROVADO_NOTA' || item.status === 'REPROVADO_FALTA',
      ).length ?? 0,
  );

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.alunoService.historico().subscribe({
      next: (historico) => {
        this.historico.set(historico);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar seu histórico.');
      },
    });
  }

  exportar(): void {
    const dados = this.historico();
    if (!dados) {
      return;
    }
    this.exportando.set(true);
    this.relatorioService.historicoCsv(dados.alunoId).subscribe({
      next: (csv) => {
        this.exportando.set(false);
        this.baixarCsv(csv, `historico-${dados.numeroMatricula}.csv`);
      },
      error: () => {
        this.exportando.set(false);
        this.snackBar.open(
          'Não foi possível exportar o arquivo. Tente novamente.',
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
