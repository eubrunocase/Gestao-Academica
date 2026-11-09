import { Component, computed, input } from '@angular/core';
import { PercentualPipe } from '../pipes/percentual.pipe';

@Component({
  selector: 'app-frequencia-badge',
  standalone: true,
  imports: [PercentualPipe],
  template: `
    <span class="badge" [class]="'badge ' + classe()">
      {{ percentual() | percentual }}
    </span>
  `,
})
export class FrequenciaBadgeComponent {
  readonly percentual = input<number | null | undefined>(null);
  readonly minimo = input(75);

  readonly classe = computed(() => {
    const valor = this.percentual();
    if (valor === null || valor === undefined) {
      return 'badge-neutral';
    }
    if (valor < this.minimo()) {
      return 'badge-danger';
    }
    if (valor < this.minimo() + 5) {
      return 'badge-warning';
    }
    return 'badge-success';
  });
}
