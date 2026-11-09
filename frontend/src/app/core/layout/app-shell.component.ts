import { BreakpointObserver } from '@angular/cdk/layout';
import { Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { PerfilUsuario } from '../models/auth.models';
import { ROTULO_PERFIL } from '../utils/rotulos';
import { ChatbotWidgetComponent } from './chatbot-widget.component';

interface ItemMenu {
  rotulo: string;
  icone: string;
  rota: string;
  exato?: boolean;
}

interface GrupoMenu {
  titulo: string;
  itens: ItemMenu[];
}

const MENUS: Record<PerfilUsuario, GrupoMenu[]> = {
  ALUNO: [
    {
      titulo: 'Meu portal',
      itens: [
        { rotulo: 'Início', icone: 'dashboard', rota: '/aluno', exato: true },
        { rotulo: 'Matrícula', icone: 'how_to_reg', rota: '/aluno/matricula' },
        { rotulo: 'Notas', icone: 'grade', rota: '/aluno/notas' },
        { rotulo: 'Frequência', icone: 'fact_check', rota: '/aluno/frequencia' },
        { rotulo: 'Histórico', icone: 'history_edu', rota: '/aluno/historico' },
      ],
    },
    {
      titulo: 'Comunicação',
      itens: [
        { rotulo: 'Avisos', icone: 'campaign', rota: '/avisos' },
        { rotulo: 'Nova denúncia', icone: 'report', rota: '/denuncias/nova' },
        { rotulo: 'Acompanhar denúncia', icone: 'travel_explore', rota: '/denuncias/acompanhar' },
      ],
    },
  ],
  PROFESSOR: [
    {
      titulo: 'Docência',
      itens: [
        { rotulo: 'Minhas turmas', icone: 'class', rota: '/professor/turmas' },
        { rotulo: 'Novo aviso', icone: 'add_alert', rota: '/avisos/novo' },
        { rotulo: 'Relatórios', icone: 'summarize', rota: '/relatorios' },
      ],
    },
    {
      titulo: 'Comunicação',
      itens: [
        { rotulo: 'Avisos', icone: 'campaign', rota: '/avisos' },
        { rotulo: 'Nova denúncia', icone: 'report', rota: '/denuncias/nova' },
        { rotulo: 'Acompanhar denúncia', icone: 'travel_explore', rota: '/denuncias/acompanhar' },
      ],
    },
  ],
  SECRETARIA: [
    {
      titulo: 'Gestão acadêmica',
      itens: [
        { rotulo: 'Usuários', icone: 'manage_accounts', rota: '/gestao/usuarios' },
        { rotulo: 'Cursos', icone: 'school', rota: '/gestao/cursos' },
        { rotulo: 'Disciplinas', icone: 'menu_book', rota: '/gestao/disciplinas' },
        { rotulo: 'Turmas', icone: 'groups', rota: '/gestao/turmas' },
        { rotulo: 'Relatórios', icone: 'summarize', rota: '/relatorios' },
      ],
    },
    {
      titulo: 'Comunicação',
      itens: [
        { rotulo: 'Avisos', icone: 'campaign', rota: '/avisos' },
        { rotulo: 'Novo aviso', icone: 'add_alert', rota: '/avisos/novo' },
        { rotulo: 'Nova denúncia', icone: 'report', rota: '/denuncias/nova' },
        { rotulo: 'Acompanhar denúncia', icone: 'travel_explore', rota: '/denuncias/acompanhar' },
      ],
    },
  ],
  COORDENADOR: [
    {
      titulo: 'Coordenação',
      itens: [
        { rotulo: 'Indicadores', icone: 'monitoring', rota: '/indicadores' },
        { rotulo: 'Cursos', icone: 'school', rota: '/gestao/cursos' },
        { rotulo: 'Disciplinas', icone: 'menu_book', rota: '/gestao/disciplinas' },
        { rotulo: 'Turmas', icone: 'groups', rota: '/gestao/turmas' },
        { rotulo: 'Relatórios', icone: 'summarize', rota: '/relatorios' },
      ],
    },
    {
      titulo: 'Denúncias',
      itens: [
        { rotulo: 'Tratamento', icone: 'gavel', rota: '/denuncias/tratamento' },
        { rotulo: 'Estatísticas', icone: 'analytics', rota: '/denuncias/estatisticas' },
        { rotulo: 'Nova denúncia', icone: 'report', rota: '/denuncias/nova' },
        { rotulo: 'Acompanhar', icone: 'travel_explore', rota: '/denuncias/acompanhar' },
      ],
    },
    {
      titulo: 'Comunicação',
      itens: [
        { rotulo: 'Avisos', icone: 'campaign', rota: '/avisos' },
        { rotulo: 'Novo aviso', icone: 'add_alert', rota: '/avisos/novo' },
      ],
    },
  ],
  ADMIN: [
    {
      titulo: 'Administração',
      itens: [
        { rotulo: 'Usuários', icone: 'manage_accounts', rota: '/gestao/usuarios' },
        { rotulo: 'Cursos', icone: 'school', rota: '/gestao/cursos' },
        { rotulo: 'Disciplinas', icone: 'menu_book', rota: '/gestao/disciplinas' },
        { rotulo: 'Turmas', icone: 'groups', rota: '/gestao/turmas' },
        { rotulo: 'Logs', icone: 'receipt_long', rota: '/admin/logs' },
      ],
    },
    {
      titulo: 'Denúncias',
      itens: [
        { rotulo: 'Tratamento', icone: 'gavel', rota: '/denuncias/tratamento' },
        { rotulo: 'Estatísticas', icone: 'analytics', rota: '/denuncias/estatisticas' },
        { rotulo: 'Nova denúncia', icone: 'report', rota: '/denuncias/nova' },
        { rotulo: 'Acompanhar', icone: 'travel_explore', rota: '/denuncias/acompanhar' },
      ],
    },
    {
      titulo: 'Comunicação',
      itens: [
        { rotulo: 'Avisos', icone: 'campaign', rota: '/avisos' },
        { rotulo: 'Novo aviso', icone: 'add_alert', rota: '/avisos/novo' },
      ],
    },
  ],
};

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatDividerModule,
    MatTooltipModule,
    ChatbotWidgetComponent,
  ],
  templateUrl: './app-shell.component.html',
  styleUrl: './app-shell.component.scss',
})
export class AppShellComponent {
  private readonly auth = inject(AuthService);
  private readonly breakpoint = inject(BreakpointObserver);

  readonly usuario = this.auth.usuario;
  readonly rotuloPerfil = computed(() => {
    const perfil = this.auth.perfil();
    return perfil ? ROTULO_PERFIL[perfil] : '';
  });
  readonly grupos = computed<GrupoMenu[]>(() => {
    const perfil = this.auth.perfil();
    return perfil ? MENUS[perfil] : [];
  });
  readonly isMobile = signal(false);
  readonly sidenavAberto = signal(false);

  constructor() {
    this.breakpoint.observe('(max-width: 900px)').subscribe((resultado) => {
      this.isMobile.set(resultado.matches);
      this.sidenavAberto.set(!resultado.matches);
    });
  }

  alternarSidenav(): void {
    this.sidenavAberto.update((aberto) => !aberto);
  }

  fecharNoMobile(): void {
    if (this.isMobile()) {
      this.sidenavAberto.set(false);
    }
  }

  sair(): void {
    this.auth.logout();
  }
}
