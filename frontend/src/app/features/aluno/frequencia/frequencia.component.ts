import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { AlunoService } from '../../../core/services/aluno.service';
import { AlunoFrequencia } from '../../../core/models/frequencia.models';
import { EmptyStateComponent } from '../../../shared/components/empty-state.component';
import { FrequenciaBadgeComponent } from '../../../shared/components/frequencia-badge.component';
import { PageHeaderComponent } from '../../../shared/components/page-header.component';
import { PercentualPipe } from '../../../shared/pipes/percentual.pipe';

@Component({
  selector: 'app-frequencia',
  standalone: true,
  imports: [
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressBarModule,
    PageHeaderComponent,
    EmptyStateComponent,
    FrequenciaBadgeComponent,
    PercentualPipe,
  ],
  templateUrl: './frequencia.component.html',
  styleUrl: './frequencia.component.scss',
})
export class FrequenciaComponent implements OnInit {
  private readonly alunoService = inject(AlunoService);

  readonly frequencias = signal<AlunoFrequencia[]>([]);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly emRisco = computed(() =>
    this.frequencias().filter((item) => item.alerta),
  );

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.alunoService.frequencia().subscribe({
      next: (frequencias) => {
        this.frequencias.set(frequencias);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar sua frequência.');
      },
    });
  }

  classeBarra(percentual: number | null, minimo: number): string {
    if (percentual === null) {
      return '';
    }
    if (percentual < minimo) {
      return 'frequencia-risco';
    }
    if (percentual < minimo + 5) {
      return 'frequencia-alerta';
    }
    return 'frequencia-ok';
  }
}
