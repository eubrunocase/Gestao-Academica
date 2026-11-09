import { PerfilUsuario } from './auth.models';

export interface UsuarioResponse {
  id: number;
  nome: string;
  documento: string;
  email: string;
  perfil: PerfilUsuario;
  ativo: boolean;
  numeroMatricula?: string | null;
  registroFuncional?: string | null;
}

export interface UsuarioRequest {
  nome: string;
  documento: string;
  email: string;
  senha: string;
  perfil: PerfilUsuario;
  numeroMatricula?: string | null;
  registroFuncional?: string | null;
  ativo?: boolean;
}

export interface UsuarioUpdateRequest {
  nome: string;
  documento: string;
  email: string;
  senha?: string | null;
  ativo?: boolean;
  numeroMatricula?: string | null;
  registroFuncional?: string | null;
}

export interface UsuarioAtivoRequest {
  ativo: boolean;
}
