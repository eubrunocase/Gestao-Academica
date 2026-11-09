import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { DenunciaService } from '../../../core/services/denuncia.service';
import {
  EstatisticasDenuncia,
  StatusDenuncia,
} from '../../../core/models/denuncia.models';
import {
  ROTULO_STATUS_DENUNCIA,
  corStatusDenuncia,
} from '../../../core/utils/rotulos';
import { EmptyStateComponent } from '../../../shared/components/empty-state.component';
import { PageHeaderComponent } from '../../../shared/components/page-header.component';
import { StatCardComponent } from '../../../shared/components/stat-card.component';
import { PercentualPipe } from '../../../shared/pipes/percentual.pipe';

@Component({
  selector: 'app-estatisticas',
  standalone: true,
  imports: [
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressBarModule,
    MatTableModule,
    PageHeaderComponent,
    EmptyStateComponent,
    StatCardComponent,
    PercentualPipe,
  ],
  templateUrl: './estatisticas.component.html',
  styleUrl: './estatisticas.component.scss',
})
export class EstatisticasComponent implements OnInit {
  private readonly denunciaService = inject(DenunciaService);

  readonly estatisticas = signal<EstatisticasDenuncia | null>(null);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly rotuloStatus = ROTULO_STATUS_DENUNCIA;
  readonly colunasStatus = ['status', 'total', 'percentual'];
  readonly colunasCategoria = ['categoria', 'total', 'percentual'];

  readonly statusLista = computed(() => {
    const dados = this.estatisticas();
    if (!dados) {
      return [];
    }
    return Object.entries(dados.porStatus)
      .map(([status, total]) => ({
        status: status as StatusDenuncia,
        total,
        percentual: dados.total ? (total / dados.total) * 100 : 0,
      }))
      .sort((a, b) => b.total - a.total);
  });

  readonly categoriaLista = computed(() => {
    const dados = this.estatisticas();
    if (!dados) {
      return [];
    }
    return Object.entries(dados.porCategoria)
      .map(([categoria, total]) => ({
        categoria,
        total,
        percentual: dados.total ? (total / dados.total) * 100 : 0,
      }))
      .sort((a, b) => b.total - a.total);
  });

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.denunciaService.estatisticas().subscribe({
      next: (estatisticas) => {
        this.estatisticas.set(estatisticas);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar as estatísticas.');
      },
    });
  }

  classeStatus(status: StatusDenuncia): string {
    return corStatusDenuncia(status);
  }

  rotuloDoStatus(status: StatusDenuncia): string {
    return ROTULO_STATUS_DENUNCIA[status];
  }
}
