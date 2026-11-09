import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export interface ConfirmDialogData {
  titulo: string;
  mensagem: string;
  confirmar?: string;
  cancelar?: string;
  destrutivo?: boolean;
}

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <h2 mat-dialog-title>{{ dados.titulo }}</h2>
    <mat-dialog-content>
      <div class="confirm-body">
        <mat-icon [class.destrutivo]="dados.destrutivo" aria-hidden="true">
          {{ dados.destrutivo ? 'warning' : 'help_outline' }}
        </mat-icon>
        <p>{{ dados.mensagem }}</p>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button type="button" (click)="fechar(false)">
        {{ dados.cancelar ?? 'Voltar' }}
      </button>
      <button
        mat-flat-button
        type="button"
        [color]="dados.destrutivo ? 'warn' : 'primary'"
        (click)="fechar(true)"
        cdkFocusInitial
      >
        {{ dados.confirmar ?? 'Confirmar' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      .confirm-body {
        display: flex;
        gap: 14px;
        align-items: flex-start;
      }

      mat-icon {
        color: var(--ga-primary);
        flex: 0 0 auto;
      }

      mat-icon.destrutivo {
        color: var(--ga-warn);
      }

      p {
        margin: 4px 0 0;
      }
    `,
  ],
})
export class ConfirmDialogComponent {
  readonly dados = inject<ConfirmDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<ConfirmDialogComponent, boolean>);

  fechar(resultado: boolean): void {
    this.dialogRef.close(resultado);
  }
}
