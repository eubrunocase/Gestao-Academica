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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @GetMapping
    public List<CursoResponse> listar() {
        return cursoService.listar();
    }

    @GetMapping("/{id}")
    public CursoResponse buscar(@PathVariable Long id) {
        return cursoService.buscar(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SECRETARIA','COORDENADOR','ADMIN')")
    public CursoResponse criar(@Valid @RequestBody CursoRequest request) {
        return cursoService.criar(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SECRETARIA','COORDENADOR','ADMIN')")
    public CursoResponse atualizar(@PathVariable Long id, @Valid @RequestBody CursoRequest request) {
        return cursoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SECRETARIA','COORDENADOR','ADMIN')")
    public void excluir(@PathVariable Long id) {
        cursoService.excluir(id);
    }
}
