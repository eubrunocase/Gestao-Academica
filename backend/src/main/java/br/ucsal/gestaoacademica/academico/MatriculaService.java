package br.ucsal.gestaoacademica.academico;

import br.ucsal.gestaoacademica.auditoria.AuditoriaService;
import br.ucsal.gestaoacademica.common.BusinessException;
import br.ucsal.gestaoacademica.common.ErroMatricula;
import br.ucsal.gestaoacademica.common.MatriculaException;
import br.ucsal.gestaoacademica.usuario.Aluno;
import br.ucsal.gestaoacademica.usuario.AlunoRepository;
import br.ucsal.gestaoacademica.usuario.PerfilUsuario;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final TurmaRepository turmaRepository;
    private final AlunoRepository alunoRepository;
    private final CalendarioAcademicoRepository calendarioRepository;
    private final AuditoriaService auditoriaService;

    public MatriculaService(MatriculaRepository matriculaRepository, TurmaRepository turmaRepository,
            AlunoRepository alunoRepository,
            CalendarioAcademicoRepository calendarioRepository,
            AuditoriaService auditoriaService) {
        this.matriculaRepository = matriculaRepository;
        this.turmaRepository = turmaRepository;
        this.alunoRepository = alunoRepository;
        this.calendarioRepository = calendarioRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<TurmaDisponivelResponse> turmasDisponiveis(Long alunoId) {
        Aluno aluno = obterAluno(alunoId);
        return calendarioAberto().map(calendario -> turmaRepository
                .findByPeriodoLetivoAndEncerradaFalse(calendario.getPeriodoLetivo()).stream()
                .filter(turma -> prerequisitosAtendidos(aluno.getId(), turma.getDisciplina())
                        .isEmpty())
                .filter(turma -> !matriculaRepository.existsByAlunoIdAndTurmaIdAndStatusNot(
                        aluno.getId(), turma.getId(), StatusMatricula.CANCELADA))
                .filter(turma -> matriculaRepository.countByTurmaIdAndStatus(
                        turma.getId(), StatusMatricula.ATIVA) < turma.getLimiteVagas())
                .map(this::toDisponivel)
                .toList())
                .orElseGet(List::of);
    }

    @Transactional
    public MatriculaResponse matricular(Long alunoId, Long turmaId) {
        Aluno aluno = obterAluno(alunoId);
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "Turma nao encontrada"));

        validarPeriodoDeMatricula(turma);
        if (turma.isEncerrada()) {
            throw new MatriculaException(MatriculaException.Motivo.TURMA_ENCERRADA,
                    "Turma encerrada para novas matriculas", List.of(), List.of());
        }

        Matricula existente = matriculaRepository
                .findByAlunoIdAndTurmaId(aluno.getId(), turma.getId()).orElse(null);
        if (existente != null && existente.getStatus() != StatusMatricula.CANCELADA) {
            throw new MatriculaException(MatriculaException.Motivo.JA_MATRICULADO,
                    "Voce ja possui matricula nesta turma", List.of(), List.of());
        }

        List<ErroMatricula.DisciplinaPendente> pendentes =
                prerequisitosAtendidos(aluno.getId(), turma.getDisciplina());
        if (!pendentes.isEmpty()) {
            throw new MatriculaException(MatriculaException.Motivo.PREREQUISITO,
                    "Pre-requisitos nao concluidos: "
                            + String.join(", ", pendentes.stream()
                                    .map(ErroMatricula.DisciplinaPendente::nome).toList()),
                    pendentes, List.of());
        }

        long ocupadas = matriculaRepository.countByTurmaIdAndStatus(
                turma.getId(), StatusMatricula.ATIVA);
        if (ocupadas >= turma.getLimiteVagas()) {
            throw new MatriculaException(MatriculaException.Motivo.SEM_VAGA,
                    "Turma sem vagas disponiveis", List.of(),
                    turmasAlternativas(turma));
        }

        Matricula matricula;
        if (existente != null) {
            matricula = existente;
            matricula.setData(LocalDate.now());
            matricula.setStatus(StatusMatricula.ATIVA);
        } else {
            matricula = new Matricula();
            matricula.setAluno(aluno);
            matricula.setTurma(turma);
            matricula.setData(LocalDate.now());
            matricula.setStatus(StatusMatricula.ATIVA);
        }
        matriculaRepository.save(matricula);
        auditoriaService.registrarAuditoria("MATRICULAR", "Matricula", matricula.getId(),
                turma.getDisciplina().getNome() + " / " + turma.getPeriodoLetivo());
        return MatriculaResponse.from(matricula);
    }

    @Transactional
    public void cancelar(Long matriculaId, Long usuarioId, PerfilUsuario perfil) {
        Matricula matricula = matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "Matricula nao encontrada"));
        if (perfil == PerfilUsuario.ALUNO
                && !matricula.getAluno().getId().equals(usuarioId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Voce so pode cancelar suas proprias matriculas");
        }
        if (matricula.getStatus() != StatusMatricula.ATIVA) {
            throw new BusinessException("Somente matriculas ativas podem ser canceladas");
        }
        validarPeriodoDeMatricula(matricula.getTurma());
        matricula.setStatus(StatusMatricula.CANCELADA);
        matriculaRepository.save(matricula);
        auditoriaService.registrarAuditoria("CANCELAR", "Matricula", matriculaId,
                matricula.getTurma().getDisciplina().getNome());
    }

    @Transactional(readOnly = true)
    public List<MatriculaResponse> listarDoAluno(Long alunoId) {
        return matriculaRepository.findByAlunoIdOrderByTurmaPeriodoLetivoDesc(alunoId).stream()
                .map(MatriculaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Matricula obter(Long id) {
        return matriculaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "Matricula nao encontrada"));
    }

    private void validarPeriodoDeMatricula(Turma turma) {
        CalendarioAcademico calendario = calendarioRepository
                .findByPeriodoLetivo(turma.getPeriodoLetivo())
                .orElseThrow(() -> new MatriculaException(MatriculaException.Motivo.FORA_DO_PRAZO,
                        "Nao ha calendario para o periodo da turma", List.of(), List.of()));
        LocalDate hoje = LocalDate.now();
        if (hoje.isBefore(calendario.getInicioMatricula())
                || hoje.isAfter(calendario.getFimMatricula())) {
            throw new MatriculaException(MatriculaException.Motivo.FORA_DO_PRAZO,
                    "Fora do periodo de matricula de " + turma.getPeriodoLetivo(),
                    List.of(), List.of());
        }
    }

    private java.util.Optional<CalendarioAcademico> calendarioAberto() {
        LocalDate hoje = LocalDate.now();
        return calendarioRepository
                .findByInicioMatriculaLessThanEqualAndFimMatriculaGreaterThanEqual(hoje, hoje);
    }

    private List<ErroMatricula.DisciplinaPendente> prerequisitosAtendidos(
            Long alunoId, Disciplina disciplina) {
        List<ErroMatricula.DisciplinaPendente> pendentes = new ArrayList<>();
        for (Disciplina prerequisito : disciplina.getPrerequisitos()) {
            boolean concluido = matriculaRepository
                    .existsByAlunoIdAndTurmaDisciplinaIdAndStatus(
                            alunoId, prerequisito.getId(), StatusMatricula.APROVADO);
            if (!concluido) {
                pendentes.add(new ErroMatricula.DisciplinaPendente(
                        prerequisito.getId(), prerequisito.getNome()));
            }
        }
        return pendentes;
    }

    private List<ErroMatricula.TurmaAlternativa> turmasAlternativas(Turma turma) {
        return turmaRepository.findByDisciplinaIdAndPeriodoLetivoAndEncerradaFalse(
                        turma.getDisciplina().getId(), turma.getPeriodoLetivo()).stream()
                .filter(alternativa -> !alternativa.getId().equals(turma.getId()))
                .map(alternativa -> {
                    long ocupadas = matriculaRepository.countByTurmaIdAndStatus(
                            alternativa.getId(), StatusMatricula.ATIVA);
                    long disponiveis = alternativa.getLimiteVagas() - ocupadas;
                    return new Object[] {alternativa, disponiveis};
                })
                .filter(item -> (long) item[1] > 0)
                .map(item -> {
                    Turma alternativa = (Turma) item[0];
                    return new ErroMatricula.TurmaAlternativa(
                            alternativa.getId(),
                            alternativa.getDisciplina().getNome(),
                            alternativa.getProfessor().getNome(),
                            alternativa.getPeriodoLetivo(),
                            (long) item[1]);
                })
                .toList();
    }

    private TurmaDisponivelResponse toDisponivel(Turma turma) {
        long ocupadas = matriculaRepository.countByTurmaIdAndStatus(
                turma.getId(), StatusMatricula.ATIVA);
        return new TurmaDisponivelResponse(turma.getId(),
                turma.getDisciplina().getId(),
                turma.getDisciplina().getNome(),
                turma.getDisciplina().getCurso().getNome(),
                turma.getProfessor().getNome(),
                turma.getPeriodoLetivo(),
                turma.getDisciplina().getCargaHoraria(),
                turma.getLimiteVagas(),
                ocupadas,
                Math.max(0, turma.getLimiteVagas() - ocupadas));
    }

    private Aluno obterAluno(Long alunoId) {
        return alunoRepository.findById(alunoId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Aluno nao encontrado"));
    }
}
