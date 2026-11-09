export type EscopoAviso = 'TURMA' | 'CURSO' | 'GERAL';

export interface Aviso {
  id: number;
  autor: string;
  titulo: string;
  mensagem: string;
  escopo: EscopoAviso;
  turmaId: number | null;
  turmaDisciplina: string | null;
  cursoId: number | null;
  cursoNome: string | null;
  criadoEm: string;
}

export interface AvisoRequest {
  titulo: string;
  mensagem: string;
  escopo: EscopoAviso;
  turmaId?: number | null;
  cursoId?: number | null;
}
