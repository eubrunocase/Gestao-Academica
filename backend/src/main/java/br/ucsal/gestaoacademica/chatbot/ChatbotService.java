package br.ucsal.gestaoacademica.chatbot;

import br.ucsal.gestaoacademica.common.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);
    private static final String RESPOSTA_FALLBACK =
            "No momento o atendimento automatico esta indisponivel. "
            + "Consulte a Secretaria Academica ou a Coordenacao do seu curso para "
            + "esclarecer duvidas especificas sobre regras e procedimentos.";

    private final GeminiClient geminiClient;
    private final boolean fallbackHabilitado;

    public ChatbotService(GeminiClient geminiClient,
            @Value("${gemini.fallback-enabled:false}") boolean fallbackHabilitado) {
        this.geminiClient = geminiClient;
        this.fallbackHabilitado = fallbackHabilitado;
    }

    public String responder(String pergunta) {
        if (pergunta == null || pergunta.isBlank()) {
            throw new BusinessException("Digite sua duvida.");
        }
        String texto = pergunta.trim();
        if (texto.length() > 1000) {
            throw new BusinessException("A pergunta deve ter no maximo 1000 caracteres");
        }
        try {
            return geminiClient.perguntar(texto);
        } catch (Exception ex) {
            log.warn("Falha ao consultar o Gemini: {}", ex.getMessage());
            if (fallbackHabilitado) {
                return RESPOSTA_FALLBACK;
            }
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Atendimento temporariamente indisponivel.");
        }
    }
}
