package br.ucsal.gestaoacademica.relatorio;

import br.ucsal.gestaoacademica.security.SecurityUtils;
import br.ucsal.gestaoacademica.security.UsuarioPrincipal;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private static final MediaType CSV = MediaType.parseMediaType("text/csv;charset=UTF-8");

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/boletim/{matriculaId}")
    @PreAuthorize("hasAnyRole('ALUNO','PROFESSOR','SECRETARIA','COORDENADOR','ADMIN')")
    public BoletimRelatorioResponse boletim(@PathVariable Long matriculaId) {
        UsuarioPrincipal principal = SecurityUtils.principal();
        return relatorioService.boletim(matriculaId, principal.getId(), principal.getPerfil());
    }

    @GetMapping("/historico/{alunoId}")
    @PreAuthorize("hasAnyRole('ALUNO','SECRETARIA','COORDENADOR','ADMIN')")
    public ResponseEntity<?> historico(@PathVariable Long alunoId,
            @RequestParam(defaultValue = "json") String formato) {
        UsuarioPrincipal principal = SecurityUtils.principal();
        if ("csv".equalsIgnoreCase(formato)) {
            return csv("historico-" + alunoId + ".csv", relatorioService.historicoCsv(
                    alunoId, principal.getId(), principal.getPerfil()));
        }
        return ResponseEntity.ok(relatorioService.historico(
                alunoId, principal.getId(), principal.getPerfil()));
    }

    @GetMapping("/turma/{turmaId}")
    @PreAuthorize("hasAnyRole('PROFESSOR','SECRETARIA','COORDENADOR','ADMIN')")
    public ResponseEntity<?> turma(@PathVariable Long turmaId,
            @RequestParam(defaultValue = "json") String formato) {
        UsuarioPrincipal principal = SecurityUtils.principal();
        if ("csv".equalsIgnoreCase(formato)) {
            return csv("relatorio-turma-" + turmaId + ".csv", relatorioService.relatorioTurmaCsv(
                    turmaId, principal.getId(), principal.getPerfil()));
        }
        return ResponseEntity.ok(relatorioService.relatorioTurma(
                turmaId, principal.getId(), principal.getPerfil()));
    }

    private ResponseEntity<String> csv(String nomeArquivo, String conteudo) {
        return ResponseEntity.ok()
                .contentType(CSV)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nomeArquivo + "\"")
                .body(conteudo);
    }
}
