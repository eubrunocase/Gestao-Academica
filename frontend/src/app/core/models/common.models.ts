export interface ApiError {
  timestamp?: string;
  status: number;
  mensagem: string;
  campos?: Record<string, string>;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export type MotivoMatricula =
  | 'PREREQUISITO'
  | 'SEM_VAGA'
  | 'FORA_DO_PRAZO'
  | 'TURMA_ENCERRADA'
  | 'JA_MATRICULADO';

export interface DisciplinaPendente {
  id: number;
  nome: string;
}

export interface TurmaAlternativa {
  id: number;
  disciplina: string;
  professor: string;
  periodoLetivo: string;
  vagasDisponiveis: number;
}

export interface ErroMatricula extends ApiError {
  motivo: MotivoMatricula;
  disciplinasPendentes: DisciplinaPendente[];
  turmasAlternativas: TurmaAlternativa[];
}
