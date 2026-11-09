import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE } from '../http/api.config';
import { Aviso, AvisoRequest } from '../models/comunicacao.models';

@Injectable({ providedIn: 'root' })
export class AvisoService {
  private readonly http = inject(HttpClient);

  listar(): Observable<Aviso[]> {
    return this.http.get<Aviso[]>(`${API_BASE}/avisos`);
  }

  criar(request: AvisoRequest): Observable<Aviso> {
    return this.http.post<Aviso>(`${API_BASE}/avisos`, request);
  }
}
