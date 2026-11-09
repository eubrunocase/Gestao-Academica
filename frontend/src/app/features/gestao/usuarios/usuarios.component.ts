import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { PerfilUsuario } from '../../../core/models/auth.models';
import { UsuarioResponse } from '../../../core/models/usuario.models';
import { UsuarioService } from '../../../core/services/usuario.service';
import { ROTULO_PERFIL } from '../../../core/utils/rotulos';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state.component';
import { PageHeaderComponent } from '../../../shared/components/page-header.component';
import {
  UsuarioFormDialogComponent,
  UsuarioFormDialogData,
} from './usuario-form-dialog.component';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatTableModule,
    PageHeaderComponent,
    EmptyStateComponent,
  ],
  templateUrl: './usuarios.component.html',
  styleUrl: './usuarios.component.scss',
})
export class UsuariosComponent implements OnInit {
  private readonly usuarioService = inject(UsuarioService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly busca = new FormControl('', { nonNullable: true });
  readonly perfilFiltro = new FormControl<PerfilUsuario | ''>('', {
    nonNullable: true,
  });
  readonly usuarios = signal<UsuarioResponse[]>([]);
  readonly total = signal(0);
  readonly pagina = signal(0);
  readonly tamanho = signal(10);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);

  readonly perfis: PerfilUsuario[] = [
    'ALUNO',
    'PROFESSOR',
    'SECRETARIA',
    'COORDENADOR',
    'ADMIN',
  ];
  readonly rotuloPerfil = ROTULO_PERFIL;
  readonly colunas = ['nome', 'documento', 'email', 'perfil', 'ativo', 'acoes'];

  ngOnInit(): void {
    this.carregar(0);
  }

  carregar(pagina: number): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.usuarioService
      .listar({
        page: pagina,
        size: this.tamanho(),
        nome: this.busca.value.trim() || undefined,
        perfil: this.perfilFiltro.value || undefined,
      })
      .subscribe({
        next: (resposta) => {
          this.usuarios.set(resposta.content);
          this.total.set(resposta.totalElements);
          this.pagina.set(resposta.page);
          this.carregando.set(false);
        },
        error: () => {
          this.carregando.set(false);
          this.erro.set('Não foi possível carregar os usuários.');
        },
      });
  }

  aplicarFiltros(): void {
    this.carregar(0);
  }

  limparFiltros(): void {
    this.busca.setValue('');
    this.perfilFiltro.setValue('');
    this.carregar(0);
  }

  aoPaginar(evento: PageEvent): void {
    this.tamanho.set(evento.pageSize);
    this.carregar(evento.pageIndex);
  }

  novo(): void {
    const referencia = this.dialog.open<
      UsuarioFormDialogComponent,
      UsuarioFormDialogData,
      boolean
    >(UsuarioFormDialogComponent, {
      width: '680px',
      maxWidth: '96vw',
      data: {},
    });
    referencia.afterClosed().subscribe((salvou) => {
      if (salvou) {
        this.snackBar.open('Usuário criado com sucesso.', 'Fechar', {
          duration: 3500,
        });
        this.carregar(0);
      }
    });
  }

  editar(usuario: UsuarioResponse): void {
    const referencia = this.dialog.open<
      UsuarioFormDialogComponent,
      UsuarioFormDialogData,
      boolean
    >(UsuarioFormDialogComponent, {
      width: '680px',
      maxWidth: '96vw',
      data: { usuario },
    });
    referencia.afterClosed().subscribe((salvou) => {
      if (salvou) {
        this.snackBar.open('Usuário atualizado.', 'Fechar', {
          duration: 3500,
        });
        this.carregar(this.pagina());
      }
    });
  }

  rotuloDoPerfil(perfil: PerfilUsuario): string {
    return ROTULO_PERFIL[perfil];
  }

  alterarAtivo(usuario: UsuarioResponse, ativo: boolean): void {
    if (!ativo) {
      const referencia = this.dialog.open<
        ConfirmDialogComponent,
        unknown,
        boolean
      >(ConfirmDialogComponent, {
        data: {
          titulo: 'Desativar usuário',
          mensagem: `Desativar ${usuario.nome}? O usuário não conseguirá autenticar no portal até ser reativado.`,
          confirmar: 'Desativar',
          destrutivo: true,
        },
        width: '440px',
      });
      referencia.afterClosed().subscribe((confirmado) => {
        if (!confirmado) {
          this.carregar(this.pagina());
          return;
        }
        this.executarAlteracaoAtivo(usuario, false);
      });
      return;
    }
    this.executarAlteracaoAtivo(usuario, true);
  }

  private executarAlteracaoAtivo(
    usuario: UsuarioResponse,
    ativo: boolean,
  ): void {
    this.usuarioService.alterarAtivo(usuario.id, ativo).subscribe({
      next: (atualizado) => {
        this.usuarios.update((lista) =>
          lista.map((item) => (item.id === atualizado.id ? atualizado : item)),
        );
        this.snackBar.open(
          ativo ? 'Usuário ativado.' : 'Usuário desativado.',
          'Fechar',
          { duration: 3500 },
        );
      },
      error: () => this.carregar(this.pagina()),
    });
  }
}
