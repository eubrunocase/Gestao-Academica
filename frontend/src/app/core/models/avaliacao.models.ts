import { StatusMatricula } from './academico.models';

export interface Avaliacao {
  id: number;
  descricao: string;
  peso: number;
}

export interface AvaliacaoRequest {
  descricao: string;
  peso: number;
}

export interface Nota {
  id: number;
  avaliacaoId: number;
  matriculaId: number;
  valor: number;
}

export interface NotaLancamento {
  avaliacaoId: number;
  matriculaId: number;
  valor: number;
}

export interface NotasLoteRequest {
  notas: NotaLancamento[];
}

export interface AlunoNotas {
  matriculaId: number;
  alunoId: number;
  nome: string;
  numeroMatricula: string;
  media: number | null;
  frequencia: number | null;
  notas: Nota[];
}

export interface TurmaNotas {
  turmaId: number;
  avaliacoes: Avaliacao[];
  alunos: AlunoNotas[];
}

export interface AvaliacaoNota {
  avaliacaoId: number;
  descricao: string;
  peso: number;
  valor: number | null;
}

export interface BoletimAluno {
  matriculaId: number;
  disciplina: string;
  professor: string;
  periodoLetivo: string;
  status: StatusMatricula;
  media: number | null;
  notaNecessaria: number | null;
  frequencia: number | null;
  alertaFrequencia: boolean;
  avaliacoes: AvaliacaoNota[];
}
