import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'dataBr' })
export class DataBrPipe implements PipeTransform {
  transform(value: string | Date | null | undefined, incluirHora = false): string {
    if (!value) {
      return '—';
    }

    if (typeof value === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(value)) {
      const [ano, mes, dia] = value.split('-');
      return `${dia}/${mes}/${ano}`;
    }

    const data = value instanceof Date ? value : new Date(value);
    if (Number.isNaN(data.getTime())) {
      return '—';
    }

    return new Intl.DateTimeFormat('pt-BR', {
      dateStyle: 'short',
      ...(incluirHora ? { timeStyle: 'short' } : {}),
    }).format(data);
  }
}
