import { PerfilUsuario } from '../models/auth.models';
import { StatusMatricula } from '../models/academico.models';
import { EscopoAviso } from '../models/comunicacao.models';
import { StatusDenuncia } from '../models/denuncia.models';
import { EventoSeguranca } from '../models/auditoria.models';

export const ROTULO_PERFIL: Record<PerfilUsuario, string> = {
  ALUNO: 'Aluno',
  PROFESSOR: 'Professor',
  SECRETARIA: 'Secretaria',
  COORDENADOR: 'Coordenação',
  ADMIN: 'Administração',
};

export const ROTULO_STATUS_MATRICULA: Record<StatusMatricula, string> = {
  ATIVA: 'Ativa',
  CANCELADA: 'Cancelada',
  APROVADO: 'Aprovado',
  REPROVADO_NOTA: 'Reprovado por nota',
  REPROVADO_FALTA: 'Reprovado por falta',
};

export const ROTULO_ESCOPO: Record<EscopoAviso, string> = {
  GERAL: 'Geral',
  CURSO: 'Curso',
  TURMA: 'Turma',
};

export const ROTULO_STATUS_DENUNCIA: Record<StatusDenuncia, string> = {
  RECEBIDA: 'Recebida',
  EM_TRIAGEM: 'Em triagem',
  ENCAMINHADA: 'Encaminhada',
  EM_APURACAO: 'Em apuração',
  RESPONDIDA: 'Respondida',
  ENCERRADA: 'Encerrada',
};

export const ROTULO_EVENTO_SEGURANCA: Record<EventoSeguranca, string> = {
  LOGIN_SUCESSO: 'Login realizado',
  LOGIN_FALHA: 'Falha de login',
  ACESSO_NEGADO: 'Acesso negado',
  BLOQUEIO: 'Conta bloqueada',
  NAO_AUTENTICADO: 'Não autenticado',
};

export const CATEGORIAS_DENUNCIA = [
  'ASSEDIO',
  'DISCRIMINACAO',
  'INFRAESTRUTURA',
  'SEGURANCA',
  'CONDUTA',
  'OUTRO',
] as const;

export const ETAPAS_DENUNCIA = [
  'RECEBIDA',
  'EM_TRIAGEM',
  'ENCAMINHADA',
  'EM_APURACAO',
  'RESPONDIDA',
  'ENCERRADA',
] as const;

export function corStatusMatricula(status: StatusMatricula): string {
  switch (status) {
    case 'APROVADO':
      return 'badge-success';
    case 'ATIVA':
      return 'badge-warning';
    case 'CANCELADA':
      return 'badge-neutral';
    default:
      return 'badge-danger';
  }
}

export function corStatusDenuncia(status: StatusDenuncia): string {
  switch (status) {
    case 'ENCERRADA':
    case 'RESPONDIDA':
      return 'badge-success';
    case 'RECEBIDA':
      return 'badge-neutral';
    case 'EM_TRIAGEM':
    case 'ENCAMINHADA':
      return 'badge-warning';
    default:
      return 'badge-danger';
  }
}

export function corEvento(evento: EventoSeguranca): string {
  switch (evento) {
    case 'LOGIN_SUCESSO':
      return 'badge-success';
    case 'LOGIN_FALHA':
    case 'NAO_AUTENTICADO':
      return 'badge-warning';
    default:
      return 'badge-danger';
  }
}
