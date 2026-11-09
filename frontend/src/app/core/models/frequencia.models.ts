export interface Aula {
  id: number;
  data: string;
  conteudo: string | null;
}

export interface AulaRequest {
  data: string;
  conteudo?: string | null;
}

export interface RegistroFrequencia {
  id: number;
  aulaId: number;
  matriculaId: number;
  presente: boolean;
}

export interface FrequenciaLancamento {
  matriculaId: number;
  presente: boolean;
}

export interface FrequenciaLoteRequest {
  registros: FrequenciaLancamento[];
}

export interface AlunoFrequenciaTurma {
  matriculaId: number;
  alunoId: number;
  nome: string;
  numeroMatricula: string;
  presencas: number;
  totalAulas: number;
  percentual: number | null;
  registros: RegistroFrequencia[];
}

export interface TurmaFrequencia {
  turmaId: number;
  aulas: Aula[];
  alunos: AlunoFrequenciaTurma[];
}

export interface AlunoFrequencia {
  matriculaId: number;
  disciplina: string;
  professor: string;
  periodoLetivo: string;
  totalAulas: number;
  presencas: number;
  faltas: number;
  percentual: number | null;
  frequenciaMinima: number;
  alerta: boolean;
}
