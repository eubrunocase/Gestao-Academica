import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth/auth.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  template: '<router-outlet />',
})
export class App {
  private readonly auth = inject(AuthService);

  constructor() {
    if (this.auth.estaLogado()) {
      this.auth.carregarUsuario().subscribe({ error: () => undefined });
    }
  }
}
