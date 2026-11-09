import { inject } from '@angular/core';
import {
  CanActivateFn,
  Router,
  RouterStateSnapshot,
} from '@angular/router';
import { SessionService } from './session.service';

export const authGuard: CanActivateFn = (_route, state: RouterStateSnapshot) => {
  const session = inject(SessionService);
  const router = inject(Router);

  if (session.estaLogado()) {
    return true;
  }

  return router.createUrlTree(['/login'], {
    queryParams: { redirect: state.url },
  });
};
