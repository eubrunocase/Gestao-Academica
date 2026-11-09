package br.ucsal.gestaoacademica.chatbot;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

@Component
public class GeminiClient {

    private final RestClient restClient;
    private final String modelo;
    private final String apiKey;
    private final String promptSistema;

    public GeminiClient(
            @Value("${gemini.base-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl,
            @Value("${gemini.model:gemini-2.5-flash}") String modelo,
            @Value("${gemini.api-key:}") String apiKey,
            @Value("${gemini.system-prompt:}") String promptSistema) {
        this.modelo = modelo;
        this.apiKey = apiKey;
        this.promptSistema = promptSistema;
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public String perguntar(String pergunta) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY nao configurada");
        }
        Map<String, Object> corpo = Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", promptSistema))),
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", pergunta)))));

        JsonNode resposta = restClient.post()
                .uri("/models/{modelo}:generateContent", modelo)
                .header("x-goog-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(corpo)
                .retrieve()
                .body(JsonNode.class);

        if (resposta == null) {
            throw new IllegalStateException("Resposta vazia do Gemini");
        }
        String texto = resposta.path("candidates").path(0).path("content").path("parts")
                .path(0).path("text").asString(null);
        if (texto == null || texto.isBlank()) {
            throw new IllegalStateException("Gemini nao retornou texto");
        }
        return texto.trim();
    }
}
