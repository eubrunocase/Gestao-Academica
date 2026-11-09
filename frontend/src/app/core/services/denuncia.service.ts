import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE } from '../http/api.config';
import {
  AndamentoRequest,
  Denuncia,
  DenunciaCriada,
  DenunciaRequest,
  EstatisticasDenuncia,
} from '../models/denuncia.models';

@Injectable({ providedIn: 'root' })
export class DenunciaService {
  private readonly http = inject(HttpClient);

  criar(request: DenunciaRequest): Observable<DenunciaCriada> {
    return this.http.post<DenunciaCriada>(`${API_BASE}/denuncias`, request);
  }

  consultarPorProtocolo(protocolo: string): Observable<Denuncia> {
    return this.http.get<Denuncia>(
      `${API_BASE}/denuncias/protocolo/${encodeURIComponent(protocolo)}`,
    );
  }

  listar(): Observable<Denuncia[]> {
    return this.http.get<Denuncia[]>(`${API_BASE}/denuncias`);
  }

  adicionarAndamento(
    id: number,
    request: AndamentoRequest,
  ): Observable<Denuncia> {
    return this.http.post<Denuncia>(
      `${API_BASE}/denuncias/${id}/andamentos`,
      request,
    );
  }

  estatisticas(): Observable<EstatisticasDenuncia> {
    return this.http.get<EstatisticasDenuncia>(
      `${API_BASE}/denuncias/estatisticas`,
    );
  }
}
