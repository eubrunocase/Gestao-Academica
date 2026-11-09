import { StatusMatricula } from './academico.models';
import { BoletimAluno } from './avaliacao.models';
import { Aviso } from './comunicacao.models';

export interface AlunoDashboard {
  nome: string;
  periodoLetivo: string | null;
  disciplinasAtivas: number;
  mediaGeral: number | null;
  frequenciaMedia: number | null;
  alertaFrequencia: boolean;
  avisos: Aviso[];
  boletins: BoletimAluno[];
}

export interface BoletimRelatorio {
  alunoId: number;
  aluno: string;
  numeroMatricula: string;
  boletim: BoletimAluno;
}

export interface ItemHistorico {
  matriculaId: number;
  periodoLetivo: string;
  disciplina: string;
  curso: string;
  cargaHoraria: number;
  professor: string;
  media: number | null;
  frequencia: number | null;
  status: StatusMatricula;
}

export interface HistoricoAluno {
  alunoId: number;
  nome: string;
  numeroMatricula: string;
  mediaGeral: number | null;
  disciplinas: ItemHistorico[];
}

export interface AlunoRelatorio {
  matriculaId: number;
  alunoId: number;
  nome: string;
  numeroMatricula: string;
  media: number | null;
  frequencia: number | null;
  situacao: StatusMatricula;
}

export interface RelatorioTurma {
  turmaId: number;
  disciplina: string;
  professor: string;
  periodoLetivo: string;
  encerrada: boolean;
  mediaTurma: number | null;
  totalAlunos: number;
  aprovados: number;
  reprovadosNota: number;
  reprovadosFalta: number;
  ativos: number;
  alunos: AlunoRelatorio[];
}

export interface IndicadorDisciplina {
  disciplinaId: number;
  disciplina: string;
  curso: string;
  matriculas: number;
  media: number | null;
  aprovados: number;
  reprovadosNota: number;
  reprovadosFalta: number;
  emAndamento: number;
  taxaAprovacao: number | null;
  taxaReprovacao: number | null;
}

export interface PontoAluno {
  matriculaId: number;
  nome: string;
  media: number | null;
  frequencia: number | null;
}

export interface EvolucaoAvaliacao {
  avaliacaoId: number;
  descricao: string;
  peso: number;
  media: number | null;
  notasLancadas: number;
}

export interface IndicadorTurma {
  turmaId: number;
  disciplina: string;
  professor: string;
  periodoLetivo: string;
  alunos: PontoAluno[];
  evolucao: EvolucaoAvaliacao[];
}
