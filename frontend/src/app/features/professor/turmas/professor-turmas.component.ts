import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { RouterLink } from '@angular/router';
import { ProfessorService } from '../../../core/services/professor.service';
import { Turma } from '../../../core/models/academico.models';
import { EmptyStateComponent } from '../../../shared/components/empty-state.component';
import { PageHeaderComponent } from '../../../shared/components/page-header.component';
import { StatCardComponent } from '../../../shared/components/stat-card.component';

@Component({
  selector: 'app-professor-turmas',
  standalone: true,
  imports: [
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressBarModule,
    RouterLink,
    PageHeaderComponent,
    StatCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './professor-turmas.component.html',
  styleUrl: './professor-turmas.component.scss',
})
export class ProfessorTurmasComponent implements OnInit {
  private readonly professorService = inject(ProfessorService);

  readonly turmas = signal<Turma[]>([]);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly ativas = computed(() => this.turmas().filter((t) => !t.encerrada).length);
  readonly encerradas = computed(() => this.turmas().filter((t) => t.encerrada).length);

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.professorService.minhasTurmas().subscribe({
      next: (turmas) => {
        this.turmas.set(turmas);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar suas turmas.');
      },
    });
  }
}
