import { Component, input } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { EmptyStateComponent } from './empty-state.component';

export interface ColunaTabela {
  chave: string;
  titulo: string;
}

@Component({
  selector: 'app-data-table',
  standalone: true,
  imports: [MatTableModule, EmptyStateComponent],
  template: `
    @if (linhas().length === 0) {
      <app-empty-state [titulo]="vazioTitulo()" [mensagem]="vazioMensagem()" />
    } @else {
      <div class="table-container">
        <table mat-table [dataSource]="linhas()">
          @for (coluna of colunas(); track coluna.chave) {
            <ng-container [matColumnDef]="coluna.chave">
              <th mat-header-cell *matHeaderCellDef>{{ coluna.titulo }}</th>
              <td mat-cell *matCellDef="let linha">{{ valor(linha, coluna.chave) }}</td>
            </ng-container>
          }
          <tr mat-header-row *matHeaderRowDef="chaves()"></tr>
          <tr mat-row *matRowDef="let linha; columns: chaves()"></tr>
        </table>
      </div>
    }
  `,
})
export class DataTableComponent {
  readonly colunas = input.required<ColunaTabela[]>();
  readonly linhas = input.required<Record<string, unknown>[]>();
  readonly vazioTitulo = input('Nenhum registro');
  readonly vazioMensagem = input('Não há dados para exibir.');

  chaves(): string[] {
    return this.colunas().map((coluna) => coluna.chave);
  }

  valor(linha: Record<string, unknown>, chave: string): string {
    const valor = linha[chave];
    return valor === null || valor === undefined ? '—' : String(valor);
  }
}
