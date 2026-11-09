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
import { Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { AcademicoService } from '../../../core/services/academico.service';
import { AvisoService } from '../../../core/services/aviso.service';
import { ProfessorService } from '../../../core/services/professor.service';
import {
  Curso,
  Turma,
} from '../../../core/models/academico.models';
import {
  AvisoRequest,
  EscopoAviso,
} from '../../../core/models/comunicacao.models';
import { ApiError } from '../../../core/models/common.models';
import { ROTULO_ESCOPO } from '../../../core/utils/rotulos';
import { PageHeaderComponent } from '../../../shared/components/page-header.component';

@Component({
  selector: 'app-novo-aviso',
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
    PageHeaderComponent,
  ],
  templateUrl: './novo-aviso.component.html',
  styleUrl: './novo-aviso.component.scss',
})
export class NovoAvisoComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly avisoService = inject(AvisoService);
  private readonly academicoService = inject(AcademicoService);
  private readonly professorService = inject(ProfessorService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly rotuloEscopo = ROTULO_ESCOPO;
  readonly ehProfessor = computed(() => this.auth.perfil() === 'PROFESSOR');
  readonly escopos = computed<EscopoAviso[]>(() =>
    this.ehProfessor() ? ['TURMA'] : ['GERAL', 'CURSO', 'TURMA'],
  );
  readonly turmas = signal<Turma[]>([]);
  readonly cursos = signal<Curso[]>([]);
  readonly carregando = signal(true);
  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);

  readonly form = this.fb.group({
    titulo: ['', [Validators.required, Validators.maxLength(160)]],
    mensagem: ['', [Validators.required, Validators.maxLength(2000)]],
    escopo: [
      (this.auth.perfil() === 'PROFESSOR' ? 'TURMA' : 'GERAL') as EscopoAviso,
      [Validators.required],
    ],
    turmaId: [null as number | null],
    cursoId: [null as number | null],
  });

  ngOnInit(): void {
    const turmas$ = this.ehProfessor()
      ? this.professorService.minhasTurmas()
      : this.academicoService.listarTurmas();
    const cursos$ = this.ehProfessor()
      ? of([] as Curso[])
      : this.academicoService.listarCursos();

    forkJoin({ turmas: turmas$, cursos: cursos$ }).subscribe({
      next: ({ turmas, cursos }) => {
        this.turmas.set(turmas);
        this.cursos.set(cursos);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar turmas e cursos.');
      },
    });

    this.form.controls.escopo.valueChanges.subscribe(() =>
      this.atualizarValidadores(),
    );
    this.atualizarValidadores();
  }

  cancelar(): void {
    void this.router.navigate(['/avisos']);
  }

  criar(): void {
    this.erro.set(null);
    this.form.markAllAsTouched();
    if (this.form.invalid || this.salvando()) {
      return;
    }

    const valores = this.form.getRawValue();
    const request: AvisoRequest = {
      titulo: valores.titulo ?? '',
      mensagem: valores.mensagem ?? '',
      escopo: valores.escopo ?? 'GERAL',
      turmaId: valores.escopo === 'TURMA' ? valores.turmaId : null,
      cursoId: valores.escopo === 'CURSO' ? valores.cursoId : null,
    };

    this.salvando.set(true);
    this.avisoService.criar(request).subscribe({
      next: () => {
        this.salvando.set(false);
        this.snackBar.open('Aviso publicado com sucesso.', 'Fechar', {
          duration: 3500,
        });
        void this.router.navigate(['/avisos']);
      },
      error: (erro: unknown) => {
        this.salvando.set(false);
        const corpo =
          erro instanceof HttpErrorResponse ? (erro.error as ApiError | null) : null;
        this.erro.set(corpo?.mensagem ?? 'Não foi possível publicar o aviso.');
      },
    });
  }

  private atualizarValidadores(): void {
    const escopo = this.form.controls.escopo.value;
    const turma = this.form.controls.turmaId;
    const curso = this.form.controls.cursoId;

    if (escopo === 'TURMA') {
      turma.setValidators([Validators.required]);
      curso.clearValidators();
      curso.setValue(null);
    } else if (escopo === 'CURSO') {
      curso.setValidators([Validators.required]);
      turma.clearValidators();
      turma.setValue(null);
    } else {
      turma.clearValidators();
      curso.clearValidators();
      turma.setValue(null);
      curso.setValue(null);
    }
    turma.updateValueAndValidity();
    curso.updateValueAndValidity();
  }
}
