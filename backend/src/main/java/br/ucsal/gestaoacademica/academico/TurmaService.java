package br.ucsal.gestaoacademica.academico;

import br.ucsal.gestaoacademica.auditoria.AuditoriaService;
import br.ucsal.gestaoacademica.avaliacao.Avaliacao;
import br.ucsal.gestaoacademica.avaliacao.AvaliacaoRepository;
import br.ucsal.gestaoacademica.avaliacao.CalculoAcademico;
import br.ucsal.gestaoacademica.avaliacao.Nota;
import br.ucsal.gestaoacademica.avaliacao.NotaRepository;
import br.ucsal.gestaoacademica.common.BusinessException;
import br.ucsal.gestaoacademica.config.AcademicProperties;
import br.ucsal.gestaoacademica.frequencia.AulaRepository;
import br.ucsal.gestaoacademica.frequencia.RegistroFrequencia;
import br.ucsal.gestaoacademica.frequencia.RegistroFrequenciaRepository;
import br.ucsal.gestaoacademica.usuario.PerfilUsuario;
import br.ucsal.gestaoacademica.usuario.Professor;
import br.ucsal.gestaoacademica.usuario.ProfessorRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final ProfessorRepository professorRepository;
    private final CalendarioAcademicoRepository calendarioRepository;
    private final MatriculaRepository matriculaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final NotaRepository notaRepository;
    private final AulaRepository aulaRepository;
    private final RegistroFrequenciaRepository frequenciaRepository;
    private final AuditoriaService auditoriaService;
    private final AcademicProperties propriedades;

    public TurmaService(TurmaRepository turmaRepository, DisciplinaRepository disciplinaRepository,
            ProfessorRepository professorRepository,
            CalendarioAcademicoRepository calendarioRepository,
            MatriculaRepository matriculaRepository, AvaliacaoRepository avaliacaoRepository,
            NotaRepository notaRepository, AulaRepository aulaRepository,
            RegistroFrequenciaRepository frequenciaRepository, AuditoriaService auditoriaService,
            AcademicProperties propriedades) {
        this.turmaRepository = turmaRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.professorRepository = professorRepository;
        this.calendarioRepository = calendarioRepository;
        this.matriculaRepository = matriculaRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.notaRepository = notaRepository;
        this.aulaRepository = aulaRepository;
        this.frequenciaRepository = frequenciaRepository;
        this.auditoriaService = auditoriaService;
        this.propriedades = propriedades;
    }

    @Transactional(readOnly = true)
    public List<TurmaResponse> listar(String periodoLetivo, Long professorId) {
        return turmaRepository.findAllByOrderByPeriodoLetivoDescIdAsc().stream()
                .filter(turma -> periodoLetivo == null || periodoLetivo.isBlank()
                        || turma.getPeriodoLetivo().equals(periodoLetivo))
                .filter(turma -> professorId == null
                        || turma.getProfessor().getId().equals(professorId))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TurmaResponse buscar(Long id) {
        return toResponse(obter(id));
    }

    @Transactional(readOnly = true)
    public List<TurmaResponse> listarDoProfessor(Long professorId) {
        return turmaRepository.findByProfessorIdOrderByPeriodoLetivoDesc(professorId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AlunoTurmaResponse> listarAlunos(Long turmaId, Long usuarioId,
            PerfilUsuario perfil) {
        Turma turma = validarAcessoGestao(turmaId, usuarioId, perfil);
        return matriculaRepository.findByTurmaIdOrderByAlunoNomeAsc(turma.getId()).stream()
                .filter(matricula -> matricula.getStatus() != StatusMatricula.CANCELADA)
                .map(matricula -> new AlunoTurmaResponse(
                        matricula.getId(),
                        matricula.getAluno().getId(),
                        matricula.getAluno().getNome(),
                        matricula.getAluno().getNumeroMatricula(),
                        matricula.getAluno().getEmail(),
                        matricula.getStatus()))
                .toList();
    }

    @Transactional
    public TurmaResponse criar(TurmaRequest request) {
        Disciplina disciplina = disciplinaRepository.findById(request.disciplinaId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "Disciplina nao encontrada"));
        Professor professor = professorRepository.findById(request.professorId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "Professor nao encontrado"));
        if (calendarioRepository.findByPeriodoLetivo(request.periodoLetivo()).isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Nao existe calendario para o periodo letivo informado");
        }
        Turma turma = new Turma();
        turma.setDisciplina(disciplina);
        turma.setProfessor(professor);
        turma.setPeriodoLetivo(request.periodoLetivo());
        turma.setLimiteVagas(request.limiteVagas());
        turma.setEncerrada(false);
        turmaRepository.save(turma);
        auditoriaService.registrarAuditoria("CRIAR", "Turma", turma.getId(),
                disciplina.getNome() + " / " + request.periodoLetivo());
        return toResponse(turma);
    }

    @Transactional
    public TurmaResponse atualizar(Long id, TurmaRequest request) {
        Turma turma = obter(id);
        long ocupadas = matriculaRepository.countByTurmaIdAndStatus(id, StatusMatricula.ATIVA);
        if (request.limiteVagas() < ocupadas) {
            throw new BusinessException("Limite de vagas menor que o numero de alunos matriculados");
        }
        Disciplina disciplina = disciplinaRepository.findById(request.disciplinaId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "Disciplina nao encontrada"));
        Professor professor = professorRepository.findById(request.professorId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "Professor nao encontrado"));
        if (calendarioRepository.findByPeriodoLetivo(request.periodoLetivo()).isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Nao existe calendario para o periodo letivo informado");
        }
        turma.setDisciplina(disciplina);
        turma.setProfessor(professor);
        turma.setPeriodoLetivo(request.periodoLetivo());
        turma.setLimiteVagas(request.limiteVagas());
        turmaRepository.save(turma);
        auditoriaService.registrarAuditoria("ATUALIZAR", "Turma", id, null);
        return toResponse(turma);
    }

    @Transactional
    public TurmaResponse encerrar(Long id, Long usuarioId, PerfilUsuario perfil) {
        Turma turma = validarAcessoGestao(id, usuarioId, perfil);
        turma.setEncerrada(true);

        List<Avaliacao> avaliacoes =
                avaliacaoRepository.findByTurmaIdOrderByDescricaoAsc(turma.getId());
        Map<Long, List<Nota>> notasPorMatricula = new HashMap<>();
        for (Nota nota : notaRepository.findByAvaliacaoTurmaId(turma.getId())) {
            notasPorMatricula.computeIfAbsent(nota.getMatricula().getId(), k -> new java.util.ArrayList<>())
                    .add(nota);
        }
        Map<Long, long[]> frequencias = new HashMap<>();
        for (RegistroFrequencia registro : frequenciaRepository.findByAulaTurmaId(turma.getId())) {
            long[] contagem = frequencias.computeIfAbsent(registro.getMatricula().getId(),
                    k -> new long[2]);
            if (registro.isPresente()) {
                contagem[0]++;
            }
            contagem[1]++;
        }

        for (Matricula matricula : matriculaRepository.findByTurmaIdAndStatus(
                turma.getId(), StatusMatricula.ATIVA)) {
            List<Nota> notas = notasPorMatricula.getOrDefault(matricula.getId(), List.of());
            BigDecimal media = CalculoAcademico.media(avaliacoes,
                    CalculoAcademico.indexarPorAvaliacao(notas));
            long[] contagem = frequencias.get(matricula.getId());
            BigDecimal frequencia = contagem == null ? null
                    : CalculoAcademico.frequencia(contagem[0], contagem[1]);
            matricula.setStatus(CalculoAcademico.situacao(media, frequencia, propriedades));
        }

        turmaRepository.save(turma);
        auditoriaService.registrarAuditoria("ENCERRAR", "Turma", id,
                "Encerrada em " + LocalDateTime.now());
        return toResponse(turma);
    }

    @Transactional
    public void excluir(Long id) {
        Turma turma = obter(id);
        if (matriculaRepository.existsByTurmaId(id)) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "Turma possui matriculas e nao pode ser excluida");
        }
        if (avaliacaoRepository.existsByTurmaId(id) || aulaRepository.existsByTurmaId(id)) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "Turma possui avaliacoes ou aulas e nao pode ser excluida");
        }
        turmaRepository.delete(turma);
        auditoriaService.registrarAuditoria("EXCLUIR", "Turma", id, turma.getDisciplina().getNome());
    }

    /**
     * RN-003 / RN-009: professor so acessa as turmas em que e o responsavel.
     * Coordenacao, secretaria e admin possuem acesso amplo.
     */
    @Transactional(readOnly = true)
    public Turma validarAcessoGestao(Long turmaId, Long usuarioId, PerfilUsuario perfil) {
        Turma turma = obter(turmaId);
        if (perfil == PerfilUsuario.PROFESSOR
                && !turma.getProfessor().getId().equals(usuarioId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Voce so pode acessar turmas em que e o professor responsavel");
        }
        if (perfil == PerfilUsuario.ALUNO) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Acesso nao permitido para alunos");
        }
        return turma;
    }

    @Transactional(readOnly = true)
    public Turma obter(Long id) {
        return turmaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Turma nao encontrada"));
    }

    private TurmaResponse toResponse(Turma turma) {
        long ocupadas = matriculaRepository.countByTurmaIdAndStatus(
                turma.getId(), StatusMatricula.ATIVA);
        return TurmaResponse.of(turma, ocupadas);
    }

    public record AlunoTurmaResponse(Long matriculaId, Long alunoId, String nome,
            String numeroMatricula, String email, StatusMatricula status) {
    }
}
