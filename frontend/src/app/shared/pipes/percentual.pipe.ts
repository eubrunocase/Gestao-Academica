import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'percentual' })
export class PercentualPipe implements PipeTransform {
  transform(value: number | null | undefined, casas = 1): string {
    if (value === null || value === undefined || Number.isNaN(value)) {
      return '—';
    }
    return `${new Intl.NumberFormat('pt-BR', {
      minimumFractionDigits: casas,
      maximumFractionDigits: casas,
    }).format(value)}%`;
  }
}
