package br.ucsal.gestaoacademica.avaliacao;

import br.ucsal.gestaoacademica.academico.Turma;
import br.ucsal.gestaoacademica.academico.TurmaService;
import br.ucsal.gestaoacademica.auditoria.AuditoriaService;
import br.ucsal.gestaoacademica.common.BusinessException;
import br.ucsal.gestaoacademica.usuario.PerfilUsuario;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final NotaRepository notaRepository;
    private final TurmaService turmaService;
    private final AuditoriaService auditoriaService;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository, NotaRepository notaRepository,
            TurmaService turmaService, AuditoriaService auditoriaService) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.notaRepository = notaRepository;
        this.turmaService = turmaService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<AvaliacaoResponse> listar(Long turmaId, Long usuarioId, PerfilUsuario perfil) {
        Turma turma = turmaService.validarAcessoGestao(turmaId, usuarioId, perfil);
        return avaliacaoRepository.findByTurmaIdOrderByDescricaoAsc(turma.getId()).stream()
                .map(AvaliacaoResponse::from)
                .toList();
    }

    @Transactional
    public AvaliacaoResponse criar(Long turmaId, AvaliacaoRequest request, Long usuarioId,
            PerfilUsuario perfil) {
        Turma turma = turmaService.validarAcessoGestao(turmaId, usuarioId, perfil);
        if (turma.isEncerrada() && perfil == PerfilUsuario.PROFESSOR) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Turma encerrada: somente coordenacao/secretaria pode alterar avaliacoes");
        }
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setTurma(turma);
        avaliacao.setDescricao(request.descricao());
        avaliacao.setPeso(request.peso());
        avaliacaoRepository.save(avaliacao);
        auditoriaService.registrarAuditoria("CRIAR", "Avaliacao", avaliacao.getId(),
                request.descricao());
        return AvaliacaoResponse.from(avaliacao);
    }

    @Transactional
    public void excluir(Long id, Long usuarioId, PerfilUsuario perfil) {
        Avaliacao avaliacao = avaliacaoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "Avaliacao nao encontrada"));
        turmaService.validarAcessoGestao(avaliacao.getTurma().getId(), usuarioId, perfil);
        if (notaRepository.existsByAvaliacaoId(id)) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "Avaliacao possui notas lancadas e nao pode ser excluida");
        }
        avaliacaoRepository.delete(avaliacao);
        auditoriaService.registrarAuditoria("EXCLUIR", "Avaliacao", id,
                avaliacao.getDescricao());
    }
}
