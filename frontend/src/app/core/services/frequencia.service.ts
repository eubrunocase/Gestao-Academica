import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE } from '../http/api.config';
import {
  Aula,
  AulaRequest,
  FrequenciaLoteRequest,
  RegistroFrequencia,
  TurmaFrequencia,
} from '../models/frequencia.models';

@Injectable({ providedIn: 'root' })
export class FrequenciaService {
  private readonly http = inject(HttpClient);

  listarAulas(turmaId: number): Observable<Aula[]> {
    return this.http.get<Aula[]>(`${API_BASE}/turmas/${turmaId}/aulas`);
  }

  criarAula(turmaId: number, request: AulaRequest): Observable<Aula> {
    return this.http.post<Aula>(
      `${API_BASE}/turmas/${turmaId}/aulas`,
      request,
    );
  }

  listarFrequenciaTurma(turmaId: number): Observable<TurmaFrequencia> {
    return this.http.get<TurmaFrequencia>(
      `${API_BASE}/turmas/${turmaId}/frequencia`,
    );
  }

  registrar(
    aulaId: number,
    request: FrequenciaLoteRequest,
  ): Observable<RegistroFrequencia[]> {
    return this.http.put<RegistroFrequencia[]>(
      `${API_BASE}/aulas/${aulaId}/frequencia`,
      request,
    );
  }
}
