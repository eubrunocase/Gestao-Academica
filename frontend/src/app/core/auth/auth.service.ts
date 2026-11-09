import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { API_BASE } from '../http/api.config';
import {
  LoginRequest,
  LoginResponse,
  MeResponse,
  Usuario,
} from '../models/auth.models';
import { SessionService } from './session.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly session = inject(SessionService);

  readonly usuario = this.session.usuario;
  readonly perfil = this.session.perfil;
  readonly estaLogado = this.session.estaLogado;

  login(credenciais: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${API_BASE}/auth/login`, credenciais)
      .pipe(
        tap((resposta) => {
          const usuario: Usuario = {
            id: resposta.usuario.id,
            nome: resposta.usuario.nome,
            email: resposta.usuario.email,
            perfil: resposta.usuario.perfil,
            documento: '',
            ativo: true,
          };
          this.session.setSession(resposta.token, usuario);
        }),
      );
  }

  carregarUsuario(): Observable<MeResponse> {
    return this.http
      .get<MeResponse>(`${API_BASE}/auth/me`)
      .pipe(tap((usuario) => this.session.setUsuario(usuario)));
  }

  logout(redirecionar = true): void {
    this.session.clear();
    if (redirecionar) {
      void this.router.navigate(['/login']);
    }
  }

  home(): string {
    return this.session.home();
  }
}
