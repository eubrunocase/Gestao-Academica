import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE } from '../http/api.config';
import { LogAuditoria, LogSeguranca } from '../models/auditoria.models';
import { PageResponse } from '../models/common.models';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);

  logsSeguranca(page = 0, size = 30): Observable<PageResponse<LogSeguranca>> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size));
    return this.http.get<PageResponse<LogSeguranca>>(
      `${API_BASE}/admin/logs-seguranca`,
      { params },
    );
  }

  logsAuditoria(page = 0, size = 30): Observable<PageResponse<LogAuditoria>> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size));
    return this.http.get<PageResponse<LogAuditoria>>(
      `${API_BASE}/admin/logs-auditoria`,
      { params },
    );
  }
}
