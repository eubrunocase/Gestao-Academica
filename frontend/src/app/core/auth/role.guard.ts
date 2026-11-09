import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { PerfilUsuario } from '../models/auth.models';
import { SessionService } from './session.service';

export function roleGuard(...perfis: PerfilUsuario[]): CanActivateFn {
  return () => {
    const session = inject(SessionService);
    const router = inject(Router);
    const perfil = session.perfil();

    if (perfil && perfis.includes(perfil)) {
      return true;
    }

    return router.createUrlTree([session.home()]);
  };
}
