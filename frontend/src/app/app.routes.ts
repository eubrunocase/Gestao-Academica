import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { roleGuard } from './core/auth/role.guard';
import { AppShellComponent } from './core/layout/app-shell.component';
import { HomeRedirectComponent } from './core/layout/home-redirect.component';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then(
        (m) => m.LoginComponent,
      ),
  },
  {
    path: '',
    component: AppShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', component: HomeRedirectComponent },
      {
        path: 'aluno',
        pathMatch: 'full',
        canActivate: [roleGuard('ALUNO')],
        loadComponent: () =>
          import('./features/aluno/dashboard/aluno-dashboard.component').then(
            (m) => m.AlunoDashboardComponent,
          ),
      },
      {
        path: 'aluno/matricula',
        pathMatch: 'full',
        canActivate: [roleGuard('ALUNO')],
        loadComponent: () =>
          import('./features/aluno/matricula/matricula.component').then(
            (m) => m.MatriculaComponent,
          ),
      },
      {
        path: 'aluno/notas',
        pathMatch: 'full',
        canActivate: [roleGuard('ALUNO')],
        loadComponent: () =>
          import('./features/aluno/notas/notas.component').then(
            (m) => m.NotasComponent,
          ),
      },
      {
        path: 'aluno/frequencia',
        pathMatch: 'full',
        canActivate: [roleGuard('ALUNO')],
        loadComponent: () =>
          import('./features/aluno/frequencia/frequencia.component').then(
            (m) => m.FrequenciaComponent,
          ),
      },
      {
        path: 'aluno/historico',
        pathMatch: 'full',
        canActivate: [roleGuard('ALUNO')],
        loadComponent: () =>
          import('./features/aluno/historico/historico.component').then(
            (m) => m.HistoricoComponent,
          ),
      },
      {
        path: 'professor/turmas',
        pathMatch: 'full',
        canActivate: [roleGuard('PROFESSOR')],
        loadComponent: () =>
          import('./features/professor/turmas/professor-turmas.component').then(
            (m) => m.ProfessorTurmasComponent,
          ),
      },
      {
        path: 'professor/turmas/:id',
        pathMatch: 'full',
        canActivate: [roleGuard('PROFESSOR')],
        loadComponent: () =>
          import('./features/professor/turma-detalhe/turma-detalhe.component').then(
            (m) => m.TurmaDetalheComponent,
          ),
      },
      {
        path: 'gestao/usuarios',
        pathMatch: 'full',
        canActivate: [roleGuard('ADMIN', 'SECRETARIA')],
        loadComponent: () =>
          import('./features/gestao/usuarios/usuarios.component').then(
            (m) => m.UsuariosComponent,
          ),
      },
      {
        path: 'gestao/cursos',
        pathMatch: 'full',
        canActivate: [roleGuard('SECRETARIA', 'COORDENADOR', 'ADMIN')],
        loadComponent: () =>
          import('./features/gestao/cursos/cursos.component').then(
            (m) => m.CursosComponent,
          ),
      },
      {
        path: 'gestao/disciplinas',
        pathMatch: 'full',
        canActivate: [roleGuard('SECRETARIA', 'COORDENADOR', 'ADMIN')],
        loadComponent: () =>
          import('./features/gestao/disciplinas/disciplinas.component').then(
            (m) => m.DisciplinasComponent,
          ),
      },
      {
        path: 'gestao/turmas',
        pathMatch: 'full',
        canActivate: [roleGuard('SECRETARIA', 'COORDENADOR', 'ADMIN')],
        loadComponent: () =>
          import('./features/gestao/turmas/turmas.component').then(
            (m) => m.TurmasComponent,
          ),
      },
      {
        path: 'avisos',
        pathMatch: 'full',
        loadComponent: () =>
          import('./features/avisos/lista/avisos-lista.component').then(
            (m) => m.AvisosListaComponent,
          ),
      },
      {
        path: 'avisos/novo',
        pathMatch: 'full',
        canActivate: [
          roleGuard('PROFESSOR', 'COORDENADOR', 'SECRETARIA', 'ADMIN'),
        ],
        loadComponent: () =>
          import('./features/avisos/novo-aviso/novo-aviso.component').then(
            (m) => m.NovoAvisoComponent,
          ),
      },
      {
        path: 'relatorios',
        pathMatch: 'full',
        canActivate: [roleGuard('PROFESSOR', 'COORDENADOR', 'SECRETARIA')],
        loadComponent: () =>
          import('./features/relatorios/relatorios/relatorios.component').then(
            (m) => m.RelatoriosComponent,
          ),
      },
      {
        path: 'indicadores',
        pathMatch: 'full',
        canActivate: [roleGuard('COORDENADOR')],
        loadComponent: () =>
          import('./features/relatorios/indicadores/indicadores.component').then(
            (m) => m.IndicadoresComponent,
          ),
      },
      {
        path: 'denuncias/nova',
        pathMatch: 'full',
        loadComponent: () =>
          import('./features/denuncias/nova-denuncia/nova-denuncia.component').then(
            (m) => m.NovaDenunciaComponent,
          ),
      },
      {
        path: 'denuncias/acompanhar',
        pathMatch: 'full',
        loadComponent: () =>
          import('./features/denuncias/acompanhar/acompanhar.component').then(
            (m) => m.AcompanharComponent,
          ),
      },
      {
        path: 'denuncias/tratamento',
        pathMatch: 'full',
        canActivate: [roleGuard('COORDENADOR', 'ADMIN')],
        loadComponent: () =>
          import('./features/denuncias/tratamento/tratamento.component').then(
            (m) => m.TratamentoComponent,
          ),
      },
      {
        path: 'denuncias/estatisticas',
        pathMatch: 'full',
        canActivate: [roleGuard('COORDENADOR', 'ADMIN')],
        loadComponent: () =>
          import('./features/denuncias/estatisticas/estatisticas.component').then(
            (m) => m.EstatisticasComponent,
          ),
      },
      {
        path: 'admin/logs',
        pathMatch: 'full',
        canActivate: [roleGuard('ADMIN')],
        loadComponent: () =>
          import('./features/admin/logs/logs.component').then(
            (m) => m.LogsComponent,
          ),
      },
    ],
  },
  { path: '**', component: HomeRedirectComponent },
];
