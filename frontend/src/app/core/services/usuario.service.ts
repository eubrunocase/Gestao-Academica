import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE } from '../http/api.config';
import { PerfilUsuario } from '../models/auth.models';
import { PageResponse } from '../models/common.models';
import {
  UsuarioAtivoRequest,
  UsuarioRequest,
  UsuarioResponse,
  UsuarioUpdateRequest,
} from '../models/usuario.models';

export interface FiltroUsuarios {
  page?: number;
  size?: number;
  perfil?: PerfilUsuario | '';
  nome?: string;
}

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly http = inject(HttpClient);

  listar(filtro: FiltroUsuarios = {}): Observable<PageResponse<UsuarioResponse>> {
    let params = new HttpParams()
      .set('page', String(filtro.page ?? 0))
      .set('size', String(filtro.size ?? 20));
    if (filtro.perfil) {
      params = params.set('perfil', filtro.perfil);
    }
    if (filtro.nome) {
      params = params.set('nome', filtro.nome);
    }
    return this.http.get<PageResponse<UsuarioResponse>>(`${API_BASE}/usuarios`, {
      params,
    });
  }

  buscar(id: number): Observable<UsuarioResponse> {
    return this.http.get<UsuarioResponse>(`${API_BASE}/usuarios/${id}`);
  }

  criar(request: UsuarioRequest): Observable<UsuarioResponse> {
    return this.http.post<UsuarioResponse>(`${API_BASE}/usuarios`, request);
  }

  atualizar(
    id: number,
    request: UsuarioUpdateRequest,
  ): Observable<UsuarioResponse> {
    return this.http.put<UsuarioResponse>(
      `${API_BASE}/usuarios/${id}`,
      request,
    );
  }

  alterarAtivo(id: number, ativo: boolean): Observable<UsuarioResponse> {
    const request: UsuarioAtivoRequest = { ativo };
    return this.http.patch<UsuarioResponse>(
      `${API_BASE}/usuarios/${id}/ativo`,
      request,
    );
  }
}
