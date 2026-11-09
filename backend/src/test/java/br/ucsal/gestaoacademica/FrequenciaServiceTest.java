package br.ucsal.gestaoacademica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import br.ucsal.gestaoacademica.academico.Disciplina;
import br.ucsal.gestaoacademica.academico.Matricula;
import br.ucsal.gestaoacademica.academico.MatriculaRepository;
import br.ucsal.gestaoacademica.academico.StatusMatricula;
import br.ucsal.gestaoacademica.academico.Turma;
import br.ucsal.gestaoacademica.academico.TurmaService;
import br.ucsal.gestaoacademica.auditoria.AuditoriaService;
import br.ucsal.gestaoacademica.config.AcademicProperties;
import br.ucsal.gestaoacademica.frequencia.AulaRepository;
import br.ucsal.gestaoacademica.frequencia.FrequenciaService;
import br.ucsal.gestaoacademica.frequencia.RegistroFrequencia;
import br.ucsal.gestaoacademica.frequencia.RegistroFrequenciaRepository;
import br.ucsal.gestaoacademica.usuario.Aluno;
import br.ucsal.gestaoacademica.usuario.Professor;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FrequenciaServiceTest {

    @Mock
    private AulaRepository aulaRepository;
    @Mock
    private RegistroFrequenciaRepository registroRepository;
    @Mock
    private MatriculaRepository matriculaRepository;
    @Mock
    private TurmaService turmaService;
    @Mock
    private AuditoriaService auditoriaService;

    private FrequenciaService frequenciaService;
    private Matricula matricula;

    @BeforeEach
    void setUp() {
        AcademicProperties propriedades = new AcademicProperties();
        frequenciaService = new FrequenciaService(aulaRepository, registroRepository,
                matriculaRepository, turmaService, auditoriaService, propriedades);

        Professor professor = new Professor();
        professor.setId(2L);
        professor.setNome("Professor Teste");

        Disciplina disciplina = new Disciplina();
        disciplina.setId(10L);
        disciplina.setNome("Estrutura de Dados");

        Turma turma = new Turma();
        turma.setId(20L);
        turma.setDisciplina(disciplina);
        turma.setProfessor(professor);
        turma.setPeriodoLetivo("2026.2");

        Aluno aluno = new Aluno();
        aluno.setId(1L);
        aluno.setNome("Aluno Teste");

        matricula = new Matricula();
        matricula.setId(30L);
        matricula.setAluno(aluno);
        matricula.setTurma(turma);
        matricula.setStatus(StatusMatricula.ATIVA);
    }

    @Test
    void calculaPercentualEAlertaProximoDoLimite() {
        List<RegistroFrequencia> registros = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            RegistroFrequencia registro = new RegistroFrequencia();
            registro.setMatricula(matricula);
            registro.setPresente(i < 7);
            registros.add(registro);
        }
        when(matriculaRepository.findByAlunoIdOrderByTurmaPeriodoLetivoDesc(1L))
                .thenReturn(List.of(matricula));
        when(registroRepository.findByMatriculaId(30L)).thenReturn(registros);

        var respostas = frequenciaService.frequenciasDoAluno(1L);

        assertThat(respostas).hasSize(1);
        assertThat(respostas.get(0).percentual()).isEqualByComparingTo("77.8");
        assertThat(respostas.get(0).presencas()).isEqualTo(7);
        assertThat(respostas.get(0).faltas()).isEqualTo(2);
        assertThat(respostas.get(0).alerta()).isTrue();
    }

    @Test
    void naoAlertaQuandoFrequenciaConfortavel() {
        List<RegistroFrequencia> registros = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            RegistroFrequencia registro = new RegistroFrequencia();
            registro.setMatricula(matricula);
            registro.setPresente(i < 9);
            registros.add(registro);
        }
        when(matriculaRepository.findByAlunoIdOrderByTurmaPeriodoLetivoDesc(1L))
                .thenReturn(List.of(matricula));
        when(registroRepository.findByMatriculaId(30L)).thenReturn(registros);

        var respostas = frequenciaService.frequenciasDoAluno(1L);

        assertThat(respostas.get(0).percentual()).isEqualByComparingTo("90.0");
        assertThat(respostas.get(0).alerta()).isFalse();
    }

    @Test
    void semAulasRegistradasNaoCalculaPercentual() {
        when(matriculaRepository.findByAlunoIdOrderByTurmaPeriodoLetivoDesc(1L))
                .thenReturn(List.of(matricula));
        when(registroRepository.findByMatriculaId(30L)).thenReturn(List.of());

        var respostas = frequenciaService.frequenciasDoAluno(1L);

        assertThat(respostas.get(0).percentual()).isNull();
        assertThat(respostas.get(0).alerta()).isFalse();
    }
}
