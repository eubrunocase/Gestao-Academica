package br.ucsal.gestaoacademica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import br.ucsal.gestaoacademica.chatbot.ChatbotService;
import br.ucsal.gestaoacademica.chatbot.GeminiClient;
import br.ucsal.gestaoacademica.common.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ChatbotServiceTest {

    @Mock
    private GeminiClient geminiClient;

    @Test
    void recusaPerguntaEmBranco() {
        ChatbotService service = new ChatbotService(geminiClient, false);

        assertThatThrownBy(() -> service.responder("   "))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Digite sua duvida");
    }

    @Test
    void recusaPerguntaMuitoLonga() {
        ChatbotService service = new ChatbotService(geminiClient, false);

        assertThatThrownBy(() -> service.responder("a".repeat(1001)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("1000");
    }

    @Test
    void retornaRespostaDoGemini() {
        when(geminiClient.perguntar("Como faco matricula?"))
                .thenReturn("Acesse o portal do aluno.");
        ChatbotService service = new ChatbotService(geminiClient, false);

        assertThat(service.responder("Como faco matricula?"))
                .isEqualTo("Acesse o portal do aluno.");
    }

    @Test
    void retorna503QuandoApiFalha() {
        when(geminiClient.perguntar("Como faco matricula?"))
                .thenThrow(new RuntimeException("timeout"));
        ChatbotService service = new ChatbotService(geminiClient, false);

        assertThatThrownBy(() -> service.responder("Como faco matricula?"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus())
                        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE))
                .hasMessageContaining("indisponivel");
    }

    @Test
    void usaFallbackNoPerfilDeDemonstracao() {
        when(geminiClient.perguntar("Como faco matricula?"))
                .thenThrow(new RuntimeException("sem cota"));
        ChatbotService service = new ChatbotService(geminiClient, true);

        assertThat(service.responder("Como faco matricula?"))
                .contains("Secretaria Academica");
    }
}
