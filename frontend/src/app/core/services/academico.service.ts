import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE } from '../http/api.config';
import {
  AlunoTurma,
  CalendarioAcademico,
  Curso,
  CursoRequest,
  Disciplina,
  DisciplinaRequest,
  Turma,
  TurmaRequest,
} from '../models/academico.models';

@Injectable({ providedIn: 'root' })
export class AcademicoService {
  private readonly http = inject(HttpClient);
  private readonly base = `${API_BASE}`;

  listarCursos(): Observable<Curso[]> {
    return this.http.get<Curso[]>(`${this.base}/cursos`);
  }

  criarCurso(request: CursoRequest): Observable<Curso> {
    return this.http.post<Curso>(`${this.base}/cursos`, request);
  }

  atualizarCurso(id: number, request: CursoRequest): Observable<Curso> {
    return this.http.put<Curso>(`${this.base}/cursos/${id}`, request);
  }

  excluirCurso(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/cursos/${id}`);
  }

  listarDisciplinas(cursoId?: number): Observable<Disciplina[]> {
    let params = new HttpParams();
    if (cursoId) {
      params = params.set('cursoId', cursoId);
    }
    return this.http.get<Disciplina[]>(`${this.base}/disciplinas`, { params });
  }

  criarDisciplina(request: DisciplinaRequest): Observable<Disciplina> {
    return this.http.post<Disciplina>(`${this.base}/disciplinas`, request);
  }

  atualizarDisciplina(id: number, request: DisciplinaRequest): Observable<Disciplina> {
    return this.http.put<Disciplina>(`${this.base}/disciplinas/${id}`, request);
  }

  excluirDisciplina(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/disciplinas/${id}`);
  }

  listarCalendarios(): Observable<CalendarioAcademico[]> {
    return this.http.get<CalendarioAcademico[]>(`${this.base}/calendarios`);
  }

  listarTurmas(periodoLetivo?: string, professorId?: number): Observable<Turma[]> {
    let params = new HttpParams();
    if (periodoLetivo) {
      params = params.set('periodoLetivo', periodoLetivo);
    }
    if (professorId) {
      params = params.set('professorId', professorId);
    }
    return this.http.get<Turma[]>(`${this.base}/turmas`, { params });
  }

  buscarTurma(id: number): Observable<Turma> {
    return this.http.get<Turma>(`${this.base}/turmas/${id}`);
  }

  criarTurma(request: TurmaRequest): Observable<Turma> {
    return this.http.post<Turma>(`${this.base}/turmas`, request);
  }

  atualizarTurma(id: number, request: TurmaRequest): Observable<Turma> {
    return this.http.put<Turma>(`${this.base}/turmas/${id}`, request);
  }

  encerrarTurma(id: number): Observable<Turma> {
    return this.http.patch<Turma>(`${this.base}/turmas/${id}/encerrar`, {});
  }

  excluirTurma(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/turmas/${id}`);
  }

  listarAlunosTurma(id: number): Observable<AlunoTurma[]> {
    return this.http.get<AlunoTurma[]>(`${this.base}/turmas/${id}/alunos`);
  }
}
