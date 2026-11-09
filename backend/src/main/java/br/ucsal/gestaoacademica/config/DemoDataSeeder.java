package br.ucsal.gestaoacademica.config;

import br.ucsal.gestaoacademica.academico.CalendarioAcademico;
import br.ucsal.gestaoacademica.academico.CalendarioAcademicoRepository;
import br.ucsal.gestaoacademica.academico.Curso;
import br.ucsal.gestaoacademica.academico.CursoRepository;
import br.ucsal.gestaoacademica.academico.Disciplina;
import br.ucsal.gestaoacademica.academico.DisciplinaRepository;
import br.ucsal.gestaoacademica.academico.Matricula;
import br.ucsal.gestaoacademica.academico.MatriculaRepository;
import br.ucsal.gestaoacademica.academico.StatusMatricula;
import br.ucsal.gestaoacademica.academico.Turma;
import br.ucsal.gestaoacademica.academico.TurmaRepository;
import br.ucsal.gestaoacademica.avaliacao.Avaliacao;
import br.ucsal.gestaoacademica.avaliacao.AvaliacaoRepository;
import br.ucsal.gestaoacademica.avaliacao.Nota;
import br.ucsal.gestaoacademica.avaliacao.NotaRepository;
import br.ucsal.gestaoacademica.comunicacao.Aviso;
import br.ucsal.gestaoacademica.comunicacao.AvisoRepository;
import br.ucsal.gestaoacademica.comunicacao.EscopoAviso;
import br.ucsal.gestaoacademica.denuncia.Denuncia;
import br.ucsal.gestaoacademica.denuncia.DenunciaAndamento;
import br.ucsal.gestaoacademica.denuncia.DenunciaAndamentoRepository;
import br.ucsal.gestaoacademica.denuncia.DenunciaRepository;
import br.ucsal.gestaoacademica.denuncia.StatusDenuncia;
import br.ucsal.gestaoacademica.frequencia.Aula;
import br.ucsal.gestaoacademica.frequencia.AulaRepository;
import br.ucsal.gestaoacademica.frequencia.RegistroFrequencia;
import br.ucsal.gestaoacademica.frequencia.RegistroFrequenciaRepository;
import br.ucsal.gestaoacademica.usuario.Aluno;
import br.ucsal.gestaoacademica.usuario.AlunoRepository;
import br.ucsal.gestaoacademica.usuario.PerfilUsuario;
import br.ucsal.gestaoacademica.usuario.Professor;
import br.ucsal.gestaoacademica.usuario.ProfessorRepository;
import br.ucsal.gestaoacademica.usuario.Usuario;
import br.ucsal.gestaoacademica.usuario.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dados de demonstracao. So executa quando academico.seed-enabled=true e o banco
 * esta vazio. O dump final do banco fica em db/init/01-dump.sql.
 */
@Component
@ConditionalOnProperty(name = "academico.seed-enabled", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);
    private static final String SENHA_DEMO = "Demo@123";

    private final UsuarioRepository usuarioRepository;
    private final AlunoRepository alunoRepository;
    private final ProfessorRepository professorRepository;
    private final CursoRepository cursoRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final CalendarioAcademicoRepository calendarioRepository;
    private final TurmaRepository turmaRepository;
    private final MatriculaRepository matriculaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final NotaRepository notaRepository;
    private final AulaRepository aulaRepository;
    private final RegistroFrequenciaRepository frequenciaRepository;
    private final AvisoRepository avisoRepository;
    private final DenunciaRepository denunciaRepository;
    private final DenunciaAndamentoRepository andamentoRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(UsuarioRepository usuarioRepository, AlunoRepository alunoRepository,
            ProfessorRepository professorRepository, CursoRepository cursoRepository,
            DisciplinaRepository disciplinaRepository,
            CalendarioAcademicoRepository calendarioRepository, TurmaRepository turmaRepository,
            MatriculaRepository matriculaRepository, AvaliacaoRepository avaliacaoRepository,
            NotaRepository notaRepository, AulaRepository aulaRepository,
            RegistroFrequenciaRepository frequenciaRepository, AvisoRepository avisoRepository,
            DenunciaRepository denunciaRepository,
            DenunciaAndamentoRepository andamentoRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.alunoRepository = alunoRepository;
        this.professorRepository = professorRepository;
        this.cursoRepository = cursoRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.calendarioRepository = calendarioRepository;
        this.turmaRepository = turmaRepository;
        this.matriculaRepository = matriculaRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.notaRepository = notaRepository;
        this.aulaRepository = aulaRepository;
        this.frequenciaRepository = frequenciaRepository;
        this.avisoRepository = avisoRepository;
        this.denunciaRepository = denunciaRepository;
        this.andamentoRepository = andamentoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioRepository.count() > 0) {
            log.info("Seed ignorado: banco ja possui usuarios");
            return;
        }
        log.info("Gerando dados de demonstracao...");
        String senha = passwordEncoder.encode(SENHA_DEMO);

        Usuario admin = usuario(new Usuario(), "Administrador do Sistema",
                "00000000001", "admin@ucsal.br", PerfilUsuario.ADMIN, senha);
        Usuario coordenador = usuario(new Usuario(), "Carla Coordenadora",
                "00000000002", "coordenador@ucsal.br", PerfilUsuario.COORDENADOR, senha);
        Usuario secretaria = usuario(new Usuario(), "Sergio Secretario",
                "00000000003", "secretaria@ucsal.br", PerfilUsuario.SECRETARIA, senha);

        Professor paulo = professor("Paulo Professor", "00000000004", "professor@ucsal.br",
                "PROF-001", senha);
        Professor patricia = professor("Patricia Professora", "00000000005",
                "professor2@ucsal.br", "PROF-002", senha);

        Aluno ana = aluno("Ana Aluna", "00000000101", "aluno@ucsal.br", "20260001", senha);
        Aluno bruno = aluno("Bruno Aluno", "00000000102", "aluno2@ucsal.br", "20260002", senha);
        Aluno carla = aluno("Carla Aluna", "00000000103", "aluno3@ucsal.br", "20260003", senha);
        Aluno diego = aluno("Diego Aluno", "00000000104", "aluno4@ucsal.br", "20260004", senha);
        Aluno elisa = aluno("Elisa Aluna", "00000000105", "aluno5@ucsal.br", "20260005", senha);
        Aluno felipe = aluno("Felipe Aluno", "00000000106", "aluno6@ucsal.br", "20260006", senha);

        Curso cc = curso("Ciencia da Computacao");
        Curso es = curso("Engenharia de Software");

        Disciplina algoritmos = disciplina("Algoritmos", 60, cc);
        Disciplina estrutura = disciplina("Estrutura de Dados", 60, cc);
        Disciplina banco = disciplina("Banco de Dados", 60, cc);
        Disciplina engenharia = disciplina("Engenharia de Software", 60, cc);
        Disciplina redes = disciplina("Redes de Computadores", 60, cc);
        Disciplina web = disciplina("Programacao Web", 60, es);
        Disciplina qualidade = disciplina("Qualidade de Software", 60, es);
        Disciplina projetos = disciplina("Gestao de Projetos", 60, es);

        estrutura.setPrerequisitos(new HashSet<>(Set.of(algoritmos)));
        disciplinaRepository.save(estrutura);
        banco.setPrerequisitos(new HashSet<>(Set.of(estrutura)));
        disciplinaRepository.save(banco);
        engenharia.setPrerequisitos(new HashSet<>(Set.of(algoritmos)));
        disciplinaRepository.save(engenharia);
        qualidade.setPrerequisitos(new HashSet<>(Set.of(engenharia)));
        disciplinaRepository.save(qualidade);

        calendario("2025.2", LocalDate.of(2025, 7, 1), LocalDate.of(2025, 8, 10),
                LocalDate.of(2025, 12, 15));
        calendario("2026.1", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 7, 10));
        calendario("2026.2", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 11, 30),
                LocalDate.of(2026, 12, 20));

        Turma histAlgoritmos = turma(algoritmos, paulo, "2025.2", 40, true);
        Turma histEngenharia = turma(engenharia, paulo, "2026.1", 40, true);
        Turma turmaA = turma(estrutura, paulo, "2026.2", 2, false);
        Turma turmaB = turma(estrutura, patricia, "2026.2", 5, false);
        Turma turmaC = turma(banco, paulo, "2026.2", 10, false);
        Turma turmaD = turma(redes, patricia, "2026.2", 10, false);

        Matricula anaAlgoritmos = matricula(ana, histAlgoritmos, StatusMatricula.APROVADO,
                LocalDate.of(2025, 7, 5));
        Matricula brunoAlgoritmos = matricula(bruno, histAlgoritmos, StatusMatricula.APROVADO,
                LocalDate.of(2025, 7, 5));
        Matricula carlaAlgoritmos = matricula(carla, histAlgoritmos,
                StatusMatricula.REPROVADO_NOTA, LocalDate.of(2025, 7, 5));
        Matricula felipeAlgoritmos = matricula(felipe, histAlgoritmos,
                StatusMatricula.APROVADO, LocalDate.of(2025, 7, 5));

        Matricula anaEngenharia = matricula(ana, histEngenharia, StatusMatricula.APROVADO,
                LocalDate.of(2026, 2, 5));
        Matricula diegoEngenharia = matricula(diego, histEngenharia, StatusMatricula.APROVADO,
                LocalDate.of(2026, 2, 5));

        Matricula anaEstrutura = matricula(ana, turmaA, StatusMatricula.ATIVA,
                LocalDate.of(2026, 10, 2));
        Matricula brunoEstrutura = matricula(bruno, turmaA, StatusMatricula.ATIVA,
                LocalDate.of(2026, 10, 2));
        Matricula carlaEstrutura = matricula(carla, turmaB, StatusMatricula.ATIVA,
                LocalDate.of(2026, 10, 3));
        matricula(diego, turmaD, StatusMatricula.ATIVA, LocalDate.of(2026, 10, 3));
        matricula(elisa, turmaD, StatusMatricula.ATIVA, LocalDate.of(2026, 10, 3));

        Avaliacao histNotaAlgoritmos = avaliacao(histAlgoritmos, "Nota final", BigDecimal.ONE);
        nota(histNotaAlgoritmos, anaAlgoritmos, new BigDecimal("8.5"));
        nota(histNotaAlgoritmos, brunoAlgoritmos, new BigDecimal("7.5"));
        nota(histNotaAlgoritmos, carlaAlgoritmos, new BigDecimal("5.0"));
        nota(histNotaAlgoritmos, felipeAlgoritmos, new BigDecimal("9.0"));

        Avaliacao histNotaEngenharia = avaliacao(histEngenharia, "Nota final", BigDecimal.ONE);
        nota(histNotaEngenharia, anaEngenharia, new BigDecimal("9.0"));
        nota(histNotaEngenharia, diegoEngenharia, new BigDecimal("8.0"));

        Avaliacao prova1 = avaliacao(turmaA, "Prova 1", new BigDecimal("2"));
        Avaliacao prova2 = avaliacao(turmaA, "Prova 2", new BigDecimal("3"));
        Avaliacao trabalho = avaliacao(turmaA, "Trabalho", new BigDecimal("1"));
        nota(prova1, anaEstrutura, new BigDecimal("8.0"));
        nota(prova2, anaEstrutura, new BigDecimal("6.0"));
        nota(trabalho, anaEstrutura, new BigDecimal("9.0"));
        nota(prova1, brunoEstrutura, new BigDecimal("5.0"));
        nota(prova2, brunoEstrutura, new BigDecimal("7.0"));
        nota(trabalho, brunoEstrutura, new BigDecimal("6.0"));

        List<LocalDate> datas = List.of(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 8),
                LocalDate.of(2026, 9, 15), LocalDate.of(2026, 9, 22),
                LocalDate.of(2026, 9, 29), LocalDate.of(2026, 10, 6),
                LocalDate.of(2026, 10, 13), LocalDate.of(2026, 10, 20),
                LocalDate.of(2026, 10, 27));
        int indice = 0;
        for (LocalDate data : datas) {
            Aula aula = new Aula();
            aula.setTurma(turmaA);
            aula.setData(data);
            aula.setConteudo("Aula " + (++indice) + " - Estrutura de Dados");
            aulaRepository.save(aula);

            registro(aula, anaEstrutura, indice != 4 && indice != 8);
            registro(aula, brunoEstrutura, indice <= 6);
        }

        aviso(coordenador, "Bem-vindos ao semestre 2026.2",
                "As matriculas seguem ate 30/11. Em caso de duvida, procure a Secretaria.",
                EscopoAviso.GERAL, null, null);
        aviso(coordenador, "Semana de Tecnologia",
                "A Semana de Tecnologia acontece em novembro, com palestras e minicursos.",
                EscopoAviso.CURSO, null, cc);
        aviso(paulo, "Prova 2 de Estrutura de Dados",
                "A Prova 2 sera aplicada em 13/10. Revisem listas e exercicios.",
                EscopoAviso.TURMA, turmaA, null);
        aviso(paulo, "Material complementar",
                "Novo material sobre arvores binarias disponivel no portal.",
                EscopoAviso.TURMA, turmaA, null);
        aviso(patricia, "Atendimento extra",
                "Atendimento extra de Redes na sexta-feira, no laboratorio 4.",
                EscopoAviso.TURMA, turmaD, null);

        Denuncia denuncia1 = denuncia("DEN-2026-000001", true, null, "ASSEDIO",
                "Relato anonimo sobre conduta inadequada em sala de aula.",
                LocalDate.of(2026, 9, 20), StatusDenuncia.RECEBIDA);
        Denuncia denuncia2 = denuncia("DEN-2026-000002", false, ana, "INFRAESTRUTURA",
                "Ar-condicionado do laboratorio 2 esta quebrado ha duas semanas.",
                LocalDate.of(2026, 10, 1), StatusDenuncia.EM_TRIAGEM);
        Denuncia denuncia3 = denuncia("DEN-2026-000003", true, null, "SEGURANCA",
                "Iluminacao deficiente no acesso ao bloco C durante a noite.",
                LocalDate.of(2026, 10, 15), StatusDenuncia.EM_APURACAO);

        andamento(denuncia2, coordenador, "EM_TRIAGEM",
                "Denuncia recebida e encaminhada para a equipe de infraestrutura.");
        andamento(denuncia3, coordenador, "EM_APURACAO",
                "Equipe de seguranca acionada para vistoria no local.");
        andamento(denuncia3, coordenador, "ENCAMINHADA",
                "Solicitacao de melhorias encaminhada a prefeitura do campus.");

        log.info("Seed concluido: {} usuarios, {} turmas, {} matriculas",
                usuarioRepository.count(), turmaRepository.count(),
                matriculaRepository.count());
    }

    private Usuario usuario(Usuario usuario, String nome, String documento, String email,
            PerfilUsuario perfil, String senha) {
        usuario.setNome(nome);
        usuario.setDocumento(documento);
        usuario.setEmail(email);
        usuario.setPerfil(perfil);
        usuario.setSenhaHash(senha);
        usuario.setAtivo(true);
        return usuarioRepository.save(usuario);
    }

    private Professor professor(String nome, String documento, String email,
            String registroFuncional, String senha) {
        Professor professor = new Professor();
        professor.setNome(nome);
        professor.setDocumento(documento);
        professor.setEmail(email);
        professor.setPerfil(PerfilUsuario.PROFESSOR);
        professor.setSenhaHash(senha);
        professor.setAtivo(true);
        professor.setRegistroFuncional(registroFuncional);
        return professorRepository.save(professor);
    }

    private Aluno aluno(String nome, String documento, String email, String numeroMatricula,
            String senha) {
        Aluno aluno = new Aluno();
        aluno.setNome(nome);
        aluno.setDocumento(documento);
        aluno.setEmail(email);
        aluno.setPerfil(PerfilUsuario.ALUNO);
        aluno.setSenhaHash(senha);
        aluno.setAtivo(true);
        aluno.setNumeroMatricula(numeroMatricula);
        return alunoRepository.save(aluno);
    }

    private Curso curso(String nome) {
        Curso curso = new Curso();
        curso.setNome(nome);
        return cursoRepository.save(curso);
    }

    private Disciplina disciplina(String nome, int cargaHoraria, Curso curso) {
        Disciplina disciplina = new Disciplina();
        disciplina.setNome(nome);
        disciplina.setCargaHoraria(cargaHoraria);
        disciplina.setCurso(curso);
        return disciplinaRepository.save(disciplina);
    }

    private void calendario(String periodo, LocalDate inicio, LocalDate fimMatricula,
            LocalDate fimPeriodo) {
        CalendarioAcademico calendario = new CalendarioAcademico();
        calendario.setPeriodoLetivo(periodo);
        calendario.setInicioMatricula(inicio);
        calendario.setFimMatricula(fimMatricula);
        calendario.setFimPeriodo(fimPeriodo);
        calendarioRepository.save(calendario);
    }

    private Turma turma(Disciplina disciplina, Professor professor, String periodo,
            int limiteVagas, boolean encerrada) {
        Turma turma = new Turma();
        turma.setDisciplina(disciplina);
        turma.setProfessor(professor);
        turma.setPeriodoLetivo(periodo);
        turma.setLimiteVagas(limiteVagas);
        turma.setEncerrada(encerrada);
        return turmaRepository.save(turma);
    }

    private Matricula matricula(Aluno aluno, Turma turma, StatusMatricula status,
            LocalDate data) {
        Matricula matricula = new Matricula();
        matricula.setAluno(aluno);
        matricula.setTurma(turma);
        matricula.setStatus(status);
        matricula.setData(data);
        return matriculaRepository.save(matricula);
    }

    private Avaliacao avaliacao(Turma turma, String descricao, BigDecimal peso) {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setTurma(turma);
        avaliacao.setDescricao(descricao);
        avaliacao.setPeso(peso);
        return avaliacaoRepository.save(avaliacao);
    }

    private void nota(Avaliacao avaliacao, Matricula matricula, BigDecimal valor) {
        Nota nota = new Nota();
        nota.setAvaliacao(avaliacao);
        nota.setMatricula(matricula);
        nota.setValor(valor);
        notaRepository.save(nota);
    }

    private void registro(Aula aula, Matricula matricula, boolean presente) {
        RegistroFrequencia registro = new RegistroFrequencia();
        registro.setAula(aula);
        registro.setMatricula(matricula);
        registro.setPresente(presente);
        frequenciaRepository.save(registro);
    }

    private void aviso(Usuario autor, String titulo, String mensagem, EscopoAviso escopo,
            Turma turma, Curso curso) {
        Aviso aviso = new Aviso();
        aviso.setAutor(autor);
        aviso.setTitulo(titulo);
        aviso.setMensagem(mensagem);
        aviso.setEscopo(escopo);
        aviso.setTurma(turma);
        aviso.setCurso(curso);
        aviso.setCriadoEm(LocalDateTime.now());
        avisoRepository.save(aviso);
    }

    private Denuncia denuncia(String protocolo, boolean anonima, Usuario denunciante,
            String categoria, String descricao, LocalDate dataAproximada,
            StatusDenuncia status) {
        Denuncia denuncia = new Denuncia();
        denuncia.setProtocolo(protocolo);
        denuncia.setAnonima(anonima);
        denuncia.setDenunciante(denunciante);
        denuncia.setCategoria(categoria);
        denuncia.setDescricao(descricao);
        denuncia.setDataAproximada(dataAproximada);
        denuncia.setStatus(status);
        return denunciaRepository.save(denuncia);
    }

    private void andamento(Denuncia denuncia, Usuario autor, String etapa, String descricao) {
        DenunciaAndamento andamento = new DenunciaAndamento();
        andamento.setDenuncia(denuncia);
        andamento.setAutor(autor);
        andamento.setEtapa(etapa);
        andamento.setDescricao(descricao);
        andamentoRepository.save(andamento);
    }
}
