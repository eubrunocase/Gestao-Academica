package br.ucsal.gestaoacademica.academico;

import br.ucsal.gestaoacademica.common.BusinessException;
import br.ucsal.gestaoacademica.security.SecurityUtils;
import br.ucsal.gestaoacademica.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matriculas")
public class MatriculaController {

    private final MatriculaService matriculaService;

    public MatriculaController(MatriculaService matriculaService) {
        this.matriculaService = matriculaService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ALUNO','SECRETARIA','ADMIN')")
    public MatriculaResponse matricular(@Valid @RequestBody MatriculaRequest request) {
        UsuarioPrincipal principal = SecurityUtils.principal();
        Long alunoId;
        if (principal.getPerfil().name().equals("ALUNO")) {
            alunoId = principal.getId();
        } else {
            if (request.alunoId() == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "Informe o aluno para realizar a matricula");
            }
            alunoId = request.alunoId();
        }
        return matriculaService.matricular(alunoId, request.turmaId());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ALUNO','SECRETARIA','ADMIN')")
    public void cancelar(@PathVariable Long id) {
        UsuarioPrincipal principal = SecurityUtils.principal();
        matriculaService.cancelar(id, principal.getId(), principal.getPerfil());
    }

    @GetMapping("/aluno/{alunoId}")
    @PreAuthorize("hasAnyRole('SECRETARIA','COORDENADOR','ADMIN')")
    public List<MatriculaResponse> listarDoAluno(@PathVariable Long alunoId) {
        return matriculaService.listarDoAluno(alunoId);
    }
}
