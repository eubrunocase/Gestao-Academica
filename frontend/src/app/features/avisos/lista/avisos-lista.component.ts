import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { AvisoService } from '../../../core/services/aviso.service';
import { Aviso, EscopoAviso } from '../../../core/models/comunicacao.models';
import { ROTULO_ESCOPO } from '../../../core/utils/rotulos';
import { EmptyStateComponent } from '../../../shared/components/empty-state.component';
import { PageHeaderComponent } from '../../../shared/components/page-header.component';
import { DataBrPipe } from '../../../shared/pipes/data-br.pipe';

@Component({
  selector: 'app-avisos-lista',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatSelectModule,
    PageHeaderComponent,
    EmptyStateComponent,
    DataBrPipe,
  ],
  templateUrl: './avisos-lista.component.html',
  styleUrl: './avisos-lista.component.scss',
})
export class AvisosListaComponent implements OnInit {
  private readonly avisoService = inject(AvisoService);
  private readonly auth = inject(AuthService);

  readonly busca = new FormControl('', { nonNullable: true });
  readonly escopoFiltro = new FormControl<EscopoAviso | ''>('', {
    nonNullable: true,
  });
  readonly buscaTermo = toSignal(this.busca.valueChanges, {
    initialValue: this.busca.value,
  });
  readonly escopoSelecionado = toSignal(this.escopoFiltro.valueChanges, {
    initialValue: this.escopoFiltro.value,
  });
  readonly avisos = signal<Aviso[]>([]);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly rotuloEscopo = ROTULO_ESCOPO;
  readonly escopos: EscopoAviso[] = ['GERAL', 'CURSO', 'TURMA'];
  readonly podeCriar = computed(() => this.auth.perfil() !== 'ALUNO');

  readonly filtrados = computed(() => {
    const termo = this.buscaTermo().trim().toLowerCase();
    const escopo = this.escopoSelecionado();
    return this.avisos().filter((aviso) => {
      const combinaEscopo = !escopo || aviso.escopo === escopo;
      const combinaTermo =
        !termo ||
        aviso.titulo.toLowerCase().includes(termo) ||
        aviso.mensagem.toLowerCase().includes(termo) ||
        aviso.autor.toLowerCase().includes(termo);
      return combinaEscopo && combinaTermo;
    });
  });

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.avisoService.listar().subscribe({
      next: (avisos) => {
        this.avisos.set(avisos);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar os avisos.');
      },
    });
  }

  limparFiltros(): void {
    this.busca.setValue('');
    this.escopoFiltro.setValue('');
  }

  contexto(aviso: Aviso): string {
    if (aviso.escopo === 'TURMA') {
      return aviso.turmaDisciplina ?? 'Turma';
    }
    if (aviso.escopo === 'CURSO') {
      return aviso.cursoNome ?? 'Curso';
    }
    return 'Todos os usuários';
  }
}
