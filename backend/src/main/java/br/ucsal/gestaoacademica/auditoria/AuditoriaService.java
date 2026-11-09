package br.ucsal.gestaoacademica.auditoria;

import br.ucsal.gestaoacademica.common.PageResponse;
import br.ucsal.gestaoacademica.security.SecurityUtils;
import br.ucsal.gestaoacademica.security.UsuarioPrincipal;
import br.ucsal.gestaoacademica.usuario.Usuario;
import br.ucsal.gestaoacademica.usuario.UsuarioRepository;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditoriaService {

    private final LogAuditoriaRepository logAuditoriaRepository;
    private final LogSegurancaRepository logSegurancaRepository;
    private final UsuarioRepository usuarioRepository;

    public AuditoriaService(LogAuditoriaRepository logAuditoriaRepository,
            LogSegurancaRepository logSegurancaRepository,
            UsuarioRepository usuarioRepository) {
        this.logAuditoriaRepository = logAuditoriaRepository;
        this.logSegurancaRepository = logSegurancaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarAuditoria(String acao, String entidade, Long entidadeId, String detalhe) {
        LogAuditoria log = new LogAuditoria();
        log.setUsuario(usuarioAtual().orElse(null));
        log.setAcao(acao);
        log.setEntidade(entidade);
        log.setEntidadeId(entidadeId);
        log.setDetalhe(truncar(detalhe, 1000));
        log.setCriadoEm(LocalDateTime.now());
        logAuditoriaRepository.save(log);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarEvento(EventoSeguranca evento, Usuario usuario, String ip) {
        LogSeguranca log = new LogSeguranca();
        Usuario referencia = null;
        if (usuario != null && usuario.getId() != null) {
            referencia = usuarioRepository.getReferenceById(usuario.getId());
        } else {
            referencia = usuarioAtual().orElse(null);
        }
        log.setUsuario(referencia);
        log.setEvento(evento);
        log.setIp(truncar(ip, 60));
        log.setCriadoEm(LocalDateTime.now());
        logSegurancaRepository.save(log);
    }

    @Transactional(readOnly = true)
    public PageResponse<LogAuditoriaResponse> listarAuditoria(Pageable pageable) {
        Page<LogAuditoria> pagina = logAuditoriaRepository.findAllByOrderByCriadoEmDesc(pageable);
        return PageResponse.from(pagina, LogAuditoriaResponse::from);
    }

    @Transactional(readOnly = true)
    public PageResponse<LogSegurancaResponse> listarSeguranca(Pageable pageable) {
        Page<LogSeguranca> pagina = logSegurancaRepository.findAllByOrderByCriadoEmDesc(pageable);
        return PageResponse.from(pagina, LogSegurancaResponse::from);
    }

    private java.util.Optional<Usuario> usuarioAtual() {
        try {
            UsuarioPrincipal principal = SecurityUtils.principal();
            if (principal == null || principal.getId() == null) {
                return java.util.Optional.empty();
            }
            return usuarioRepository.findById(principal.getId());
        } catch (RuntimeException ex) {
            return java.util.Optional.empty();
        }
    }

    private String truncar(String valor, int maximo) {
        if (valor == null) {
            return null;
        }
        return valor.length() <= maximo ? valor : valor.substring(0, maximo);
    }

    public record LogAuditoriaResponse(Long id, String usuario, String acao, String entidade,
            Long entidadeId, String detalhe, LocalDateTime criadoEm) {

        static LogAuditoriaResponse from(LogAuditoria log) {
            return new LogAuditoriaResponse(log.getId(),
                    log.getUsuario() == null ? null : log.getUsuario().getNome(),
                    log.getAcao(), log.getEntidade(), log.getEntidadeId(),
                    log.getDetalhe(), log.getCriadoEm());
        }
    }

    public record LogSegurancaResponse(Long id, String usuario, EventoSeguranca evento, String ip,
            LocalDateTime criadoEm) {

        static LogSegurancaResponse from(LogSeguranca log) {
            return new LogSegurancaResponse(log.getId(),
                    log.getUsuario() == null ? null : log.getUsuario().getNome(),
                    log.getEvento(), log.getIp(), log.getCriadoEm());
        }
    }
}
