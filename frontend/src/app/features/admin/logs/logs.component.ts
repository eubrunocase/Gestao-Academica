import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { AdminService } from '../../../core/services/admin.service';
import {
  EventoSeguranca,
  LogAuditoria,
  LogSeguranca,
} from '../../../core/models/auditoria.models';
import {
  ROTULO_EVENTO_SEGURANCA,
  corEvento,
} from '../../../core/utils/rotulos';
import { EmptyStateComponent } from '../../../shared/components/empty-state.component';
import { PageHeaderComponent } from '../../../shared/components/page-header.component';
import { DataBrPipe } from '../../../shared/pipes/data-br.pipe';

@Component({
  selector: 'app-logs',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatSelectModule,
    MatTableModule,
    MatTabsModule,
    PageHeaderComponent,
    EmptyStateComponent,
    DataBrPipe,
  ],
  templateUrl: './logs.component.html',
  styleUrl: './logs.component.scss',
})
export class LogsComponent implements OnInit {
  private readonly adminService = inject(AdminService);

  readonly eventos: EventoSeguranca[] = [
    'LOGIN_SUCESSO',
    'LOGIN_FALHA',
    'ACESSO_NEGADO',
    'BLOQUEIO',
    'NAO_AUTENTICADO',
  ];
  readonly rotuloEvento = ROTULO_EVENTO_SEGURANCA;

  readonly seguranca = signal<LogSeguranca[]>([]);
  readonly auditoria = signal<LogAuditoria[]>([]);
  readonly totalSeguranca = signal(0);
  readonly totalAuditoria = signal(0);
  readonly paginaSeguranca = signal(0);
  readonly paginaAuditoria = signal(0);
  readonly tamanho = signal(30);
  readonly carregandoSeguranca = signal(true);
  readonly carregandoAuditoria = signal(true);
  readonly erroSeguranca = signal<string | null>(null);
  readonly erroAuditoria = signal<string | null>(null);

  readonly eventoFiltro = new FormControl<EventoSeguranca | ''>('', {
    nonNullable: true,
  });
  readonly ipFiltro = new FormControl('', { nonNullable: true });
  readonly dataSeguranca = new FormControl('', { nonNullable: true });
  readonly acaoFiltro = new FormControl('', { nonNullable: true });
  readonly usuarioAuditoria = new FormControl('', { nonNullable: true });
  readonly dataAuditoria = new FormControl('', { nonNullable: true });

  readonly eventoSelecionado = toSignal(this.eventoFiltro.valueChanges, {
    initialValue: this.eventoFiltro.value,
  });
  readonly ipBusca = toSignal(this.ipFiltro.valueChanges, {
    initialValue: this.ipFiltro.value,
  });
  readonly dataSegurancaBusca = toSignal(this.dataSeguranca.valueChanges, {
    initialValue: this.dataSeguranca.value,
  });
  readonly acaoBusca = toSignal(this.acaoFiltro.valueChanges, {
    initialValue: this.acaoFiltro.value,
  });
  readonly usuarioBusca = toSignal(this.usuarioAuditoria.valueChanges, {
    initialValue: this.usuarioAuditoria.value,
  });
  readonly dataAuditoriaBusca = toSignal(this.dataAuditoria.valueChanges, {
    initialValue: this.dataAuditoria.value,
  });

  readonly colunasSeguranca = ['usuario', 'evento', 'ip', 'criadoEm'];
  readonly colunasAuditoria = [
    'usuario',
    'acao',
    'entidade',
    'detalhe',
    'criadoEm',
  ];

  readonly segurancaFiltrada = computed(() => {
    const evento = this.eventoSelecionado();
    const ip = this.ipBusca().trim();
    const data = this.dataSegurancaBusca();
    return this.seguranca().filter((log) => {
      return (
        (!evento || log.evento === evento) &&
        (!ip || log.ip.includes(ip)) &&
        (!data || log.criadoEm.startsWith(data))
      );
    });
  });

  readonly auditoriaFiltrada = computed(() => {
    const acao = this.acaoBusca().trim().toLowerCase();
    const usuario = this.usuarioBusca().trim().toLowerCase();
    const data = this.dataAuditoriaBusca();
    return this.auditoria().filter((log) => {
      return (
        (!acao || log.acao.toLowerCase().includes(acao)) &&
        (!usuario || (log.usuario ?? '').toLowerCase().includes(usuario)) &&
        (!data || log.criadoEm.startsWith(data))
      );
    });
  });

  ngOnInit(): void {
    this.carregarSeguranca(0);
    this.carregarAuditoria(0);
  }

  carregarSeguranca(pagina: number): void {
    this.carregandoSeguranca.set(true);
    this.erroSeguranca.set(null);
    this.adminService.logsSeguranca(pagina, this.tamanho()).subscribe({
      next: (resposta) => {
        this.seguranca.set(resposta.content);
        this.totalSeguranca.set(resposta.totalElements);
        this.paginaSeguranca.set(resposta.page);
        this.carregandoSeguranca.set(false);
      },
      error: () => {
        this.carregandoSeguranca.set(false);
        this.erroSeguranca.set('Não foi possível carregar os logs de segurança.');
      },
    });
  }

  carregarAuditoria(pagina: number): void {
    this.carregandoAuditoria.set(true);
    this.erroAuditoria.set(null);
    this.adminService.logsAuditoria(pagina, this.tamanho()).subscribe({
      next: (resposta) => {
        this.auditoria.set(resposta.content);
        this.totalAuditoria.set(resposta.totalElements);
        this.paginaAuditoria.set(resposta.page);
        this.carregandoAuditoria.set(false);
      },
      error: () => {
        this.carregandoAuditoria.set(false);
        this.erroAuditoria.set('Não foi possível carregar os logs de auditoria.');
      },
    });
  }

  paginarSeguranca(evento: PageEvent): void {
    this.tamanho.set(evento.pageSize);
    this.carregarSeguranca(evento.pageIndex);
  }

  paginarAuditoria(evento: PageEvent): void {
    this.tamanho.set(evento.pageSize);
    this.carregarAuditoria(evento.pageIndex);
  }

  limparSeguranca(): void {
    this.eventoFiltro.setValue('');
    this.ipFiltro.setValue('');
    this.dataSeguranca.setValue('');
  }

  limparAuditoria(): void {
    this.acaoFiltro.setValue('');
    this.usuarioAuditoria.setValue('');
    this.dataAuditoria.setValue('');
  }

  classeEvento(evento: EventoSeguranca): string {
    return corEvento(evento);
  }

  rotuloDoEvento(evento: EventoSeguranca): string {
    return ROTULO_EVENTO_SEGURANCA[evento];
  }
}
