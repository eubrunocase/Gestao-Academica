import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE } from '../http/api.config';
import {
  Matricula,
  MatriculaRequest,
  TurmaDisponivel,
} from '../models/academico.models';
import { BoletimAluno } from '../models/avaliacao.models';
import { AlunoFrequencia } from '../models/frequencia.models';
import { AlunoDashboard, HistoricoAluno } from '../models/relatorio.models';

@Injectable({ providedIn: 'root' })
export class AlunoService {
  private readonly http = inject(HttpClient);

  turmasDisponiveis(): Observable<TurmaDisponivel[]> {
    return this.http.get<TurmaDisponivel[]>(`${API_BASE}/aluno/turmas-disponiveis`);
  }

  minhasMatriculas(): Observable<Matricula[]> {
    return this.http.get<Matricula[]>(`${API_BASE}/aluno/matriculas`);
  }

  matricular(request: MatriculaRequest): Observable<Matricula> {
    return this.http.post<Matricula>(`${API_BASE}/matriculas`, request);
  }

  cancelarMatricula(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/matriculas/${id}`);
  }

  dashboard(): Observable<AlunoDashboard> {
    return this.http.get<AlunoDashboard>(`${API_BASE}/aluno/dashboard`);
  }

  notas(): Observable<BoletimAluno[]> {
    return this.http.get<BoletimAluno[]>(`${API_BASE}/aluno/notas`);
  }

  frequencia(): Observable<AlunoFrequencia[]> {
    return this.http.get<AlunoFrequencia[]>(`${API_BASE}/aluno/frequencia`);
  }

  historico(): Observable<HistoricoAluno> {
    return this.http.get<HistoricoAluno>(`${API_BASE}/aluno/historico`);
  }
}
