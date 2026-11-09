package br.ucsal.gestaoacademica.comunicacao;

import br.ucsal.gestaoacademica.academico.Curso;
import br.ucsal.gestaoacademica.academico.CursoRepository;
import br.ucsal.gestaoacademica.academico.Matricula;
import br.ucsal.gestaoacademica.academico.MatriculaRepository;
import br.ucsal.gestaoacademica.academico.StatusMatricula;
import br.ucsal.gestaoacademica.academico.Turma;
import br.ucsal.gestaoacademica.academico.TurmaRepository;
import br.ucsal.gestaoacademica.auditoria.AuditoriaService;
import br.ucsal.gestaoacademica.common.BusinessException;
import br.ucsal.gestaoacademica.usuario.PerfilUsuario;
import br.ucsal.gestaoacademica.usuario.UsuarioRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AvisoService {

    private final AvisoRepository avisoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TurmaRepository turmaRepository;
    private final CursoRepository cursoRepository;
    private final MatriculaRepository matriculaRepository;
    private final AuditoriaService auditoriaService;

    public AvisoService(AvisoRepository avisoRepository, UsuarioRepository usuarioRepository,
            TurmaRepository turmaRepository, CursoRepository cursoRepository,
            MatriculaRepository matriculaRepository, AuditoriaService auditoriaService) {
        this.avisoRepository = avisoRepository;
        this.usuarioRepository = usuarioRepository;
        this.turmaRepository = turmaRepository;
        this.cursoRepository = cursoRepository;
        this.matriculaRepository = matriculaRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public AvisoResponse criar(AvisoRequest request, Long usuarioId, PerfilUsuario perfil) {
        Aviso aviso = new Aviso();
        aviso.setAutor(usuarioRepository.getReferenceById(usuarioId));
        aviso.setTitulo(request.titulo());
        aviso.setMensagem(request.mensagem());
        aviso.setEscopo(request.escopo());

        switch (request.escopo()) {
            case TURMA -> {
                if (request.turmaId() == null) {
                    throw new BusinessException("Informe a turma do aviso");
                }
                Turma turma = turmaRepository.findById(request.turmaId())
                        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                                "Turma nao encontrada"));
                if (perfil == PerfilUsuario.PROFESSOR
                        && !turma.getProfessor().getId().equals(usuarioId)) {
                    throw new BusinessException(HttpStatus.FORBIDDEN,
                            "Professor so pode publicar avisos em suas turmas");
                }
                aviso.setTurma(turma);
            }
            case CURSO -> {
                if (perfil == PerfilUsuario.PROFESSOR) {
                    throw new BusinessException(HttpStatus.FORBIDDEN,
                            "Professor so pode publicar avisos para suas turmas");
                }
                if (request.cursoId() == null) {
                    throw new BusinessException("Informe o curso do aviso");
                }
                Curso curso = cursoRepository.findById(request.cursoId())
                        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                                "Curso nao encontrado"));
                aviso.setCurso(curso);
            }
            case GERAL -> {
                if (perfil == PerfilUsuario.PROFESSOR) {
                    throw new BusinessException(HttpStatus.FORBIDDEN,
                            "Professor so pode publicar avisos para suas turmas");
                }
            }
        }

        avisoRepository.save(aviso);
        auditoriaService.registrarAuditoria("CRIAR", "Aviso", aviso.getId(), request.titulo());
        return AvisoResponse.from(aviso);
    }

    @Transactional(readOnly = true)
    public List<AvisoResponse> listar(Long usuarioId, PerfilUsuario perfil) {
        List<Aviso> avisos = avisoRepository.findAllByOrderByCriadoEmDesc();
        if (perfil == PerfilUsuario.ADMIN || perfil == PerfilUsuario.SECRETARIA
                || perfil == PerfilUsuario.COORDENADOR) {
            return avisos.stream().map(AvisoResponse::from).toList();
        }

        Set<Long> turmaIds = new HashSet<>();
        Set<Long> cursoIds = new HashSet<>();
        if (perfil == PerfilUsuario.ALUNO) {
            for (Matricula matricula : matriculaRepository.findByAlunoIdAndStatus(
                    usuarioId, StatusMatricula.ATIVA)) {
                turmaIds.add(matricula.getTurma().getId());
                cursoIds.add(matricula.getTurma().getDisciplina().getCurso().getId());
            }
        } else if (perfil == PerfilUsuario.PROFESSOR) {
            for (Turma turma : turmaRepository.findByProfessorIdOrderByPeriodoLetivoDesc(usuarioId)) {
                turmaIds.add(turma.getId());
                cursoIds.add(turma.getDisciplina().getCurso().getId());
            }
        }

        return avisos.stream()
                .filter(aviso -> switch (aviso.getEscopo()) {
                    case GERAL -> true;
                    case TURMA -> aviso.getTurma() != null
                            && turmaIds.contains(aviso.getTurma().getId());
                    case CURSO -> aviso.getCurso() != null
                            && cursoIds.contains(aviso.getCurso().getId());
                })
                .map(AvisoResponse::from)
                .toList();
    }
}
