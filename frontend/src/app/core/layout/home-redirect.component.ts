import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-home-redirect',
  standalone: true,
  template: `
    <div class="redirect" role="status" aria-live="polite">
      <span>Redirecionando…</span>
    </div>
  `,
  styles: [
    `
      .redirect {
        display: grid;
        place-items: center;
        min-height: 50vh;
        color: var(--ga-text-muted);
      }
    `,
  ],
})
export class HomeRedirectComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  constructor() {
    void this.router.navigateByUrl(this.auth.home(), { replaceUrl: true });
  }
}
