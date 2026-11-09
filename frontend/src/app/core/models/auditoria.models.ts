export type EventoSeguranca =
  | 'LOGIN_SUCESSO'
  | 'LOGIN_FALHA'
  | 'ACESSO_NEGADO'
  | 'BLOQUEIO'
  | 'NAO_AUTENTICADO';

export interface LogSeguranca {
  id: number;
  usuario: string | null;
  evento: EventoSeguranca;
  ip: string;
  criadoEm: string;
}

export interface LogAuditoria {
  id: number;
  usuario: string | null;
  acao: string;
  entidade: string;
  entidadeId: number | null;
  detalhe: string | null;
  criadoEm: string;
}
