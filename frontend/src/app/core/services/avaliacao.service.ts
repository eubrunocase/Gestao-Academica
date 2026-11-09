import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE } from '../http/api.config';
import {
  Avaliacao,
  AvaliacaoRequest,
  Nota,
  NotasLoteRequest,
  TurmaNotas,
} from '../models/avaliacao.models';

@Injectable({ providedIn: 'root' })
export class AvaliacaoService {
  private readonly http = inject(HttpClient);

  listar(turmaId: number): Observable<Avaliacao[]> {
    return this.http.get<Avaliacao[]>(
      `${API_BASE}/turmas/${turmaId}/avaliacoes`,
    );
  }

  criar(turmaId: number, request: AvaliacaoRequest): Observable<Avaliacao> {
    return this.http.post<Avaliacao>(
      `${API_BASE}/turmas/${turmaId}/avaliacoes`,
      request,
    );
  }

  excluir(turmaId: number, avaliacaoId: number): Observable<void> {
    return this.http.delete<void>(
      `${API_BASE}/turmas/${turmaId}/avaliacoes/${avaliacaoId}`,
    );
  }

  listarNotas(turmaId: number): Observable<TurmaNotas> {
    return this.http.get<TurmaNotas>(`${API_BASE}/turmas/${turmaId}/notas`);
  }

  lancarLote(turmaId: number, request: NotasLoteRequest): Observable<Nota[]> {
    return this.http.put<Nota[]>(
      `${API_BASE}/turmas/${turmaId}/notas`,
      request,
    );
  }
}
