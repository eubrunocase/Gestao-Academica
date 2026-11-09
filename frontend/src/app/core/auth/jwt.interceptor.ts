import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { API_BASE } from '../http/api.config';
import { SessionService } from './session.service';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const session = inject(SessionService);
  const token = session.token();
  const ehApi = req.url.startsWith(API_BASE);
  const ehLogin = req.url.endsWith('/auth/login');

  if (!ehApi || ehLogin || !token) {
    return next(req);
  }

  return next(
    req.clone({
      setHeaders: { Authorization: `Bearer ${token}` },
    }),
  );
};
