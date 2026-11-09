import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
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
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { forkJoin } from 'rxjs';
import { DenunciaService } from '../../../core/services/denuncia.service';
import {
  AndamentoRequest,
  Denuncia,
  EstatisticasDenuncia,
  StatusDenuncia,
} from '../../../core/models/denuncia.models';
import { ApiError } from '../../../core/models/common.models';
import {
  ETAPAS_DENUNCIA,
  ROTULO_STATUS_DENUNCIA,
  corStatusDenuncia,
} from '../../../core/utils/rotulos';
import { EmptyStateComponent } from '../../../shared/components/empty-state.component';
import { PageHeaderComponent } from '../../../shared/components/page-header.component';
import { StatCardComponent } from '../../../shared/components/stat-card.component';
import { DataBrPipe } from '../../../shared/pipes/data-br.pipe';

@Component({
  selector: 'app-tratamento',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatSelectModule,
    MatTableModule,
    PageHeaderComponent,
    EmptyStateComponent,
    StatCardComponent,
    DataBrPipe,
  ],
  templateUrl: './tratamento.component.html',
  styleUrl: './tratamento.component.scss',
})
export class TratamentoComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly denunciaService = inject(DenunciaService);
  private readonly snackBar = inject(MatSnackBar);

  readonly denuncias = signal<Denuncia[]>([]);
  readonly estatisticas = signal<EstatisticasDenuncia | null>(null);
  readonly selecionada = signal<Denuncia | null>(null);
  readonly carregando = signal(true);
  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly filtroStatus = signal<StatusDenuncia | ''>('');
  readonly busca = signal('');

  readonly statusOpcoes: StatusDenuncia[] = [
    'RECEBIDA',
    'EM_TRIAGEM',
    'ENCAMINHADA',
    'EM_APURACAO',
    'RESPONDIDA',
    'ENCERRADA',
  ];
  readonly etapas = ETAPAS_DENUNCIA;
  readonly rotuloStatus = ROTULO_STATUS_DENUNCIA;
  readonly colunas = ['protocolo', 'categoria', 'status', 'criadoEm', 'acoes'];

  readonly formAndamento = this.fb.nonNullable.group({
    etapa: ['EM_TRIAGEM', [Validators.required]],
    descricao: ['', [Validators.required, Validators.maxLength(2000)]],
    status: [null as StatusDenuncia | null],
  });

  readonly filtradas = computed(() => {
    const status = this.filtroStatus();
    const termo = this.busca().trim().toLowerCase();
    return this.denuncias().filter((denuncia) => {
      const combinaStatus = !status || denuncia.status === status;
      const combinaTermo =
        !termo ||
        denuncia.protocolo.toLowerCase().includes(termo) ||
        denuncia.categoria.toLowerCase().includes(termo) ||
        denuncia.descricao.toLowerCase().includes(termo);
      return combinaStatus && combinaTermo;
    });
  });

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    forkJoin({
      denuncias: this.denunciaService.listar(),
      estatisticas: this.denunciaService.estatisticas(),
    }).subscribe({
      next: ({ denuncias, estatisticas }) => {
        this.denuncias.set(denuncias);
        this.estatisticas.set(estatisticas);
        const atual = this.selecionada();
        if (atual) {
          const atualizada = denuncias.find((item) => item.id === atual.id);
          this.selecionada.set(atualizada ?? null);
        }
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar as denúncias.');
      },
    });
  }

  selecionar(denuncia: Denuncia): void {
    this.selecionada.set(denuncia);
    this.formAndamento.reset({
      etapa: 'EM_TRIAGEM',
      descricao: '',
      status: null,
    });
  }

  registrarAndamento(): void {
    const denuncia = this.selecionada();
    if (!denuncia || this.salvando()) {
      return;
    }
    this.formAndamento.markAllAsTouched();
    if (this.formAndamento.invalid) {
      return;
    }

    const valores = this.formAndamento.getRawValue();
    const request: AndamentoRequest = {
      etapa: valores.etapa,
      descricao: valores.descricao,
      status: valores.status,
    };

    this.salvando.set(true);
    this.denunciaService.adicionarAndamento(denuncia.id, request).subscribe({
      next: (atualizada) => {
        this.salvando.set(false);
        this.selecionada.set(atualizada);
        this.formAndamento.reset({
          etapa: 'EM_TRIAGEM',
          descricao: '',
          status: null,
        });
        this.snackBar.open('Andamento registrado.', 'Fechar', {
          duration: 3500,
        });
        this.carregar();
      },
      error: (erro: unknown) => {
        this.salvando.set(false);
        const corpo =
          erro instanceof HttpErrorResponse ? (erro.error as ApiError | null) : null;
        this.snackBar.open(
          corpo?.mensagem ?? 'Não foi possível registrar o andamento.',
          'Fechar',
          { duration: 5000 },
        );
      },
    });
  }

  classeStatus(denuncia: Denuncia): string {
    return corStatusDenuncia(denuncia.status);
  }

  rotuloDoStatus(status: StatusDenuncia): string {
    return ROTULO_STATUS_DENUNCIA[status];
  }

  aoFiltrarStatus(valor: StatusDenuncia | ''): void {
    this.filtroStatus.set(valor);
  }

  aoBuscar(evento: Event): void {
    this.busca.set((evento.target as HTMLInputElement).value);
  }
}
