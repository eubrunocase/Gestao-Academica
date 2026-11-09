package br.ucsal.gestaoacademica.academico;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calendarios")
public class CalendarioController {

    private final CalendarioService calendarioService;

    public CalendarioController(CalendarioService calendarioService) {
        this.calendarioService = calendarioService;
    }

    @GetMapping
    public List<CalendarioResponse> listar() {
        return calendarioService.listar();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SECRETARIA','COORDENADOR','ADMIN')")
    public CalendarioResponse criar(@Valid @RequestBody CalendarioRequest request) {
        return calendarioService.criar(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SECRETARIA','COORDENADOR','ADMIN')")
    public CalendarioResponse atualizar(@PathVariable Long id,
            @Valid @RequestBody CalendarioRequest request) {
        return calendarioService.atualizar(id, request);
    }
}
