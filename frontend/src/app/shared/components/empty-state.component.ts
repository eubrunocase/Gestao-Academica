import { Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [MatIconModule],
  template: `
    <div class="empty-state" role="status">
      <mat-icon aria-hidden="true">{{ icone() }}</mat-icon>
      <h3>{{ titulo() }}</h3>
      <p class="muted">{{ mensagem() }}</p>
      <ng-content />
    </div>
  `,
  styles: [
    `
      .empty-state {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        text-align: center;
        gap: 6px;
        padding: 42px 20px;
        color: var(--ga-text-muted);
      }

      mat-icon {
        font-size: 44px;
        width: 44px;
        height: 44px;
        color: #98a2b3;
      }

      h3 {
        color: var(--ga-text);
        font-size: 1rem;
      }

      p {
        margin: 0;
        max-width: 460px;
        font-size: 0.9rem;
      }
    `,
  ],
})
export class EmptyStateComponent {
  readonly titulo = input('Nada por aqui');
  readonly mensagem = input('Não há registros para exibir.');
  readonly icone = input('inbox');
}
