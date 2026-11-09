import {
  Component,
  ElementRef,
  computed,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ChatbotService } from '../services/chatbot.service';
import { MensagemChat } from '../models/chatbot.models';

@Component({
  selector: 'app-chatbot-widget',
  standalone: true,
  imports: [
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './chatbot-widget.component.html',
  styleUrl: './chatbot-widget.component.scss',
})
export class ChatbotWidgetComponent {
  private readonly chatbot = inject(ChatbotService);
  private readonly areaMensagens = viewChild<ElementRef<HTMLDivElement>>('areaMensagens');

  readonly aberto = signal(false);
  readonly texto = signal('');
  readonly enviando = signal(false);
  readonly mensagens = signal<MensagemChat[]>([
    {
      autor: 'bot',
      texto:
        'Olá! Sou o assistente virtual do portal acadêmico. Posso ajudar com dúvidas sobre matrícula, notas, frequência, histórico, avisos e denúncias.',
      horario: new Date(),
    },
  ]);

  readonly restante = computed(() => 1000 - this.texto().length);
  readonly podeEnviar = computed(
    () => this.texto().trim().length > 0 && !this.enviando(),
  );

  alternar(): void {
    this.aberto.update((valor) => !valor);
    if (this.aberto()) {
      this.rolarParaFim();
    }
  }

  fechar(): void {
    this.aberto.set(false);
  }

  enviar(): void {
    const pergunta = this.texto().trim();
    if (!pergunta || this.enviando()) {
      return;
    }

    this.mensagens.update((lista) => [
      ...lista,
      { autor: 'usuario', texto: pergunta, horario: new Date() },
    ]);
    this.texto.set('');
    this.enviando.set(true);
    this.rolarParaFim();

    this.chatbot.perguntar(pergunta).subscribe({
      next: (resposta) => {
        this.enviando.set(false);
        this.mensagens.update((lista) => [
          ...lista,
          {
            autor: 'bot',
            texto:
              resposta.resposta?.trim() ||
              'Não consegui formular uma resposta agora. Tente novamente.',
            horario: new Date(),
          },
        ]);
        this.rolarParaFim();
      },
      error: () => {
        this.enviando.set(false);
        this.mensagens.update((lista) => [
          ...lista,
          {
            autor: 'bot',
            texto: 'Atendimento temporariamente indisponível.',
            horario: new Date(),
          },
        ]);
        this.rolarParaFim();
      },
    });
  }

  aoDigitar(evento: Event): void {
    const alvo = evento.target as HTMLInputElement;
    this.texto.set(alvo.value.slice(0, 1000));
  }

  aoPressionarEnter(evento: Event): void {
    const teclado = evento as KeyboardEvent;
    if (teclado.key === 'Enter' && !teclado.shiftKey) {
      evento.preventDefault();
      this.enviar();
    }
  }

  private rolarParaFim(): void {
    setTimeout(() => {
      const elemento = this.areaMensagens()?.nativeElement;
      if (elemento) {
        elemento.scrollTop = elemento.scrollHeight;
      }
    }, 30);
  }
}
