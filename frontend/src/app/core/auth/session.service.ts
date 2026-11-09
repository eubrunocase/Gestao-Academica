import { computed, Injectable, signal } from '@angular/core';
import { PerfilUsuario, Usuario } from '../models/auth.models';

const TOKEN_KEY = 'ga.token';
const USUARIO_KEY = 'ga.usuario';

export function homePorPerfil(perfil: PerfilUsuario | null | undefined): string {
  switch (perfil) {
    case 'ALUNO':
      return '/aluno';
    case 'PROFESSOR':
      return '/professor/turmas';
    case 'SECRETARIA':
    case 'ADMIN':
      return '/gestao/usuarios';
    case 'COORDENADOR':
      return '/indicadores';
    default:
      return '/login';
  }
}

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly tokenState = signal<string | null>(this.lerTokenValido());
  private readonly usuarioState = signal<Usuario | null>(this.lerUsuario());

  readonly token = this.tokenState.asReadonly();
  readonly usuario = this.usuarioState.asReadonly();
  readonly perfil = computed<PerfilUsuario | null>(() => this.usuarioState()?.perfil ?? null);
  readonly estaLogado = computed(() => !!this.tokenState());

  setSession(token: string, usuario: Usuario): void {
    this.tokenState.set(token);
    this.usuarioState.set(usuario);
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USUARIO_KEY, JSON.stringify(usuario));
  }

  setUsuario(usuario: Usuario): void {
    this.usuarioState.set(usuario);
    localStorage.setItem(USUARIO_KEY, JSON.stringify(usuario));
  }

  clear(): void {
    this.tokenState.set(null);
    this.usuarioState.set(null);
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USUARIO_KEY);
  }

  home(): string {
    return homePorPerfil(this.perfil());
  }

  private lerTokenValido(): string | null {
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token) {
      return null;
    }
    if (!this.tokenNaoExpirado(token)) {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USUARIO_KEY);
      return null;
    }
    return token;
  }

  private lerUsuario(): Usuario | null {
    const bruto = localStorage.getItem(USUARIO_KEY);
    if (!bruto) {
      return null;
    }
    try {
      return JSON.parse(bruto) as Usuario;
    } catch {
      localStorage.removeItem(USUARIO_KEY);
      return null;
    }
  }

  private tokenNaoExpirado(token: string): boolean {
    try {
      const payload = token.split('.')[1];
      if (!payload) {
        return true;
      }
      const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
      const json = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join(''),
      );
      const dados = JSON.parse(json) as { exp?: number };
      return !dados.exp || dados.exp * 1000 > Date.now();
    } catch {
      return true;
    }
  }
}
