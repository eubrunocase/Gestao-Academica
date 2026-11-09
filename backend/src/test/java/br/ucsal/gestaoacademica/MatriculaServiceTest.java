package br.ucsal.gestaoacademica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.ucsal.gestaoacademica.academico.CalendarioAcademico;
import br.ucsal.gestaoacademica.academico.CalendarioAcademicoRepository;
import br.ucsal.gestaoacademica.academico.Disciplina;
import br.ucsal.gestaoacademica.academico.Matricula;
import br.ucsal.gestaoacademica.academico.MatriculaRepository;
import br.ucsal.gestaoacademica.academico.MatriculaService;
import br.ucsal.gestaoacademica.academico.StatusMatricula;
import br.ucsal.gestaoacademica.academico.Turma;
import br.ucsal.gestaoacademica.academico.TurmaRepository;
import br.ucsal.gestaoacademica.auditoria.AuditoriaService;
import br.ucsal.gestaoacademica.common.MatriculaException;
import br.ucsal.gestaoacademica.usuario.Aluno;
import br.ucsal.gestaoacademica.usuario.AlunoRepository;
import br.ucsal.gestaoacademica.usuario.Professor;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MatriculaServiceTest {

    @Mock
    private MatriculaRepository matriculaRepository;
    @Mock
    private TurmaRepository turmaRepository;
    @Mock
    private AlunoRepository alunoRepository;
    @Mock
    private CalendarioAcademicoRepository calendarioRepository;
    @Mock
    private AuditoriaService auditoriaService;

    private MatriculaService matriculaService;
    private Aluno aluno;
    private Turma turma;
    private CalendarioAcademico calendario;

    @BeforeEach
    void setUp() {
        matriculaService = new MatriculaService(matriculaRepository, turmaRepository,
                alunoRepository, calendarioRepository, auditoriaService);

        aluno = new Aluno();
        aluno.setId(1L);
        aluno.setNome("Aluno Teste");

        Professor professor = new Professor();
        professor.setId(2L);
        professor.setNome("Professor Teste");

        Disciplina disciplina = new Disciplina();
        disciplina.setId(10L);
        disciplina.setNome("Banco de Dados");
        disciplina.setCargaHoraria(60);
        disciplina.setPrerequisitos(new java.util.HashSet<>());

        turma = new Turma();
        turma.setId(20L);
        turma.setDisciplina(disciplina);
        turma.setProfessor(professor);
        turma.setPeriodoLetivo("2026.2");
        turma.setLimiteVagas(2);

        calendario = new CalendarioAcademico();
        calendario.setPeriodoLetivo("2026.2");
        calendario.setInicioMatricula(LocalDate.now().minusDays(5));
        calendario.setFimMatricula(LocalDate.now().plusDays(5));
        calendario.setFimPeriodo(LocalDate.now().plusMonths(2));

        lenient().when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        lenient().when(turmaRepository.findById(20L)).thenReturn(Optional.of(turma));
        lenient().when(calendarioRepository.findByPeriodoLetivo("2026.2"))
                .thenReturn(Optional.of(calendario));
        lenient().when(matriculaRepository.findByAlunoIdAndTurmaId(1L, 20L))
                .thenReturn(Optional.empty());
        lenient().when(matriculaRepository.save(any(Matricula.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void matriculaComSucesso() {
        when(matriculaRepository.countByTurmaIdAndStatus(20L, StatusMatricula.ATIVA))
                .thenReturn(0L);

        var resposta = matriculaService.matricular(1L, 20L);

        assertThat(resposta.status()).isEqualTo(StatusMatricula.ATIVA);
        assertThat(resposta.disciplina()).isEqualTo("Banco de Dados");
        verify(matriculaRepository).save(any(Matricula.class));
    }

    @Test
    void bloqueiaMatriculaSemPrerequisitoConcluido() {
        Disciplina prerequisito = new Disciplina();
        prerequisito.setId(11L);
        prerequisito.setNome("Estrutura de Dados");
        turma.getDisciplina().setPrerequisitos(
                new java.util.HashSet<>(Set.of(prerequisito)));
        when(matriculaRepository.existsByAlunoIdAndTurmaDisciplinaIdAndStatus(
                1L, 11L, StatusMatricula.APROVADO)).thenReturn(false);

        assertThatThrownBy(() -> matriculaService.matricular(1L, 20L))
                .isInstanceOf(MatriculaException.class)
                .satisfies(ex -> {
                    MatriculaException erro = (MatriculaException) ex;
                    assertThat(erro.getMotivo())
                            .isEqualTo(MatriculaException.Motivo.PREREQUISITO);
                    assertThat(erro.getDisciplinasPendentes())
                            .extracting("nome").containsExactly("Estrutura de Dados");
                });
    }

    @Test
    void bloqueiaMatriculaSemVagaESugereAlternativa() {
        when(matriculaRepository.countByTurmaIdAndStatus(20L, StatusMatricula.ATIVA))
                .thenReturn(2L);

        Turma alternativa = new Turma();
        alternativa.setId(21L);
        alternativa.setDisciplina(turma.getDisciplina());
        alternativa.setProfessor(turma.getProfessor());
        alternativa.setPeriodoLetivo("2026.2");
        alternativa.setLimiteVagas(5);
        when(turmaRepository.findByDisciplinaIdAndPeriodoLetivoAndEncerradaFalse(
                10L, "2026.2")).thenReturn(List.of(alternativa));
        when(matriculaRepository.countByTurmaIdAndStatus(21L, StatusMatricula.ATIVA))
                .thenReturn(1L);

        assertThatThrownBy(() -> matriculaService.matricular(1L, 20L))
                .isInstanceOf(MatriculaException.class)
                .satisfies(ex -> {
                    MatriculaException erro = (MatriculaException) ex;
                    assertThat(erro.getMotivo()).isEqualTo(MatriculaException.Motivo.SEM_VAGA);
                    assertThat(erro.getTurmasAlternativas()).hasSize(1);
                    assertThat(erro.getTurmasAlternativas().get(0).vagasDisponiveis())
                            .isEqualTo(4L);
                });
    }

    @Test
    void bloqueiaMatriculaForaDoPrazo() {
        calendario.setInicioMatricula(LocalDate.now().minusDays(30));
        calendario.setFimMatricula(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> matriculaService.matricular(1L, 20L))
                .isInstanceOf(MatriculaException.class)
                .satisfies(ex -> assertThat(((MatriculaException) ex).getMotivo())
                        .isEqualTo(MatriculaException.Motivo.FORA_DO_PRAZO));
    }

    @Test
    void bloqueiaMatriculaDuplicada() {
        Matricula existente = new Matricula();
        existente.setId(99L);
        existente.setAluno(aluno);
        existente.setTurma(turma);
        existente.setStatus(StatusMatricula.ATIVA);
        when(matriculaRepository.findByAlunoIdAndTurmaId(1L, 20L))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> matriculaService.matricular(1L, 20L))
                .isInstanceOf(MatriculaException.class)
                .satisfies(ex -> assertThat(((MatriculaException) ex).getMotivo())
                        .isEqualTo(MatriculaException.Motivo.JA_MATRICULADO));
    }

    @Test
    void reativaMatriculaCancelada() {
        Matricula existente = new Matricula();
        existente.setId(99L);
        existente.setAluno(aluno);
        existente.setTurma(turma);
        existente.setStatus(StatusMatricula.CANCELADA);
        when(matriculaRepository.findByAlunoIdAndTurmaId(1L, 20L))
                .thenReturn(Optional.of(existente));
        when(matriculaRepository.countByTurmaIdAndStatus(20L, StatusMatricula.ATIVA))
                .thenReturn(0L);

        var resposta = matriculaService.matricular(1L, 20L);

        assertThat(resposta.id()).isEqualTo(99L);
        assertThat(resposta.status()).isEqualTo(StatusMatricula.ATIVA);
        verify(matriculaRepository).save(eq(existente));
    }
}
