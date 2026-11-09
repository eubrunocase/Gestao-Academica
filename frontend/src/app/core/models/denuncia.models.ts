export type StatusDenuncia =
  | 'RECEBIDA'
  | 'EM_TRIAGEM'
  | 'ENCAMINHADA'
  | 'EM_APURACAO'
  | 'RESPONDIDA'
  | 'ENCERRADA';

export interface Andamento {
  id: number;
  etapa: string;
  descricao: string;
  autor: string;
  criadoEm: string;
}

export interface AndamentoRequest {
  etapa: string;
  descricao: string;
  status?: StatusDenuncia | null;
}

export interface Denuncia {
  id: number;
  protocolo: string;
  anonima: boolean;
  denunciante: string | null;
  categoria: string;
  descricao: string;
  dataAproximada: string | null;
  status: StatusDenuncia;
  criadoEm: string;
  andamentos: Andamento[];
}

export interface DenunciaRequest {
  anonima: boolean;
  categoria: string;
  descricao: string;
  dataAproximada?: string | null;
}

export interface DenunciaCriada {
  protocolo: string;
  status: StatusDenuncia;
  anonima: boolean;
  criadoEm: string;
}

export interface EstatisticasDenuncia {
  total: number;
  anonimas: number;
  porStatus: Record<string, number>;
  porCategoria: Record<string, number>;
}
