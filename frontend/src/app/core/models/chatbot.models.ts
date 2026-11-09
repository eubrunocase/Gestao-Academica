export interface PerguntaRequest {
  pergunta: string;
}

export interface RespostaChatbot {
  resposta: string;
}

export interface MensagemChat {
  autor: 'usuario' | 'bot';
  texto: string;
  horario: Date;
}
