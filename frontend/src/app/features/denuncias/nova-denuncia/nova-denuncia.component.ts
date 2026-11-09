import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { RouterLink } from '@angular/router';
import { DenunciaService } from '../../../core/services/denuncia.service';
import {
  DenunciaCriada,
  DenunciaRequest,
} from '../../../core/models/denuncia.models';
import { ApiError } from '../../../core/models/common.models';
import { ROTULO_STATUS_DENUNCIA } from '../../../core/utils/rotulos';
import { PageHeaderComponent } from '../../../shared/components/page-header.component';
import { DataBrPipe } from '../../../shared/pipes/data-br.pipe';

@Component({
  selector: 'app-nova-denuncia',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatSelectModule,
    PageHeaderComponent,
    DataBrPipe,
  ],
  templateUrl: './nova-denuncia.component.html',
  styleUrl: './nova-denuncia.component.scss',
})
export class NovaDenunciaComponent {
  private readonly fb = inject(FormBuilder);
  private readonly denunciaService = inject(DenunciaService);
  private readonly snackBar = inject(MatSnackBar);

  readonly categorias = [
    { valor: 'ASSEDIO', rotulo: 'Assédio' },
    { valor: 'DISCRIMINACAO', rotulo: 'Discriminação' },
    { valor: 'INFRAESTRUTURA', rotulo: 'Infraestrutura' },
    { valor: 'SEGURANCA', rotulo: 'Segurança' },
    { valor: 'CONDUTA', rotulo: 'Conduta' },
    { valor: 'OUTRO', rotulo: 'Outro' },
  ];
  readonly rotuloStatus = ROTULO_STATUS_DENUNCIA;

  readonly enviando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly criada = signal<DenunciaCriada | null>(null);

  readonly form = this.fb.nonNullable.group({
    anonima: [false],
    categoria: ['', [Validators.required]],
    descricao: ['', [Validators.required, Validators.maxLength(4000)]],
    dataAproximada: [''],
  });

  enviar(): void {
    this.erro.set(null);
    this.form.markAllAsTouched();
    if (this.form.invalid || this.enviando()) {
      return;
    }

    const valores = this.form.getRawValue();
    const request: DenunciaRequest = {
      anonima: valores.anonima,
      categoria: valores.categoria,
      descricao: valores.descricao,
      dataAproximada: valores.dataAproximada || null,
    };

    this.enviando.set(true);
    this.denunciaService.criar(request).subscribe({
      next: (criada) => {
        this.enviando.set(false);
        this.criada.set(criada);
        this.form.reset({
          anonima: false,
          categoria: '',
          descricao: '',
          dataAproximada: '',
        });
      },
      error: (erro: unknown) => {
        this.enviando.set(false);
        const corpo =
          erro instanceof HttpErrorResponse ? (erro.error as ApiError | null) : null;
        this.erro.set(corpo?.mensagem ?? 'Não foi possível registrar a denúncia.');
      },
    });
  }

  copiarProtocolo(): void {
    const protocolo = this.criada()?.protocolo;
    if (!protocolo) {
      return;
    }
    if (navigator.clipboard?.writeText) {
      navigator.clipboard.writeText(protocolo).then(
        () =>
          this.snackBar.open('Protocolo copiado.', 'Fechar', { duration: 3000 }),
        () => this.avisarCopiaManual(protocolo),
      );
      return;
    }
    this.avisarCopiaManual(protocolo);
  }

  novaDenuncia(): void {
    this.criada.set(null);
    this.erro.set(null);
  }

  private avisarCopiaManual(protocolo: string): void {
    this.snackBar.open(`Copie o protocolo: ${protocolo}`, 'Fechar', {
      duration: 8000,
    });
  }
}
