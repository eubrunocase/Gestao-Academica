import { DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { AlunoService } from '../../../core/services/aluno.service';
import { BoletimAluno } from '../../../core/models/avaliacao.models';
import { AlunoDashboard } from '../../../core/models/relatorio.models';
import { ROTULO_ESCOPO, ROTULO_STATUS_MATRICULA, corStatusMatricula } from '../../../core/utils/rotulos';
import { EmptyStateComponent } from '../../../shared/components/empty-state.component';
import { PageHeaderComponent } from '../../../shared/components/page-header.component';
import { StatCardComponent } from '../../../shared/components/stat-card.component';
import { DataBrPipe } from '../../../shared/pipes/data-br.pipe';
import { PercentualPipe } from '../../../shared/pipes/percentual.pipe';

@Component({
  selector: 'app-aluno-dashboard',
  standalone: true,
  imports: [
    DecimalPipe,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressBarModule,
    MatTableModule,
    PageHeaderComponent,
    StatCardComponent,
    EmptyStateComponent,
    DataBrPipe,
    PercentualPipe,
  ],
  templateUrl: './aluno-dashboard.component.html',
  styleUrl: './aluno-dashboard.component.scss',
})
export class AlunoDashboardComponent implements OnInit {
  private readonly alunoService = inject(AlunoService);
  readonly auth = inject(AuthService);

  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly dados = signal<AlunoDashboard | null>(null);
  readonly disciplinasEmRisco = computed<BoletimAluno[]>(
    () => this.dados()?.boletins.filter((boletim) => boletim.alertaFrequencia) ?? [],
  );
  readonly colunasBoletins = ['disciplina', 'media', 'frequencia', 'situacao'];
  readonly atalhos = [
    { rotulo: 'Matrícula', descricao: 'Turmas disponíveis e cancelamento', icone: 'how_to_reg', rota: '/aluno/matricula' },
    { rotulo: 'Notas', descricao: 'Avaliações, médias e situação', icone: 'grade', rota: '/aluno/notas' },
    { rotulo: 'Frequência', descricao: 'Presenças, faltas e alertas', icone: 'fact_check', rota: '/aluno/frequencia' },
    { rotulo: 'Histórico', descricao: 'Disciplinas cursadas e exportação', icone: 'history_edu', rota: '/aluno/historico' },
  ];

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.alunoService.dashboard().subscribe({
      next: (dados) => {
        this.dados.set(dados);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar o resumo acadêmico.');
      },
    });
  }

  nomesDisciplinasRisco(): string {
    return this.disciplinasEmRisco()
      .map((boletim) => boletim.disciplina)
      .join(', ');
  }

  rotuloStatus(boletim: BoletimAluno): string {
    return ROTULO_STATUS_MATRICULA[boletim.status];
  }

  classeStatus(boletim: BoletimAluno): string {
    return corStatusMatricula(boletim.status);
  }

  rotuloEscopo(escopo: string): string {
    return ROTULO_ESCOPO[escopo as keyof typeof ROTULO_ESCOPO] ?? escopo;
  }
}
