package br.ucsal.gestaoacademica.avaliacao;

import br.ucsal.gestaoacademica.academico.Matricula;
import br.ucsal.gestaoacademica.academico.MatriculaRepository;
import br.ucsal.gestaoacademica.academico.StatusMatricula;
import br.ucsal.gestaoacademica.academico.Turma;
import br.ucsal.gestaoacademica.academico.TurmaService;
import br.ucsal.gestaoacademica.auditoria.AuditoriaService;
import br.ucsal.gestaoacademica.common.BusinessException;
import br.ucsal.gestaoacademica.config.AcademicProperties;
import br.ucsal.gestaoacademica.frequencia.RegistroFrequencia;
import br.ucsal.gestaoacademica.frequencia.RegistroFrequenciaRepository;
import br.ucsal.gestaoacademica.usuario.PerfilUsuario;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotaService {

    private final NotaRepository notaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final MatriculaRepository matriculaRepository;
    private final RegistroFrequenciaRepository frequenciaRepository;
    private final TurmaService turmaService;
    private final AuditoriaService auditoriaService;
    private final AcademicProperties propriedades;

    public NotaService(NotaRepository notaRepository, AvaliacaoRepository avaliacaoRepository,
            MatriculaRepository matriculaRepository,
            RegistroFrequenciaRepository frequenciaRepository, TurmaService turmaService,
            AuditoriaService auditoriaService, AcademicProperties propriedades) {
        this.notaRepository = notaRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.matriculaRepository = matriculaRepository;
        this.frequenciaRepository = frequenciaRepository;
        this.turmaService = turmaService;
        this.auditoriaService = auditoriaService;
        this.propriedades = propriedades;
    }

    @Transactional
    public List<NotaResponse> lancarLote(Long turmaId, NotasLoteRequest request, Long usuarioId,
            PerfilUsuario perfil) {
        Turma turma = turmaService.validarAcessoGestao(turmaId, usuarioId, perfil);
        boolean turmaEncerrada = turma.isEncerrada();
        if (turmaEncerrada && !podeAlterarAposEncerramento(perfil)) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Turma encerrada: alteracao de nota exige coordenacao ou secretaria");
        }

        List<NotaResponse> respostas = new ArrayList<>();
        for (NotaLancamentoRequest item : request.notas()) {
            if (item.valor().compareTo(BigDecimal.ZERO) < 0
                    || item.valor().compareTo(BigDecimal.TEN) > 0) {
                throw new BusinessException("Nota deve estar entre 0 e 10");
            }
            Avaliacao avaliacao = avaliacaoRepository.findByIdAndTurmaId(
                            item.avaliacaoId(), turma.getId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST,
                            "Avaliacao nao pertence a turma informada"));
            Matricula matricula = matriculaRepository.findById(item.matriculaId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                            "Matricula nao encontrada"));
            if (!matricula.getTurma().getId().equals(turma.getId())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "Matricula nao pertence a turma informada");
            }
            if (matricula.getStatus() == StatusMatricula.CANCELADA) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "Nao e possivel lancar nota em matricula cancelada");
            }

            Nota nota = notaRepository
                    .findByAvaliacaoIdAndMatriculaId(avaliacao.getId(), matricula.getId())
                    .orElseGet(() -> {
                        Nota nova = new Nota();
                        nova.setAvaliacao(avaliacao);
                        nova.setMatricula(matricula);
                        return nova;
                    });
            nota.setValor(item.valor());
            notaRepository.save(nota);
            respostas.add(NotaResponse.from(nota));

            if (turmaEncerrada) {
                auditoriaService.registrarAuditoria("ALTERAR_NOTA_POS_ENCERRAMENTO", "Nota",
                        nota.getId(), "Turma " + turma.getId() + ", matricula "
                                + matricula.getId() + ", valor " + item.valor());
            }
        }
        if (!turmaEncerrada) {
            auditoriaService.registrarAuditoria("LANCAR_NOTAS", "Turma", turma.getId(),
                    respostas.size() + " nota(s)");
        }
        return respostas;
    }

    @Transactional(readOnly = true)
    public TurmaNotasResponse listarNotasDaTurma(Long turmaId, Long usuarioId,
            PerfilUsuario perfil) {
        Turma turma = turmaService.validarAcessoGestao(turmaId, usuarioId, perfil);
        List<Avaliacao> avaliacoes =
                avaliacaoRepository.findByTurmaIdOrderByDescricaoAsc(turma.getId());
        List<Nota> todasNotas = notaRepository.findByAvaliacaoTurmaId(turma.getId());
        Map<Long, List<Nota>> notasPorMatricula = new HashMap<>();
        for (Nota nota : todasNotas) {
            notasPorMatricula.computeIfAbsent(nota.getMatricula().getId(),
                    chave -> new ArrayList<>()).add(nota);
        }

        List<TurmaNotasResponse.AlunoNotas> alunos = new ArrayList<>();
        for (Matricula matricula : matriculaRepository.findByTurmaIdOrderByAlunoNomeAsc(
                turma.getId())) {
            if (matricula.getStatus() == StatusMatricula.CANCELADA) {
                continue;
            }
            List<Nota> notas = notasPorMatricula.getOrDefault(matricula.getId(), List.of());
            BigDecimal media = CalculoAcademico.media(avaliacoes,
                    CalculoAcademico.indexarPorAvaliacao(notas));
            BigDecimal frequencia = calcularFrequencia(matricula.getId());
            alunos.add(new TurmaNotasResponse.AlunoNotas(matricula.getId(),
                    matricula.getAluno().getId(), matricula.getAluno().getNome(),
                    matricula.getAluno().getNumeroMatricula(), media, frequencia,
                    notas.stream().map(NotaResponse::from).toList()));
        }
        return new TurmaNotasResponse(turma.getId(),
                avaliacoes.stream().map(AvaliacaoResponse::from).toList(), alunos);
    }

    @Transactional(readOnly = true)
    public List<BoletimAlunoResponse> boletinsDoAluno(Long alunoId) {
        List<BoletimAlunoResponse> boletins = new ArrayList<>();
        for (Matricula matricula : matriculaRepository
                .findByAlunoIdOrderByTurmaPeriodoLetivoDesc(alunoId)) {
            if (matricula.getStatus() != StatusMatricula.CANCELADA) {
                boletins.add(boletim(matricula));
            }
        }
        return boletins;
    }

    @Transactional(readOnly = true)
    public BoletimAlunoResponse boletim(Matricula matricula) {
        List<Avaliacao> avaliacoes = avaliacaoRepository
                .findByTurmaIdOrderByDescricaoAsc(matricula.getTurma().getId());
        List<Nota> notas = notaRepository.findByMatriculaId(matricula.getId());
        Map<Long, Nota> notasPorAvaliacao = CalculoAcademico.indexarPorAvaliacao(notas);
        BigDecimal media = CalculoAcademico.media(avaliacoes, notasPorAvaliacao);
        BigDecimal necessario = CalculoAcademico.notaNecessaria(
                avaliacoes, notasPorAvaliacao, propriedades.getMediaAprovacao());
        BigDecimal frequencia = calcularFrequencia(matricula.getId());
        StatusMatricula status = matricula.getStatus() == StatusMatricula.ATIVA
                ? CalculoAcademico.situacao(media, frequencia, propriedades)
                : matricula.getStatus();

        List<BoletimAlunoResponse.AvaliacaoNotaResponse> itens = avaliacoes.stream()
                .map(avaliacao -> new BoletimAlunoResponse.AvaliacaoNotaResponse(
                        avaliacao.getId(), avaliacao.getDescricao(), avaliacao.getPeso(),
                        notasPorAvaliacao.containsKey(avaliacao.getId())
                                ? notasPorAvaliacao.get(avaliacao.getId()).getValor() : null))
                .toList();

        return new BoletimAlunoResponse(matricula.getId(),
                matricula.getTurma().getDisciplina().getNome(),
                matricula.getTurma().getProfessor().getNome(),
                matricula.getTurma().getPeriodoLetivo(),
                status, media, necessario, frequencia,
                CalculoAcademico.alertaFrequencia(frequencia, propriedades), itens);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularFrequencia(Long matriculaId) {
        List<RegistroFrequencia> registros = frequenciaRepository.findByMatriculaId(matriculaId);
        long presencas = registros.stream().filter(RegistroFrequencia::isPresente).count();
        return CalculoAcademico.frequencia(presencas, registros.size());
    }

    private boolean podeAlterarAposEncerramento(PerfilUsuario perfil) {
        return perfil == PerfilUsuario.COORDENADOR
                || perfil == PerfilUsuario.SECRETARIA
                || perfil == PerfilUsuario.ADMIN;
    }
}
