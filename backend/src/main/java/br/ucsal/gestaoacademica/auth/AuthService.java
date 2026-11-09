package br.ucsal.gestaoacademica.auth;

import br.ucsal.gestaoacademica.auditoria.AuditoriaService;
import br.ucsal.gestaoacademica.auditoria.EventoSeguranca;
import br.ucsal.gestaoacademica.common.BusinessException;
import br.ucsal.gestaoacademica.config.AcademicProperties;
import br.ucsal.gestaoacademica.security.JwtService;
import br.ucsal.gestaoacademica.security.SecurityUtils;
import br.ucsal.gestaoacademica.usuario.Aluno;
import br.ucsal.gestaoacademica.usuario.Professor;
import br.ucsal.gestaoacademica.usuario.Usuario;
import br.ucsal.gestaoacademica.usuario.UsuarioRepository;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditoriaService auditoriaService;
    private final AcademicProperties propriedades;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
            JwtService jwtService, AuditoriaService auditoriaService,
            AcademicProperties propriedades) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditoriaService = auditoriaService;
        this.propriedades = propriedades;
    }

    /**
     * As alteracoes de tentativas/bloqueio precisam ser persistidas mesmo quando
     * o login falha; por isso a excecao de negocio nao causa rollback.
     */
    @Transactional(noRollbackFor = BusinessException.class)
    public LoginResponse login(LoginRequest request, String ip) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(request.email()).orElse(null);
        if (usuario == null) {
            auditoriaService.registrarEvento(EventoSeguranca.LOGIN_FALHA, null, ip);
            throw credenciaisInvalidas();
        }
        if (!usuario.isAtivo()) {
            auditoriaService.registrarEvento(EventoSeguranca.LOGIN_FALHA, usuario, ip);
            throw credenciaisInvalidas();
        }

        LocalDateTime agora = LocalDateTime.now();
        if (usuario.getBloqueadoAte() != null) {
            if (usuario.getBloqueadoAte().isAfter(agora)) {
                auditoriaService.registrarEvento(EventoSeguranca.BLOQUEIO, usuario, ip);
                throw new BusinessException(HttpStatus.LOCKED,
                        "Conta temporariamente bloqueada. Tente novamente mais tarde.");
            }
            usuario.setBloqueadoAte(null);
            usuario.setTentativasFalhas(0);
        }

        if (!passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
            registrarFalha(usuario, ip);
            throw credenciaisInvalidas();
        }

        usuario.setTentativasFalhas(0);
        usuario.setBloqueadoAte(null);
        usuarioRepository.save(usuario);
        auditoriaService.registrarEvento(EventoSeguranca.LOGIN_SUCESSO, usuario, ip);

        return new LoginResponse(
                jwtService.gerarToken(usuario),
                "Bearer",
                jwtService.getExpiracaoSegundos(),
                new LoginResponse.UsuarioResumo(usuario.getId(), usuario.getNome(),
                        usuario.getEmail(), usuario.getPerfil()));
    }

    @Transactional(readOnly = true)
    public MeResponse me() {
        Long id = SecurityUtils.usuarioId();
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Nao autenticado"));
        String numeroMatricula = usuario instanceof Aluno aluno ? aluno.getNumeroMatricula() : null;
        String registroFuncional = usuario instanceof Professor professor
                ? professor.getRegistroFuncional() : null;
        return new MeResponse(usuario.getId(), usuario.getNome(), usuario.getDocumento(),
                usuario.getEmail(), usuario.getPerfil(), usuario.isAtivo(),
                numeroMatricula, registroFuncional);
    }

    private void registrarFalha(Usuario usuario, String ip) {
        usuario.setTentativasFalhas(usuario.getTentativasFalhas() + 1);
        if (usuario.getTentativasFalhas() >= propriedades.getLoginMaxTentativas()) {
            usuario.setBloqueadoAte(LocalDateTime.now()
                    .plusMinutes(propriedades.getLoginBloqueioMinutos()));
            usuarioRepository.save(usuario);
            auditoriaService.registrarEvento(EventoSeguranca.BLOQUEIO, usuario, ip);
        } else {
            usuarioRepository.save(usuario);
        }
        auditoriaService.registrarEvento(EventoSeguranca.LOGIN_FALHA, usuario, ip);
    }

    private BusinessException credenciaisInvalidas() {
        return new BusinessException(HttpStatus.UNAUTHORIZED, "Credenciais invalidas");
    }
}
