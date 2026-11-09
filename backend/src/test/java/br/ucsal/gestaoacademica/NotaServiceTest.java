package br.ucsal.gestaoacademica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.ucsal.gestaoacademica.academico.Disciplina;
import br.ucsal.gestaoacademica.academico.Matricula;
import br.ucsal.gestaoacademica.academico.MatriculaRepository;
import br.ucsal.gestaoacademica.academico.StatusMatricula;
import br.ucsal.gestaoacademica.academico.Turma;
import br.ucsal.gestaoacademica.academico.TurmaService;
import br.ucsal.gestaoacademica.auditoria.AuditoriaService;
import br.ucsal.gestaoacademica.avaliacao.Avaliacao;
import br.ucsal.gestaoacademica.avaliacao.AvaliacaoRepository;
import br.ucsal.gestaoacademica.avaliacao.Nota;
import br.ucsal.gestaoacademica.avaliacao.NotaRepository;
import br.ucsal.gestaoacademica.avaliacao.NotaService;
import br.ucsal.gestaoacademica.avaliacao.NotaLancamentoRequest;
import br.ucsal.gestaoacademica.avaliacao.NotasLoteRequest;
import br.ucsal.gestaoacademica.common.BusinessException;
import br.ucsal.gestaoacademica.config.AcademicProperties;
import br.ucsal.gestaoacademica.frequencia.RegistroFrequenciaRepository;
import br.ucsal.gestaoacademica.usuario.Aluno;
import br.ucsal.gestaoacademica.usuario.PerfilUsuario;
import br.ucsal.gestaoacademica.usuario.Professor;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotaServiceTest {

    @Mock
    private NotaRepository notaRepository;
    @Mock
    private AvaliacaoRepository avaliacaoRepository;
    @Mock
    private MatriculaRepository matriculaRepository;
    @Mock
    private RegistroFrequenciaRepository frequenciaRepository;
    @Mock
    private TurmaService turmaService;
    @Mock
    private AuditoriaService auditoriaService;

    private NotaService notaService;
    private AcademicProperties propriedades;
    private Turma turma;
    private Matricula matricula;
    private Avaliacao avaliacao;

    @BeforeEach
    void setUp() {
        propriedades = new AcademicProperties();
        notaService = new NotaService(notaRepository, avaliacaoRepository, matriculaRepository,
                frequenciaRepository, turmaService, auditoriaService, propriedades);

        Professor professor = new Professor();
        professor.setId(2L);
        professor.setNome("Professor Teste");

        Disciplina disciplina = new Disciplina();
        disciplina.setId(10L);
        disciplina.setNome("Estrutura de Dados");

        turma = new Turma();
        turma.setId(20L);
        turma.setDisciplina(disciplina);
        turma.setProfessor(professor);
        turma.setPeriodoLetivo("2026.2");
        turma.setLimiteVagas(10);

        Aluno aluno = new Aluno();
        aluno.setId(1L);
        aluno.setNome("Aluno Teste");

        matricula = new Matricula();
        matricula.setId(30L);
        matricula.setAluno(aluno);
        matricula.setTurma(turma);
        matricula.setStatus(StatusMatricula.ATIVA);

        avaliacao = new Avaliacao();
        avaliacao.setId(40L);
        avaliacao.setTurma(turma);
        avaliacao.setDescricao("Prova 1");
        avaliacao.setPeso(new BigDecimal("2"));
    }

    @Test
    void recusaNotaForaDoIntervalo() {
        when(turmaService.validarAcessoGestao(20L, 2L, PerfilUsuario.PROFESSOR))
                .thenReturn(turma);
        NotasLoteRequest request = new NotasLoteRequest(List.of(
                new NotaLancamentoRequest(40L, 30L, new BigDecimal("11"))));

        assertThatThrownBy(() -> notaService.lancarLote(
                20L, request, 2L, PerfilUsuario.PROFESSOR))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("entre 0 e 10");

        verify(notaRepository, never()).save(any(Nota.class));
    }

    @Test
    void calculaMediaPonderada() {
        Avaliacao prova2 = new Avaliacao();
        prova2.setId(41L);
        prova2.setTurma(turma);
        prova2.setDescricao("Prova 2");
        prova2.setPeso(new BigDecimal("3"));

        Nota nota1 = new Nota();
        nota1.setId(1L);
        nota1.setAvaliacao(avaliacao);
        nota1.setMatricula(matricula);
        nota1.setValor(new BigDecimal("8"));

        Nota nota2 = new Nota();
        nota2.setId(2L);
        nota2.setAvaliacao(prova2);
        nota2.setMatricula(matricula);
        nota2.setValor(new BigDecimal("6"));

        when(avaliacaoRepository.findByTurmaIdOrderByDescricaoAsc(20L))
                .thenReturn(List.of(avaliacao, prova2));
        when(notaRepository.findByMatriculaId(30L)).thenReturn(List.of(nota1, nota2));
        when(frequenciaRepository.findByMatriculaId(30L)).thenReturn(List.of());

        var boletim = notaService.boletim(matricula);

        assertThat(boletim.media()).isEqualByComparingTo("6.80");
        assertThat(boletim.notaNecessaria()).isNull();
        assertThat(boletim.avaliacoes()).hasSize(2);
    }

    @Test
    void calculaNotaNecessariaParaMediaAlvo() {
        Avaliacao prova2 = new Avaliacao();
        prova2.setId(41L);
        prova2.setTurma(turma);
        prova2.setDescricao("Prova 2");
        prova2.setPeso(new BigDecimal("3"));

        Nota nota1 = new Nota();
        nota1.setId(1L);
        nota1.setAvaliacao(avaliacao);
        nota1.setMatricula(matricula);
        nota1.setValor(new BigDecimal("8"));

        when(avaliacaoRepository.findByTurmaIdOrderByDescricaoAsc(20L))
                .thenReturn(List.of(avaliacao, prova2));
        when(notaRepository.findByMatriculaId(30L)).thenReturn(List.of(nota1));
        when(frequenciaRepository.findByMatriculaId(30L)).thenReturn(List.of());

        var boletim = notaService.boletim(matricula);

        // (7 * 5 - 8 * 2) / 3 = 6.33
        assertThat(boletim.notaNecessaria()).isEqualByComparingTo("6.33");
    }

    @Test
    void professorNaoAlteraNotaDeTurmaEncerrada() {
        turma.setEncerrada(true);
        when(turmaService.validarAcessoGestao(20L, 2L, PerfilUsuario.PROFESSOR))
                .thenReturn(turma);
        NotasLoteRequest request = new NotasLoteRequest(List.of(
                new NotaLancamentoRequest(40L, 30L, new BigDecimal("9"))));

        assertThatThrownBy(() -> notaService.lancarLote(
                20L, request, 2L, PerfilUsuario.PROFESSOR))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Turma encerrada");
    }

    @Test
    void coordenacaoAlteraNotaDeTurmaEncerradaEGeraAuditoria() {
        turma.setEncerrada(true);
        when(turmaService.validarAcessoGestao(20L, 3L, PerfilUsuario.COORDENADOR))
                .thenReturn(turma);
        when(avaliacaoRepository.findByIdAndTurmaId(40L, 20L))
                .thenReturn(Optional.of(avaliacao));
        when(matriculaRepository.findById(30L)).thenReturn(Optional.of(matricula));
        when(notaRepository.findByAvaliacaoIdAndMatriculaId(40L, 30L))
                .thenReturn(Optional.empty());
        lenient().when(notaRepository.save(any(Nota.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        NotasLoteRequest request = new NotasLoteRequest(List.of(
                new NotaLancamentoRequest(40L, 30L, new BigDecimal("9"))));

        var notas = notaService.lancarLote(20L, request, 3L, PerfilUsuario.COORDENADOR);

        assertThat(notas).hasSize(1);
        verify(auditoriaService).registrarAuditoria(
                eq("ALTERAR_NOTA_POS_ENCERRAMENTO"), eq("Nota"), any(), anyString());
    }
}
