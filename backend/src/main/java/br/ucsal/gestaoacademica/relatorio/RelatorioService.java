package br.ucsal.gestaoacademica.relatorio;

import br.ucsal.gestaoacademica.academico.Matricula;
import br.ucsal.gestaoacademica.academico.MatriculaRepository;
import br.ucsal.gestaoacademica.academico.StatusMatricula;
import br.ucsal.gestaoacademica.academico.Turma;
import br.ucsal.gestaoacademica.academico.TurmaRepository;
import br.ucsal.gestaoacademica.academico.TurmaService;
import br.ucsal.gestaoacademica.avaliacao.Avaliacao;
import br.ucsal.gestaoacademica.avaliacao.AvaliacaoRepository;
import br.ucsal.gestaoacademica.avaliacao.BoletimAlunoResponse;
import br.ucsal.gestaoacademica.avaliacao.CalculoAcademico;
import br.ucsal.gestaoacademica.avaliacao.Nota;
import br.ucsal.gestaoacademica.avaliacao.NotaRepository;
import br.ucsal.gestaoacademica.avaliacao.NotaService;
import br.ucsal.gestaoacademica.common.BusinessException;
import br.ucsal.gestaoacademica.comunicacao.AvisoService;
import br.ucsal.gestaoacademica.config.AcademicProperties;
import br.ucsal.gestaoacademica.frequencia.RegistroFrequencia;
import br.ucsal.gestaoacademica.frequencia.RegistroFrequenciaRepository;
import br.ucsal.gestaoacademica.usuario.Aluno;
import br.ucsal.gestaoacademica.usuario.AlunoRepository;
import br.ucsal.gestaoacademica.usuario.PerfilUsuario;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RelatorioService {

    private final MatriculaRepository matriculaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final NotaRepository notaRepository;
    private final RegistroFrequenciaRepository frequenciaRepository;
    private final AlunoRepository alunoRepository;
    private final TurmaRepository turmaRepository;
    private final TurmaService turmaService;
    private final NotaService notaService;
    private final AvisoService avisoService;
    private final AcademicProperties propriedades;

    public RelatorioService(MatriculaRepository matriculaRepository,
            AvaliacaoRepository avaliacaoRepository, NotaRepository notaRepository,
            RegistroFrequenciaRepository frequenciaRepository, AlunoRepository alunoRepository,
            TurmaRepository turmaRepository, TurmaService turmaService, NotaService notaService,
            AvisoService avisoService, AcademicProperties propriedades) {
        this.matriculaRepository = matriculaRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.notaRepository = notaRepository;
        this.frequenciaRepository = frequenciaRepository;
        this.alunoRepository = alunoRepository;
        this.turmaRepository = turmaRepository;
        this.turmaService = turmaService;
        this.notaService = notaService;
        this.avisoService = avisoService;
        this.propriedades = propriedades;
    }

    @Transactional(readOnly = true)
    public BoletimRelatorioResponse boletim(Long matriculaId, Long usuarioId, PerfilUsuario perfil) {
        Matricula matricula = matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "Matricula nao encontrada"));
        validarAcessoMatricula(matricula, usuarioId, perfil);
        return new BoletimRelatorioResponse(matricula.getAluno().getId(),
                matricula.getAluno().getNome(), matricula.getAluno().getNumeroMatricula(),
                notaService.boletim(matricula));
    }

    @Transactional(readOnly = true)
    public HistoricoAlunoResponse historico(Long alunoId, Long usuarioId, PerfilUsuario perfil) {
        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Aluno nao encontrado"));
        if (perfil == PerfilUsuario.ALUNO && !aluno.getId().equals(usuarioId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Voce so pode consultar seu proprio historico");
        }
        if (perfil == PerfilUsuario.PROFESSOR) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Historico disponivel para o aluno e para a secretaria");
        }

        List<HistoricoAlunoResponse.ItemHistorico> itens = new ArrayList<>();
        BigDecimal somaMedias = BigDecimal.ZERO;
        int comMedia = 0;
        for (Matricula matricula : matriculaRepository
                .findByAlunoIdOrderByTurmaPeriodoLetivoDesc(aluno.getId())) {
            if (matricula.getStatus() == StatusMatricula.CANCELADA) {
                continue;
            }
            Dados dados = calcular(matricula);
            itens.add(new HistoricoAlunoResponse.ItemHistorico(
                    matricula.getId(),
                    matricula.getTurma().getPeriodoLetivo(),
                    matricula.getTurma().getDisciplina().getNome(),
                    matricula.getTurma().getDisciplina().getCurso().getNome(),
                    matricula.getTurma().getDisciplina().getCargaHoraria(),
                    matricula.getTurma().getProfessor().getNome(),
                    dados.media(), dados.frequencia(), dados.situacao()));
            if (dados.media() != null) {
                somaMedias = somaMedias.add(dados.media());
                comMedia++;
            }
        }
        BigDecimal mediaGeral = comMedia == 0 ? null
                : somaMedias.divide(BigDecimal.valueOf(comMedia), 2, RoundingMode.HALF_UP);
        return new HistoricoAlunoResponse(aluno.getId(), aluno.getNome(),
                aluno.getNumeroMatricula(), mediaGeral, itens);
    }

    @Transactional(readOnly = true)
    public RelatorioTurmaResponse relatorioTurma(Long turmaId, Long usuarioId,
            PerfilUsuario perfil) {
        Turma turma = turmaService.validarAcessoGestao(turmaId, usuarioId, perfil);
        List<Avaliacao> avaliacoes =
                avaliacaoRepository.findByTurmaIdOrderByDescricaoAsc(turma.getId());
        Map<Long, List<Nota>> notasPorMatricula = new HashMap<>();
        for (Nota nota : notaRepository.findByAvaliacaoTurmaId(turma.getId())) {
            notasPorMatricula.computeIfAbsent(nota.getMatricula().getId(),
                    chave -> new ArrayList<>()).add(nota);
        }

        List<RelatorioTurmaResponse.AlunoRelatorio> alunos = new ArrayList<>();
        BigDecimal somaMedias = BigDecimal.ZERO;
        int comMedia = 0;
        long aprovados = 0;
        long reprovadosNota = 0;
        long reprovadosFalta = 0;
        long ativos = 0;

        for (Matricula matricula : matriculaRepository.findByTurmaIdOrderByAlunoNomeAsc(
                turma.getId())) {
            if (matricula.getStatus() == StatusMatricula.CANCELADA) {
                continue;
            }
            List<Nota> notas = notasPorMatricula.getOrDefault(matricula.getId(), List.of());
            BigDecimal media = CalculoAcademico.media(avaliacoes,
                    CalculoAcademico.indexarPorAvaliacao(notas));
            BigDecimal frequencia = frequencia(matricula.getId());
            StatusMatricula situacao = matricula.getStatus() == StatusMatricula.ATIVA
                    ? CalculoAcademico.situacao(media, frequencia, propriedades)
                    : matricula.getStatus();

            alunos.add(new RelatorioTurmaResponse.AlunoRelatorio(matricula.getId(),
                    matricula.getAluno().getId(), matricula.getAluno().getNome(),
                    matricula.getAluno().getNumeroMatricula(), media, frequencia, situacao));
            if (media != null) {
                somaMedias = somaMedias.add(media);
                comMedia++;
            }
            switch (situacao) {
                case APROVADO -> aprovados++;
                case REPROVADO_NOTA -> reprovadosNota++;
                case REPROVADO_FALTA -> reprovadosFalta++;
                default -> ativos++;
            }
        }

        BigDecimal mediaTurma = comMedia == 0 ? null
                : somaMedias.divide(BigDecimal.valueOf(comMedia), 2, RoundingMode.HALF_UP);
        return new RelatorioTurmaResponse(turma.getId(),
                turma.getDisciplina().getNome(), turma.getProfessor().getNome(),
                turma.getPeriodoLetivo(), turma.isEncerrada(), mediaTurma, alunos.size(),
                aprovados, reprovadosNota, reprovadosFalta, ativos, alunos);
    }

    @Transactional(readOnly = true)
    public List<IndicadorDisciplinaResponse> indicadoresDisciplinas() {
        Map<Long, Acumulador> acumuladores = new LinkedHashMap<>();
        for (Turma turma : turmaRepository.findAllByOrderByPeriodoLetivoDescIdAsc()) {
            List<Avaliacao> avaliacoes =
                    avaliacaoRepository.findByTurmaIdOrderByDescricaoAsc(turma.getId());
            Map<Long, List<Nota>> notasPorMatricula = new HashMap<>();
            for (Nota nota : notaRepository.findByAvaliacaoTurmaId(turma.getId())) {
                notasPorMatricula.computeIfAbsent(nota.getMatricula().getId(),
                        chave -> new ArrayList<>()).add(nota);
            }
            Acumulador acumulador = acumuladores.computeIfAbsent(
                    turma.getDisciplina().getId(),
                    chave -> new Acumulador(turma.getDisciplina()));
            for (Matricula matricula : matriculaRepository.findByTurmaIdAndStatusNot(
                    turma.getId(), StatusMatricula.CANCELADA)) {
                BigDecimal media = CalculoAcademico.media(avaliacoes,
                        CalculoAcademico.indexarPorAvaliacao(
                                notasPorMatricula.getOrDefault(matricula.getId(), List.of())));
                BigDecimal frequencia = frequencia(matricula.getId());
                StatusMatricula situacao = matricula.getStatus() == StatusMatricula.ATIVA
                        ? CalculoAcademico.situacao(media, frequencia, propriedades)
                        : matricula.getStatus();
                acumulador.adicionar(media, situacao);
            }
        }

        return acumuladores.values().stream()
                .sorted(Comparator.comparing(acumulador -> acumulador.disciplina.getNome()))
                .map(Acumulador::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public IndicadorTurmaResponse indicadoresTurma(Long turmaId, Long usuarioId,
            PerfilUsuario perfil) {
        Turma turma = turmaService.validarAcessoGestao(turmaId, usuarioId, perfil);
        List<Avaliacao> avaliacoes =
                avaliacaoRepository.findByTurmaIdOrderByDescricaoAsc(turma.getId());
        List<Nota> todasNotas = notaRepository.findByAvaliacaoTurmaId(turma.getId());
        Map<Long, List<Nota>> notasPorMatricula = new HashMap<>();
        Map<Long, List<Nota>> notasPorAvaliacao = new HashMap<>();
        for (Nota nota : todasNotas) {
            notasPorMatricula.computeIfAbsent(nota.getMatricula().getId(),
                    chave -> new ArrayList<>()).add(nota);
            notasPorAvaliacao.computeIfAbsent(nota.getAvaliacao().getId(),
                    chave -> new ArrayList<>()).add(nota);
        }

        List<IndicadorTurmaResponse.PontoAluno> alunos = new ArrayList<>();
        for (Matricula matricula : matriculaRepository.findByTurmaIdAndStatusNot(
                turma.getId(), StatusMatricula.CANCELADA)) {
            BigDecimal media = CalculoAcademico.media(avaliacoes,
                    CalculoAcademico.indexarPorAvaliacao(
                            notasPorMatricula.getOrDefault(matricula.getId(), List.of())));
            alunos.add(new IndicadorTurmaResponse.PontoAluno(matricula.getId(),
                    matricula.getAluno().getNome(), media, frequencia(matricula.getId())));
        }

        List<IndicadorTurmaResponse.EvolucaoAvaliacao> evolucao = avaliacoes.stream()
                .map(avaliacao -> {
                    List<Nota> notas = notasPorAvaliacao.getOrDefault(
                            avaliacao.getId(), List.of());
                    BigDecimal soma = notas.stream().map(Nota::getValor)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal media = notas.isEmpty() ? null
                            : soma.divide(BigDecimal.valueOf(notas.size()), 2,
                                    RoundingMode.HALF_UP);
                    return new IndicadorTurmaResponse.EvolucaoAvaliacao(avaliacao.getId(),
                            avaliacao.getDescricao(), avaliacao.getPeso(), media,
                            notas.size());
                })
                .toList();

        return new IndicadorTurmaResponse(turma.getId(), turma.getDisciplina().getNome(),
                turma.getProfessor().getNome(), turma.getPeriodoLetivo(), alunos, evolucao);
    }

    @Transactional(readOnly = true)
    public AlunoDashboardResponse dashboard(Long alunoId) {
        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Aluno nao encontrado"));

        List<BoletimAlunoResponse> boletins = notaService.boletinsDoAluno(alunoId).stream()
                .filter(boletim -> boletim.status() == StatusMatricula.ATIVA
                        || boletim.status() == StatusMatricula.APROVADO
                        || boletim.status() == StatusMatricula.REPROVADO_NOTA
                        || boletim.status() == StatusMatricula.REPROVADO_FALTA)
                .toList();

        BigDecimal somaMedias = BigDecimal.ZERO;
        int comMedia = 0;
        BigDecimal somaFrequencias = BigDecimal.ZERO;
        int comFrequencia = 0;
        boolean alerta = false;
        String periodo = null;
        for (BoletimAlunoResponse boletim : boletins) {
            if (boletim.media() != null) {
                somaMedias = somaMedias.add(boletim.media());
                comMedia++;
            }
            if (boletim.frequencia() != null) {
                somaFrequencias = somaFrequencias.add(boletim.frequencia());
                comFrequencia++;
            }
            alerta = alerta || boletim.alertaFrequencia();
            if (periodo == null) {
                periodo = boletim.periodoLetivo();
            }
        }
        BigDecimal mediaGeral = comMedia == 0 ? null
                : somaMedias.divide(BigDecimal.valueOf(comMedia), 2, RoundingMode.HALF_UP);
        BigDecimal frequenciaMedia = comFrequencia == 0 ? null
                : somaFrequencias.divide(BigDecimal.valueOf(comFrequencia), 2,
                        RoundingMode.HALF_UP);

        int disciplinasAtivas = matriculaRepository
                .findByAlunoIdAndStatus(alunoId, StatusMatricula.ATIVA).size();
        return new AlunoDashboardResponse(aluno.getNome(), periodo, disciplinasAtivas,
                mediaGeral, frequenciaMedia, alerta,
                avisoService.listar(alunoId, PerfilUsuario.ALUNO).stream().limit(5).toList(),
                boletins);
    }

    @Transactional(readOnly = true)
    public String relatorioTurmaCsv(Long turmaId, Long usuarioId, PerfilUsuario perfil) {
        RelatorioTurmaResponse relatorio = relatorioTurma(turmaId, usuarioId, perfil);
        StringBuilder csv = new StringBuilder();
        csv.append("Aluno;Matricula;Media;Frequencia;Situacao\n");
        for (RelatorioTurmaResponse.AlunoRelatorio aluno : relatorio.alunos()) {
            csv.append(escapar(aluno.nome())).append(';')
                    .append(escapar(aluno.numeroMatricula())).append(';')
                    .append(valor(aluno.media())).append(';')
                    .append(valor(aluno.frequencia())).append(';')
                    .append(aluno.situacao()).append('\n');
        }
        return csv.toString();
    }

    @Transactional(readOnly = true)
    public String historicoCsv(Long alunoId, Long usuarioId, PerfilUsuario perfil) {
        HistoricoAlunoResponse historico = historico(alunoId, usuarioId, perfil);
        StringBuilder csv = new StringBuilder();
        csv.append("Periodo;Disciplina;Curso;CargaHoraria;Professor;Media;Frequencia;Situacao\n");
        for (HistoricoAlunoResponse.ItemHistorico item : historico.disciplinas()) {
            csv.append(escapar(item.periodoLetivo())).append(';')
                    .append(escapar(item.disciplina())).append(';')
                    .append(escapar(item.curso())).append(';')
                    .append(item.cargaHoraria()).append(';')
                    .append(escapar(item.professor())).append(';')
                    .append(valor(item.media())).append(';')
                    .append(valor(item.frequencia())).append(';')
                    .append(item.status()).append('\n');
        }
        return csv.toString();
    }

    private void validarAcessoMatricula(Matricula matricula, Long usuarioId,
            PerfilUsuario perfil) {
        if (perfil == PerfilUsuario.ALUNO && !matricula.getAluno().getId().equals(usuarioId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Voce so pode consultar seus proprios dados");
        }
        if (perfil == PerfilUsuario.PROFESSOR
                && !matricula.getTurma().getProfessor().getId().equals(usuarioId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Voce so pode consultar matriculas de suas turmas");
        }
    }

    private Dados calcular(Matricula matricula) {
        List<Avaliacao> avaliacoes = avaliacaoRepository
                .findByTurmaIdOrderByDescricaoAsc(matricula.getTurma().getId());
        List<Nota> notas = notaRepository.findByMatriculaId(matricula.getId());
        BigDecimal media = CalculoAcademico.media(avaliacoes,
                CalculoAcademico.indexarPorAvaliacao(notas));
        BigDecimal frequencia = frequencia(matricula.getId());
        StatusMatricula situacao = matricula.getStatus() == StatusMatricula.ATIVA
                ? CalculoAcademico.situacao(media, frequencia, propriedades)
                : matricula.getStatus();
        return new Dados(media, frequencia, situacao);
    }

    private BigDecimal frequencia(Long matriculaId) {
        List<RegistroFrequencia> registros = frequenciaRepository.findByMatriculaId(matriculaId);
        long presencas = registros.stream().filter(RegistroFrequencia::isPresente).count();
        return CalculoAcademico.frequencia(presencas, registros.size());
    }

    private String valor(BigDecimal valor) {
        return valor == null ? "" : valor.toPlainString();
    }

    private String escapar(String texto) {
        return texto == null ? "" : texto.replace(';', ',').replace('\n', ' ');
    }

    private record Dados(BigDecimal media, BigDecimal frequencia, StatusMatricula situacao) {
    }

    private static final class Acumulador {
        private final br.ucsal.gestaoacademica.academico.Disciplina disciplina;
        private long matriculas;
        private long aprovados;
        private long reprovadosNota;
        private long reprovadosFalta;
        private long ativos;
        private BigDecimal somaMedias = BigDecimal.ZERO;
        private long comMedia;

        private Acumulador(br.ucsal.gestaoacademica.academico.Disciplina disciplina) {
            this.disciplina = disciplina;
        }

        private void adicionar(BigDecimal media, StatusMatricula situacao) {
            matriculas++;
            if (media != null) {
                somaMedias = somaMedias.add(media);
                comMedia++;
            }
            switch (situacao) {
                case APROVADO -> aprovados++;
                case REPROVADO_NOTA -> reprovadosNota++;
                case REPROVADO_FALTA -> reprovadosFalta++;
                default -> ativos++;
            }
        }

        private IndicadorDisciplinaResponse toResponse() {
            BigDecimal media = comMedia == 0 ? null
                    : somaMedias.divide(BigDecimal.valueOf(comMedia), 2, RoundingMode.HALF_UP);
            long reprovados = reprovadosNota + reprovadosFalta;
            BigDecimal taxaAprovacao = matriculas == 0 ? BigDecimal.ZERO
                    : BigDecimal.valueOf(aprovados).multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(matriculas), 1, RoundingMode.HALF_UP);
            BigDecimal taxaReprovacao = matriculas == 0 ? BigDecimal.ZERO
                    : BigDecimal.valueOf(reprovados).multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(matriculas), 1, RoundingMode.HALF_UP);
            return new IndicadorDisciplinaResponse(disciplina.getId(), disciplina.getNome(),
                    disciplina.getCurso().getNome(), matriculas, media, aprovados,
                    reprovadosNota, reprovadosFalta, ativos, taxaAprovacao, taxaReprovacao);
        }
    }
}
