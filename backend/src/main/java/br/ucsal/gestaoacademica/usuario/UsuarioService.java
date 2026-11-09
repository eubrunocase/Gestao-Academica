package br.ucsal.gestaoacademica.usuario;

import br.ucsal.gestaoacademica.auditoria.AuditoriaService;
import br.ucsal.gestaoacademica.common.BusinessException;
import br.ucsal.gestaoacademica.common.PageResponse;
import br.ucsal.gestaoacademica.security.SecurityUtils;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AlunoRepository alunoRepository;
    private final ProfessorRepository professorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public UsuarioService(UsuarioRepository usuarioRepository, AlunoRepository alunoRepository,
            ProfessorRepository professorRepository, PasswordEncoder passwordEncoder,
            AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.alunoRepository = alunoRepository;
        this.professorRepository = professorRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public PageResponse<UsuarioResponse> listar(PerfilUsuario perfil, String nome, Pageable pageable) {
        Page<Usuario> pagina;
        if (nome != null && !nome.isBlank()) {
            pagina = usuarioRepository.findByNomeContainingIgnoreCase(nome.trim(), pageable);
        } else if (perfil != null) {
            pagina = usuarioRepository.findByPerfil(perfil, pageable);
        } else {
            pagina = usuarioRepository.findAll(pageable);
        }
        return PageResponse.from(pagina, UsuarioResponse::from);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscar(Long id) {
        return UsuarioResponse.from(obter(id));
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        if (usuarioRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException(HttpStatus.CONFLICT, "E-mail ja cadastrado");
        }
        if (usuarioRepository.existsByDocumento(request.documento())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Documento ja cadastrado");
        }

        Usuario usuario = switch (request.perfil()) {
            case ALUNO -> {
                if (request.numeroMatricula() == null || request.numeroMatricula().isBlank()) {
                    throw new BusinessException("Informe o numero de matricula do aluno");
                }
                if (alunoRepository.existsByNumeroMatricula(request.numeroMatricula())) {
                    throw new BusinessException(HttpStatus.CONFLICT, "Numero de matricula ja cadastrado");
                }
                Aluno aluno = new Aluno();
                aluno.setNumeroMatricula(request.numeroMatricula());
                yield aluno;
            }
            case PROFESSOR -> {
                if (request.registroFuncional() == null || request.registroFuncional().isBlank()) {
                    throw new BusinessException("Informe o registro funcional do professor");
                }
                if (professorRepository.existsByRegistroFuncional(request.registroFuncional())) {
                    throw new BusinessException(HttpStatus.CONFLICT, "Registro funcional ja cadastrado");
                }
                Professor professor = new Professor();
                professor.setRegistroFuncional(request.registroFuncional());
                yield professor;
            }
            default -> new Usuario();
        };

        usuario.setNome(request.nome());
        usuario.setDocumento(request.documento());
        usuario.setEmail(request.email());
        usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        usuario.setPerfil(request.perfil());
        usuario.setAtivo(request.ativo() == null || request.ativo());
        usuarioRepository.save(usuario);

        auditoriaService.registrarAuditoria("CRIAR", "Usuario", usuario.getId(),
                "Perfil " + usuario.getPerfil());
        return UsuarioResponse.from(usuario);
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = obter(id);

        usuarioRepository.findByEmailIgnoreCase(request.email())
                .filter(outro -> !Objects.equals(outro.getId(), id))
                .ifPresent(outro -> {
                    throw new BusinessException(HttpStatus.CONFLICT, "E-mail ja cadastrado");
                });
        usuarioRepository.findByDocumento(request.documento())
                .filter(outro -> !Objects.equals(outro.getId(), id))
                .ifPresent(outro -> {
                    throw new BusinessException(HttpStatus.CONFLICT, "Documento ja cadastrado");
                });

        usuario.setNome(request.nome());
        usuario.setDocumento(request.documento());
        usuario.setEmail(request.email());
        if (request.senha() != null && !request.senha().isBlank()) {
            usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        }
        if (request.ativo() != null) {
            if (!request.ativo() && Objects.equals(id, SecurityUtils.usuarioId())) {
                throw new BusinessException("Nao e permitido desativar o proprio usuario");
            }
            usuario.setAtivo(request.ativo());
        }
        if (usuario instanceof Aluno aluno && request.numeroMatricula() != null
                && !request.numeroMatricula().isBlank()
                && !request.numeroMatricula().equals(aluno.getNumeroMatricula())) {
            if (alunoRepository.existsByNumeroMatricula(request.numeroMatricula())) {
                throw new BusinessException(HttpStatus.CONFLICT, "Numero de matricula ja cadastrado");
            }
            aluno.setNumeroMatricula(request.numeroMatricula());
        }
        if (usuario instanceof Professor professor && request.registroFuncional() != null
                && !request.registroFuncional().isBlank()
                && !request.registroFuncional().equals(professor.getRegistroFuncional())) {
            if (professorRepository.existsByRegistroFuncional(request.registroFuncional())) {
                throw new BusinessException(HttpStatus.CONFLICT, "Registro funcional ja cadastrado");
            }
            professor.setRegistroFuncional(request.registroFuncional());
        }

        usuarioRepository.save(usuario);
        auditoriaService.registrarAuditoria("ATUALIZAR", "Usuario", usuario.getId(), null);
        return UsuarioResponse.from(usuario);
    }

    @Transactional
    public UsuarioResponse alterarAtivo(Long id, boolean ativo) {
        Usuario usuario = obter(id);
        if (!ativo && Objects.equals(id, SecurityUtils.usuarioId())) {
            throw new BusinessException("Nao e permitido desativar o proprio usuario");
        }
        usuario.setAtivo(ativo);
        usuarioRepository.save(usuario);
        auditoriaService.registrarAuditoria(ativo ? "ATIVAR" : "DESATIVAR", "Usuario", id, null);
        return UsuarioResponse.from(usuario);
    }

    public Usuario obter(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuario nao encontrado"));
    }
}
