import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE } from '../http/api.config';
import {
  PerguntaRequest,
  RespostaChatbot,
} from '../models/chatbot.models';

@Injectable({ providedIn: 'root' })
export class ChatbotService {
  private readonly http = inject(HttpClient);

  perguntar(pergunta: string): Observable<RespostaChatbot> {
    const request: PerguntaRequest = { pergunta };
    return this.http.post<RespostaChatbot>(
      `${API_BASE}/chatbot/perguntas`,
      request,
    );
  }
}
