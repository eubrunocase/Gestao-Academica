import { DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  Component,
  OnInit,
  inject,
  input,
  signal,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { AvaliacaoService } from '../../../core/services/avaliacao.service';
import {
  Avaliacao,
  AlunoNotas,
  NotaLancamento,
} from '../../../core/models/avaliacao.models';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state.component';

@Component({
  selector: 'app-lancar-notas',
  standalone: true,
  imports: [
    DecimalPipe,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    EmptyStateComponent,
  ],
  templateUrl: './lancar-notas.component.html',
  styleUrl: './lancar-notas.component.scss',
})
export class LancarNotasComponent implements OnInit {
  private readonly avaliacaoService = inject(AvaliacaoService);
  private readonly fb = inject(FormBuilder);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly turmaId = input.required<number>();
  readonly encerrada = input(false);

  readonly avaliacoes = signal<Avaliacao[]>([]);
  readonly alunos = signal<AlunoNotas[]>([]);
  readonly carregando = signal(true);
  readonly salvando = signal(false);
  readonly criando = signal(false);
  readonly erro = signal<string | null>(null);

  readonly formNotas = new FormGroup<Record<string, AbstractControl>>({});
  readonly formAvaliacao = this.fb.nonNullable.group({
    descricao: ['', [Validators.required, Validators.maxLength(120)]],
    peso: [1, [Validators.required, Validators.min(0.1)]],
  });

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    forkJoin({
      avaliacoes: this.avaliacaoService.listar(this.turmaId()),
      notas: this.avaliacaoService.listarNotas(this.turmaId()),
    }).subscribe({
      next: ({ avaliacoes, notas }) => {
        this.avaliacoes.set(avaliacoes);
        this.alunos.set(notas.alunos);
        this.montarFormulario(notas.alunos, avaliacoes);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar avaliações e notas.');
      },
    });
  }

  chaveNota(matriculaId: number, avaliacaoId: number): string {
    return `${matriculaId}-${avaliacaoId}`;
  }

  controle(matriculaId: number, avaliacaoId: number): AbstractControl | null {
    return this.formNotas.get(this.chaveNota(matriculaId, avaliacaoId));
  }

  mediaAluno(aluno: AlunoNotas): number | null {
    let soma = 0;
    let pesos = 0;
    for (const avaliacao of this.avaliacoes()) {
      const controle = this.controle(aluno.matriculaId, avaliacao.id);
      const valor = controle?.value;
      if (valor !== null && valor !== undefined && valor !== '') {
        soma += Number(valor) * avaliacao.peso;
        pesos += avaliacao.peso;
      }
    }
    return pesos > 0 ? Math.round((soma / pesos) * 100) / 100 : null;
  }

  salvar(): void {
    if (this.encerrada() || this.salvando()) {
      return;
    }
    this.formNotas.markAllAsTouched();
    if (this.formNotas.invalid) {
      this.snackBar.open(
        'Corrija as notas destacadas. O intervalo permitido é de 0 a 10.',
        'Fechar',
        { duration: 5000 },
      );
      return;
    }

    const notas: NotaLancamento[] = [];
    for (const [chave, controle] of Object.entries(this.formNotas.controls)) {
      const valor = controle.value;
      if (valor === null || valor === undefined) {
        continue;
      }
      const [matriculaId, avaliacaoId] = chave.split('-').map(Number);
      notas.push({ matriculaId, avaliacaoId, valor: Number(valor) });
    }

    if (notas.length === 0) {
      this.snackBar.open('Informe ao menos uma nota para salvar.', 'Fechar', {
        duration: 4000,
      });
      return;
    }

    this.salvando.set(true);
    this.avaliacaoService.lancarLote(this.turmaId(), { notas }).subscribe({
      next: () => {
        this.salvando.set(false);
        this.snackBar.open('Notas salvas com sucesso.', 'Fechar', {
          duration: 3500,
        });
        this.carregar();
      },
      error: (erro: unknown) => {
        this.salvando.set(false);
        this.mostrarErro(erro, 'Não foi possível salvar as notas.');
      },
    });
  }

  criarAvaliacao(): void {
    if (this.encerrada() || this.criando()) {
      return;
    }
    this.formAvaliacao.markAllAsTouched();
    if (this.formAvaliacao.invalid) {
      return;
    }
    this.criando.set(true);
    this.avaliacaoService
      .criar(this.turmaId(), this.formAvaliacao.getRawValue())
      .subscribe({
        next: () => {
          this.criando.set(false);
          this.formAvaliacao.reset({ descricao: '', peso: 1 });
          this.snackBar.open('Avaliação criada.', 'Fechar', { duration: 3500 });
          this.carregar();
        },
        error: (erro: unknown) => {
          this.criando.set(false);
          this.mostrarErro(erro, 'Não foi possível criar a avaliação.');
        },
      });
  }

  excluirAvaliacao(avaliacao: Avaliacao): void {
    const referencia = this.dialog.open<ConfirmDialogComponent, unknown, boolean>(
      ConfirmDialogComponent,
      {
        data: {
          titulo: 'Excluir avaliação',
          mensagem: `Excluir "${avaliacao.descricao}"? As notas vinculadas também serão removidas.`,
          confirmar: 'Excluir',
          destrutivo: true,
        },
        width: '440px',
      },
    );
    referencia.afterClosed().subscribe((confirmado) => {
      if (!confirmado) {
        return;
      }
      this.avaliacaoService
        .excluir(this.turmaId(), avaliacao.id)
        .subscribe({
          next: () => {
            this.snackBar.open('Avaliação excluída.', 'Fechar', {
              duration: 3500,
            });
            this.carregar();
          },
          error: (erro: unknown) =>
            this.mostrarErro(erro, 'Não foi possível excluir a avaliação.'),
        });
    });
  }

  private montarFormulario(alunos: AlunoNotas[], avaliacoes: Avaliacao[]): void {
    for (const chave of Object.keys(this.formNotas.controls)) {
      this.formNotas.removeControl(chave);
    }
    for (const aluno of alunos) {
      for (const avaliacao of avaliacoes) {
        const nota = aluno.notas.find(
          (item) => item.avaliacaoId === avaliacao.id,
        );
        const controle = new FormControl<number | null>(nota?.valor ?? null, [
          Validators.min(0),
          Validators.max(10),
        ]);
        if (this.encerrada()) {
          controle.disable();
        }
        this.formNotas.addControl(
          this.chaveNota(aluno.matriculaId, avaliacao.id),
          controle,
        );
      }
    }
  }

  private mostrarErro(erro: unknown, fallback: string): void {
    const mensagem =
      erro instanceof HttpErrorResponse &&
      erro.error &&
      typeof erro.error === 'object' &&
      'mensagem' in erro.error
        ? String((erro.error as { mensagem: unknown }).mensagem)
        : fallback;
    this.snackBar.open(mensagem, 'Fechar', { duration: 5000 });
  }
}
