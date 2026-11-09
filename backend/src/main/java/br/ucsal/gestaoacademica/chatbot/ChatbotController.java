package br.ucsal.gestaoacademica.chatbot;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/perguntas")
    @PreAuthorize("isAuthenticated()")
    public RespostaRequest perguntar(@Valid @RequestBody PerguntaRequest request) {
        return new RespostaRequest(chatbotService.responder(request.pergunta()));
    }
}
