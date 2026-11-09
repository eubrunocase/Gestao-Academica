package br.ucsal.gestaoacademica.frequencia;

import br.ucsal.gestaoacademica.security.SecurityUtils;
import br.ucsal.gestaoacademica.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasAnyRole('PROFESSOR','COORDENADOR','SECRETARIA','ADMIN')")
public class FrequenciaController {

    private final FrequenciaService frequenciaService;

    public FrequenciaController(FrequenciaService frequenciaService) {
        this.frequenciaService = frequenciaService;
    }

    @PutMapping("/aulas/{aulaId}/frequencia")
    public List<RegistroFrequenciaResponse> registrar(@PathVariable Long aulaId,
            @Valid @RequestBody FrequenciaLoteRequest request) {
        UsuarioPrincipal principal = SecurityUtils.principal();
        return frequenciaService.registrarFrequencia(aulaId, request, principal.getId(),
                principal.getPerfil());
    }

    @GetMapping("/turmas/{turmaId}/frequencia")
    public TurmaFrequenciaResponse listar(@PathVariable Long turmaId) {
        UsuarioPrincipal principal = SecurityUtils.principal();
        return frequenciaService.listarFrequenciaDaTurma(turmaId, principal.getId(),
                principal.getPerfil());
    }
}
