import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE } from '../http/api.config';
import {
  BoletimRelatorio,
  HistoricoAluno,
  IndicadorDisciplina,
  IndicadorTurma,
  RelatorioTurma,
} from '../models/relatorio.models';

@Injectable({ providedIn: 'root' })
export class RelatorioService {
  private readonly http = inject(HttpClient);

  relatorioTurma(turmaId: number): Observable<RelatorioTurma> {
    return this.http.get<RelatorioTurma>(
      `${API_BASE}/relatorios/turma/${turmaId}`,
    );
  }

  relatorioTurmaCsv(turmaId: number): Observable<string> {
    return this.http.get(
      `${API_BASE}/relatorios/turma/${turmaId}?formato=csv`,
      { responseType: 'text' },
    );
  }

  boletim(matriculaId: number): Observable<BoletimRelatorio> {
    return this.http.get<BoletimRelatorio>(
      `${API_BASE}/relatorios/boletim/${matriculaId}`,
    );
  }

  historico(alunoId: number): Observable<HistoricoAluno> {
    return this.http.get<HistoricoAluno>(
      `${API_BASE}/relatorios/historico/${alunoId}`,
    );
  }

  historicoCsv(alunoId: number): Observable<string> {
    return this.http.get(
      `${API_BASE}/relatorios/historico/${alunoId}?formato=csv`,
      { responseType: 'text' },
    );
  }

  indicadoresDisciplinas(): Observable<IndicadorDisciplina[]> {
    return this.http.get<IndicadorDisciplina[]>(
      `${API_BASE}/indicadores/disciplinas`,
    );
  }

  indicadoresTurma(turmaId: number): Observable<IndicadorTurma> {
    return this.http.get<IndicadorTurma>(
      `${API_BASE}/indicadores/turmas/${turmaId}`,
    );
  }
}
