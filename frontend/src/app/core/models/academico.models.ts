export type StatusMatricula =
  | 'ATIVA'
  | 'CANCELADA'
  | 'APROVADO'
  | 'REPROVADO_NOTA'
  | 'REPROVADO_FALTA';

export interface Curso {
  id: number;
  nome: string;
}

export interface CursoRequest {
  nome: string;
}

export interface PrerequisitoResumo {
  id: number;
  nome: string;
}

export interface Disciplina {
  id: number;
  nome: string;
  cargaHoraria: number;
  curso: Curso;
  prerequisitos: PrerequisitoResumo[];
}

export interface DisciplinaRequest {
  nome: string;
  cargaHoraria: number;
  cursoId: number;
  prerequisitoIds?: number[];
}

export interface CalendarioAcademico {
  id: number;
  periodoLetivo: string;
  inicioMatricula: string;
  fimMatricula: string;
  fimPeriodo: string;
  matriculaAberta: boolean;
}

export interface CalendarioRequest {
  periodoLetivo: string;
  inicioMatricula: string;
  fimMatricula: string;
  fimPeriodo: string;
}

export interface TurmaDisciplinaResumo {
  id: number;
  nome: string;
  cargaHoraria: number;
}

export interface TurmaProfessorResumo {
  id: number;
  nome: string;
}

export interface Turma {
  id: number;
  disciplina: TurmaDisciplinaResumo;
  professor: TurmaProfessorResumo;
  periodoLetivo: string;
  limiteVagas: number;
  vagasOcupadas: number;
  vagasDisponiveis: number;
  encerrada: boolean;
}

export interface TurmaRequest {
  disciplinaId: number;
  professorId: number;
  periodoLetivo: string;
  limiteVagas: number;
}

export interface TurmaDisponivel {
  id: number;
  disciplinaId: number;
  disciplina: string;
  curso: string;
  professor: string;
  periodoLetivo: string;
  cargaHoraria: number;
  limiteVagas: number;
  vagasOcupadas: number;
  vagasDisponiveis: number;
}

export interface Matricula {
  id: number;
  turmaId: number;
  disciplina: string;
  professor: string;
  periodoLetivo: string;
  data: string;
  status: StatusMatricula;
}

export interface MatriculaRequest {
  turmaId: number;
  alunoId?: number | null;
}

export interface AlunoTurma {
  matriculaId: number;
  alunoId: number;
  nome: string;
  numeroMatricula: string;
  email: string;
  status: StatusMatricula;
}
