package br.ucsal.gestaoacademica.academico;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/disciplinas")
public class DisciplinaController {

    private final DisciplinaService disciplinaService;

    public DisciplinaController(DisciplinaService disciplinaService) {
        this.disciplinaService = disciplinaService;
    }

    @GetMapping
    public List<DisciplinaResponse> listar(@RequestParam(required = false) Long cursoId) {
        return disciplinaService.listar(cursoId);
    }

    @GetMapping("/{id}")
    public DisciplinaResponse buscar(@PathVariable Long id) {
        return disciplinaService.buscar(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SECRETARIA','COORDENADOR','ADMIN')")
    public DisciplinaResponse criar(@Valid @RequestBody DisciplinaRequest request) {
        return disciplinaService.criar(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SECRETARIA','COORDENADOR','ADMIN')")
    public DisciplinaResponse atualizar(@PathVariable Long id,
            @Valid @RequestBody DisciplinaRequest request) {
        return disciplinaService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SECRETARIA','COORDENADOR','ADMIN')")
    public void excluir(@PathVariable Long id) {
        disciplinaService.excluir(id);
    }
}
