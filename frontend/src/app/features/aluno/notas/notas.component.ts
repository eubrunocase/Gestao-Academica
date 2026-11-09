import { DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { AlunoService } from '../../../core/services/aluno.service';
import { BoletimAluno } from '../../../core/models/avaliacao.models';
import { StatusMatricula } from '../../../core/models/academico.models';
import {
  ROTULO_STATUS_MATRICULA,
  corStatusMatricula,
} from '../../../core/utils/rotulos';
import { EmptyStateComponent } from '../../../shared/components/empty-state.component';
import { FrequenciaBadgeComponent } from '../../../shared/components/frequencia-badge.component';
import { PageHeaderComponent } from '../../../shared/components/page-header.component';
import { PercentualPipe } from '../../../shared/pipes/percentual.pipe';

@Component({
  selector: 'app-notas',
  standalone: true,
  imports: [
    DecimalPipe,
    MatButtonModule,
    MatCardModule,
    MatExpansionModule,
    MatIconModule,
    MatProgressBarModule,
    MatTableModule,
    PageHeaderComponent,
    EmptyStateComponent,
    FrequenciaBadgeComponent,
    PercentualPipe,
  ],
  templateUrl: './notas.component.html',
  styleUrl: './notas.component.scss',
})
export class NotasComponent implements OnInit {
  private readonly alunoService = inject(AlunoService);

  readonly boletins = signal<BoletimAluno[]>([]);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly colunasAvaliacoes = ['descricao', 'peso', 'nota'];

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.alunoService.notas().subscribe({
      next: (boletins) => {
        this.boletins.set(boletins);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar suas notas.');
      },
    });
  }

  rotuloStatus(status: StatusMatricula): string {
    return ROTULO_STATUS_MATRICULA[status];
  }

  classeStatus(boletim: BoletimAluno): string {
    return boletim.status === 'ATIVA'
      ? 'badge-warning'
      : corStatusMatricula(boletim.status);
  }

  textoNotaNecessaria(boletim: BoletimAluno): string {
    if (boletim.notaNecessaria === null) {
      return boletim.media !== null && boletim.media >= 7
        ? 'Média de aprovação atingida'
        : '—';
    }
    return boletim.notaNecessaria > 10
      ? 'Não é mais possível atingir a média'
      : `Precisa de ${new Intl.NumberFormat('pt-BR', {
          minimumFractionDigits: 2,
          maximumFractionDigits: 2,
        }).format(boletim.notaNecessaria)}`;
  }
}
