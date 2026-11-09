package br.ucsal.gestaoacademica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.ucsal.gestaoacademica.auditoria.LogAuditoriaRepository;
import br.ucsal.gestaoacademica.auditoria.LogSegurancaRepository;
import br.ucsal.gestaoacademica.auth.AuthService;
import br.ucsal.gestaoacademica.auth.LoginRequest;
import br.ucsal.gestaoacademica.common.BusinessException;
import br.ucsal.gestaoacademica.usuario.PerfilUsuario;
import br.ucsal.gestaoacademica.usuario.Usuario;
import br.ucsal.gestaoacademica.usuario.UsuarioRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private LogAuditoriaRepository logAuditoriaRepository;
    @Autowired
    private LogSegurancaRepository logSegurancaRepository;

    private Usuario usuario;

    @AfterEach
    void limpar() {
        logAuditoriaRepository.deleteAll();
        logSegurancaRepository.deleteAll();
        if (usuario != null && usuario.getId() != null) {
            usuarioRepository.deleteById(usuario.getId());
        }
    }

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setNome("Usuario Teste");
        usuario.setDocumento("99999999999");
        usuario.setEmail("teste.login@ucsal.br");
        usuario.setSenhaHash(passwordEncoder.encode("Senha@123"));
        usuario.setPerfil(PerfilUsuario.ALUNO);
        usuario.setAtivo(true);
        usuario = usuarioRepository.save(usuario);
    }

    @Test
    void loginComSucessoRetornaToken() {
        var resposta = authService.login(
                new LoginRequest("teste.login@ucsal.br", "Senha@123"), "127.0.0.1");

        assertThat(resposta.token()).isNotBlank();
        assertThat(resposta.tipo()).isEqualTo("Bearer");
        assertThat(resposta.usuario().email()).isEqualTo("teste.login@ucsal.br");
        assertThat(usuarioRepository.findById(usuario.getId()).orElseThrow()
                .getTentativasFalhas()).isZero();
    }

    @Test
    void loginInvalidoIncrementaTentativas() {
        assertThatThrownBy(() -> authService.login(
                new LoginRequest("teste.login@ucsal.br", "errada"), "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));

        assertThat(usuarioRepository.findById(usuario.getId()).orElseThrow()
                .getTentativasFalhas()).isEqualTo(1);
    }

    @Test
    void bloqueiaAposCincoFalhas() {
        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> authService.login(
                    new LoginRequest("teste.login@ucsal.br", "errada"), "127.0.0.1"))
                    .isInstanceOf(BusinessException.class);
        }

        Usuario bloqueado = usuarioRepository.findById(usuario.getId()).orElseThrow();
        assertThat(bloqueado.getTentativasFalhas()).isEqualTo(5);
        assertThat(bloqueado.getBloqueadoAte()).isAfter(LocalDateTime.now());

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("teste.login@ucsal.br", "Senha@123"), "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus())
                        .isEqualTo(HttpStatus.LOCKED));
    }

    @Test
    void emailDesconhecidoRetornaCredenciaisInvalidas() {
        assertThatThrownBy(() -> authService.login(
                new LoginRequest("naoexiste@ucsal.br", "Senha@123"), "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Credenciais invalidas");
    }
}
