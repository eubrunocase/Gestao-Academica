import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE } from '../http/api.config';
import { Turma } from '../models/academico.models';

@Injectable({ providedIn: 'root' })
export class ProfessorService {
  private readonly http = inject(HttpClient);

  minhasTurmas(): Observable<Turma[]> {
    return this.http.get<Turma[]>(`${API_BASE}/professor/turmas`);
  }
}
