import { HttpErrorResponse } from '@angular/common/http';
import {
  Component,
  OnInit,
  computed,
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
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { FrequenciaService } from '../../../core/services/frequencia.service';
import {
  Aula,
  AlunoFrequenciaTurma,
  FrequenciaLancamento,
  TurmaFrequencia,
} from '../../../core/models/frequencia.models';
import { EmptyStateComponent } from '../../../shared/components/empty-state.component';
import { FrequenciaBadgeComponent } from '../../../shared/components/frequencia-badge.component';
import { DataBrPipe } from '../../../shared/pipes/data-br.pipe';

@Component({
  selector: 'app-registrar-frequencia',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatSelectModule,
    EmptyStateComponent,
    FrequenciaBadgeComponent,
    DataBrPipe,
  ],
  templateUrl: './registrar-frequencia.component.html',
  styleUrl: './registrar-frequencia.component.scss',
})
export class RegistrarFrequenciaComponent implements OnInit {
  private readonly frequenciaService = inject(FrequenciaService);
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);

  readonly turmaId = input.required<number>();

  readonly aulas = signal<Aula[]>([]);
  readonly frequencia = signal<TurmaFrequencia | null>(null);
  readonly aulaSelecionadaId = signal<number | null>(null);
  readonly carregando = signal(true);
  readonly salvando = signal(false);
  readonly criando = signal(false);
  readonly erro = signal<string | null>(null);

  readonly formPresencas = new FormGroup<Record<string, AbstractControl>>({});
  readonly formAula = this.fb.nonNullable.group({
    data: ['', [Validators.required]],
    conteudo: [''],
  });

  readonly aulaSelecionada = computed(
    () => this.aulas().find((aula) => aula.id === this.aulaSelecionadaId()) ?? null,
  );

  ngOnInit(): void {
    this.carregar();
  }

  carregar(selecionarId?: number): void {
    this.carregando.set(true);
    this.erro.set(null);
    forkJoin({
      aulas: this.frequenciaService.listarAulas(this.turmaId()),
      frequencia: this.frequenciaService.listarFrequenciaTurma(this.turmaId()),
    }).subscribe({
      next: ({ aulas, frequencia }) => {
        this.aulas.set(aulas);
        this.frequencia.set(frequencia);
        const id =
          selecionarId ??
          this.aulaSelecionadaId() ??
          aulas[aulas.length - 1]?.id ??
          null;
        this.aulaSelecionadaId.set(aulas.some((aula) => aula.id === id) ? id : aulas[0]?.id ?? null);
        this.inicializarPresencas();
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar aulas e frequência.');
      },
    });
  }

  selecionarAula(aulaId: number): void {
    this.aulaSelecionadaId.set(aulaId);
    this.inicializarPresencas();
  }

  totalPresentes(): number {
    return Object.values(this.formPresencas.controls).filter(
      (controle) => controle.value === true,
    ).length;
  }

  totalAlunos(): number {
    return this.frequencia()?.alunos.length ?? 0;
  }

  marcarTodos(presente: boolean): void {
    for (const controle of Object.values(this.formPresencas.controls)) {
      controle.setValue(presente);
    }
  }

  salvar(): void {
    const aulaId = this.aulaSelecionadaId();
    if (!aulaId || this.salvando()) {
      return;
    }

    const registros: FrequenciaLancamento[] = this.frequencia()?.alunos.map(
      (aluno) => ({
        matriculaId: aluno.matriculaId,
        presente: this.formPresencas.get(String(aluno.matriculaId))?.value === true,
      }),
    ) ?? [];

    if (registros.length === 0) {
      this.snackBar.open('Não há alunos para registrar frequência.', 'Fechar', {
        duration: 4000,
      });
      return;
    }

    this.salvando.set(true);
    this.frequenciaService.registrar(aulaId, { registros }).subscribe({
      next: () => {
        this.salvando.set(false);
        this.snackBar.open('Frequência registrada com sucesso.', 'Fechar', {
          duration: 3500,
        });
        this.carregar(aulaId);
      },
      error: (erro: unknown) => {
        this.salvando.set(false);
        this.mostrarErro(erro, 'Não foi possível registrar a frequência.');
      },
    });
  }

  criarAula(): void {
    if (this.criando()) {
      return;
    }
    this.formAula.markAllAsTouched();
    if (this.formAula.invalid) {
      return;
    }
    this.criando.set(true);
    const valores = this.formAula.getRawValue();
    this.frequenciaService
      .criarAula(this.turmaId(), {
        data: valores.data,
        conteudo: valores.conteudo || null,
      })
      .subscribe({
        next: (aula) => {
          this.criando.set(false);
          this.formAula.reset({ data: '', conteudo: '' });
          this.snackBar.open('Aula criada.', 'Fechar', { duration: 3500 });
          this.carregar(aula.id);
        },
        error: (erro: unknown) => {
          this.criando.set(false);
          this.mostrarErro(erro, 'Não foi possível criar a aula.');
        },
      });
  }

  registrosDaAula(aluno: AlunoFrequenciaTurma): number {
    return aluno.registros.filter(
      (registro) => registro.aulaId === this.aulaSelecionadaId(),
    ).length;
  }

  private inicializarPresencas(): void {
    for (const chave of Object.keys(this.formPresencas.controls)) {
      this.formPresencas.removeControl(chave);
    }
    const aulaId = this.aulaSelecionadaId();
    for (const aluno of this.frequencia()?.alunos ?? []) {
      const registro = aluno.registros.find(
        (item) => item.aulaId === aulaId,
      );
      this.formPresencas.addControl(
        String(aluno.matriculaId),
        new FormControl<boolean>(registro?.presente ?? false, {
          nonNullable: true,
        }),
      );
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
