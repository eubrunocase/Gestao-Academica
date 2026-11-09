export type PerfilUsuario =
  | 'ALUNO'
  | 'PROFESSOR'
  | 'SECRETARIA'
  | 'COORDENADOR'
  | 'ADMIN';

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface UsuarioResumo {
  id: number;
  nome: string;
  email: string;
  perfil: PerfilUsuario;
}

export interface LoginResponse {
  token: string;
  tipo: string;
  expiraEmSegundos: number;
  usuario: UsuarioResumo;
}

export interface Usuario {
  id: number;
  nome: string;
  documento: string;
  email: string;
  perfil: PerfilUsuario;
  ativo: boolean;
  numeroMatricula?: string | null;
  registroFuncional?: string | null;
}

export type MeResponse = Usuario;
