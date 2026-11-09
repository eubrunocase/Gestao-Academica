import {
  HttpErrorResponse,
  HttpInterceptorFn,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { ApiError } from '../models/common.models';
import { SessionService } from '../auth/session.service';

function mensagemDoErro(erro: HttpErrorResponse, fallback: string): string {
  const corpo: unknown = erro.error;
  if (corpo && typeof corpo === 'object' && 'mensagem' in corpo) {
    const mensagem = (corpo as { mensagem?: unknown }).mensagem;
    if (typeof mensagem === 'string' && mensagem.trim()) {
      return mensagem;
    }
  }
  if (typeof corpo === 'string' && corpo.trim()) {
    return corpo;
  }
  return fallback;
}

function temErrosDeCampo(erro: HttpErrorResponse): boolean {
  const corpo: unknown = erro.error;
  return !!(
    corpo &&
    typeof corpo === 'object' &&
    'campos' in corpo &&
    (corpo as ApiError).campos &&
    Object.keys((corpo as ApiError).campos ?? {}).length > 0
  );
}

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);
  const router = inject(Router);
  const session = inject(SessionService);

  return next(req).pipe(
    catchError((erro: unknown) => {
      if (!(erro instanceof HttpErrorResponse)) {
        return throwError(() => erro);
      }

      const ehLogin = req.url.endsWith('/auth/login');

      if (erro.status === 401 && !ehLogin) {
        session.clear();
        snackBar.open('Sua sessão expirou. Entre novamente.', 'Fechar', {
          duration: 5000,
        });
        void router.navigate(['/login']);
        return throwError(() => erro);
      }

      if (erro.status === 403) {
        snackBar.open('Sem permissão para esta ação.', 'Fechar', {
          duration: 4500,
        });
        return throwError(() => erro);
      }

      if (erro.status === 422 || (erro.status === 400 && temErrosDeCampo(erro))) {
        return throwError(() => erro);
      }

      if (erro.status === 503) {
        snackBar.open(
          mensagemDoErro(erro, 'Atendimento temporariamente indisponível.'),
          'Fechar',
          { duration: 6000 },
        );
        return throwError(() => erro);
      }

      if (!ehLogin || erro.status !== 401) {
        snackBar.open(
          mensagemDoErro(erro, 'Não foi possível concluir a operação.'),
          'Fechar',
          { duration: 5500 },
        );
      }

      return throwError(() => erro);
    }),
  );
};
