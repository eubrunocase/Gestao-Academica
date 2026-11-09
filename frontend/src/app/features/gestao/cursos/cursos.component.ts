import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { AcademicoService } from '../../../core/services/academico.service';
import { Curso } from '../../../core/models/academico.models';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state.component';
import { PageHeaderComponent } from '../../../shared/components/page-header.component';
import {
  CursoFormDialogComponent,
  CursoFormDialogData,
} from './curso-form-dialog.component';

@Component({
  selector: 'app-cursos',
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
    MatTableModule,
    PageHeaderComponent,
    EmptyStateComponent,
  ],
  templateUrl: './cursos.component.html',
  styleUrl: './cursos.component.scss',
})
export class CursosComponent implements OnInit {
  private readonly academicoService = inject(AcademicoService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly busca = new FormControl('', { nonNullable: true });
  readonly buscaTermo = toSignal(this.busca.valueChanges, {
    initialValue: this.busca.value,
  });
  readonly cursos = signal<Curso[]>([]);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly pagina = signal(0);
  readonly tamanho = signal(10);
  readonly colunas = ['nome', 'acoes'];

  readonly filtrados = computed(() => {
    const termo = this.buscaTermo().trim().toLowerCase();
    return termo
      ? this.cursos().filter((curso) => curso.nome.toLowerCase().includes(termo))
      : this.cursos();
  });
  readonly paginados = computed(() =>
    this.filtrados().slice(
      this.pagina() * this.tamanho(),
      (this.pagina() + 1) * this.tamanho(),
    ),
  );

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.academicoService.listarCursos().subscribe({
      next: (cursos) => {
        this.cursos.set(cursos);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar os cursos.');
      },
    });
  }

  buscar(): void {
    this.pagina.set(0);
  }

  aoPaginar(evento: PageEvent): void {
    this.pagina.set(evento.pageIndex);
    this.tamanho.set(evento.pageSize);
  }

  novo(): void {
    const referencia = this.dialog.open<
      CursoFormDialogComponent,
      CursoFormDialogData,
      boolean
    >(CursoFormDialogComponent, { width: '480px', data: {} });
    referencia.afterClosed().subscribe((salvou) => {
      if (salvou) {
        this.snackBar.open('Curso criado.', 'Fechar', { duration: 3000 });
        this.carregar();
      }
    });
  }

  editar(curso: Curso): void {
    const referencia = this.dialog.open<
      CursoFormDialogComponent,
      CursoFormDialogData,
      boolean
    >(CursoFormDialogComponent, { width: '480px', data: { curso } });
    referencia.afterClosed().subscribe((salvou) => {
      if (salvou) {
        this.snackBar.open('Curso atualizado.', 'Fechar', { duration: 3000 });
        this.carregar();
      }
    });
  }

  excluir(curso: Curso): void {
    const referencia = this.dialog.open<
      ConfirmDialogComponent,
      unknown,
      boolean
    >(ConfirmDialogComponent, {
      data: {
        titulo: 'Excluir curso',
        mensagem: `Excluir o curso "${curso.nome}"? Disciplinas vinculadas podem impedir a exclusão.`,
        confirmar: 'Excluir',
        destrutivo: true,
      },
      width: '440px',
    });
    referencia.afterClosed().subscribe((confirmado) => {
      if (!confirmado) {
        return;
      }
      this.academicoService.excluirCurso(curso.id).subscribe({
        next: () => {
          this.snackBar.open('Curso excluído.', 'Fechar', { duration: 3000 });
          this.carregar();
        },
      });
    });
  }
}
