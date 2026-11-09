import { Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-stat-card',
  standalone: true,
  imports: [MatIconModule],
  template: `
    <div class="stat-card" [class]="'stat-card ' + tom()">
      <div class="stat-icon" aria-hidden="true">
        <mat-icon>{{ icone() }}</mat-icon>
      </div>
      <div class="stat-content">
        <span class="stat-label">{{ rotulo() }}</span>
        <strong class="stat-value">{{ valor() ?? '—' }}</strong>
        @if (detalhe()) {
          <span class="stat-detail">{{ detalhe() }}</span>
        }
      </div>
    </div>
  `,
  styles: [
    `
      .stat-card {
        display: flex;
        align-items: center;
        gap: 14px;
        background: #fff;
        border: 1px solid var(--ga-border);
        border-radius: 16px;
        padding: 18px;
        min-height: 96px;
        box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04), 0 6px 20px rgba(16, 24, 40, 0.04);
      }

      .stat-icon {
        display: grid;
        place-items: center;
        width: 48px;
        height: 48px;
        border-radius: 14px;
        background: var(--ga-primary-light);
        color: var(--ga-primary);
        flex: 0 0 auto;
      }

      .stat-content {
        display: flex;
        flex-direction: column;
        min-width: 0;
      }

      .stat-label {
        font-size: 0.78rem;
        text-transform: uppercase;
        letter-spacing: 0.04em;
        color: var(--ga-text-muted);
        font-weight: 600;
      }

      .stat-value {
        font-size: 1.6rem;
        line-height: 1.2;
        color: var(--ga-text);
        overflow-wrap: anywhere;
      }

      .stat-detail {
        font-size: 0.78rem;
        color: var(--ga-text-muted);
      }

      .stat-card.alerta .stat-icon {
        background: var(--ga-alert-bg);
        color: var(--ga-alert);
      }

      .stat-card.risco .stat-icon {
        background: var(--ga-warn-bg);
        color: var(--ga-warn);
      }

      .stat-card.sucesso .stat-icon {
        background: var(--ga-success-bg);
        color: var(--ga-success);
      }
    `,
  ],
})
export class StatCardComponent {
  readonly rotulo = input.required<string>();
  readonly valor = input.required<string | number | null>();
  readonly icone = input('insights');
  readonly detalhe = input<string>();
  readonly tom = input<'padrao' | 'alerta' | 'risco' | 'sucesso'>('padrao');
}
