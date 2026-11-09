import { Component, input } from '@angular/core';

@Component({
  selector: 'app-page-header',
  standalone: true,
  template: `
    <header class="page-header">
      <div class="page-header-text">
        <h1>{{ titulo() }}</h1>
        @if (subtitulo()) {
          <p class="muted">{{ subtitulo() }}</p>
        }
      </div>
      <div class="page-header-actions">
        <ng-content />
      </div>
    </header>
  `,
  styles: [
    `
      .page-header {
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        gap: 16px;
        margin-bottom: 20px;
        flex-wrap: wrap;
      }

      h1 {
        font-size: clamp(1.4rem, 2.2vw, 1.8rem);
        font-weight: 600;
        color: var(--ga-primary-dark);
      }

      p {
        margin: 4px 0 0;
        font-size: 0.9rem;
      }

      .page-header-actions {
        display: flex;
        gap: 10px;
        flex-wrap: wrap;
      }
    `,
  ],
})
export class PageHeaderComponent {
  readonly titulo = input.required<string>();
  readonly subtitulo = input<string>();
}
