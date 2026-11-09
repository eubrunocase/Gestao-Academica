package br.ucsal.gestaoacademica.academico;

import br.ucsal.gestaoacademica.security.SecurityUtils;
import br.ucsal.gestaoacademica.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/turmas")
public class TurmaController {

    private final TurmaService turmaService;

    public TurmaController(TurmaService turmaService) {
        this.turmaService = turmaService;
    }

    @GetMapping
    public List<TurmaResponse> listar(@RequestParam(required = false) String periodoLetivo,
            @RequestParam(required = false) Long professorId) {
        return turmaService.listar(periodoLetivo, professorId);
    }

    @GetMapping("/{id}")
    public TurmaResponse buscar(@PathVariable Long id) {
        return turmaService.buscar(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SECRETARIA','COORDENADOR','ADMIN')")
    public TurmaResponse criar(@Valid @RequestBody TurmaRequest request) {
        return turmaService.criar(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SECRETARIA','COORDENADOR','ADMIN')")
    public TurmaResponse atualizar(@PathVariable Long id, @Valid @RequestBody TurmaRequest request) {
        return turmaService.atualizar(id, request);
    }

    @PatchMapping("/{id}/encerrar")
    @PreAuthorize("hasAnyRole('PROFESSOR','SECRETARIA','COORDENADOR','ADMIN')")
    public TurmaResponse encerrar(@PathVariable Long id) {
        UsuarioPrincipal principal = SecurityUtils.principal();
        return turmaService.encerrar(id, principal.getId(), principal.getPerfil());
    }

    @GetMapping("/{id}/alunos")
    @PreAuthorize("hasAnyRole('PROFESSOR','SECRETARIA','COORDENADOR','ADMIN')")
    public List<TurmaService.AlunoTurmaResponse> listarAlunos(@PathVariable Long id) {
        UsuarioPrincipal principal = SecurityUtils.principal();
        return turmaService.listarAlunos(id, principal.getId(), principal.getPerfil());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SECRETARIA','COORDENADOR','ADMIN')")
    public void excluir(@PathVariable Long id) {
        turmaService.excluir(id);
    }
}
