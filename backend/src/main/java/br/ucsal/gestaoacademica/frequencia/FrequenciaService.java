package br.ucsal.gestaoacademica.frequencia;

import br.ucsal.gestaoacademica.academico.Matricula;
import br.ucsal.gestaoacademica.academico.MatriculaRepository;
import br.ucsal.gestaoacademica.academico.StatusMatricula;
import br.ucsal.gestaoacademica.academico.Turma;
import br.ucsal.gestaoacademica.academico.TurmaService;
import br.ucsal.gestaoacademica.auditoria.AuditoriaService;
import br.ucsal.gestaoacademica.avaliacao.CalculoAcademico;
import br.ucsal.gestaoacademica.common.BusinessException;
import br.ucsal.gestaoacademica.config.AcademicProperties;
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
public class FrequenciaService {

    private final AulaRepository aulaRepository;
    private final RegistroFrequenciaRepository registroRepository;
    private final MatriculaRepository matriculaRepository;
    private final TurmaService turmaService;
    private final AuditoriaService auditoriaService;
    private final AcademicProperties propriedades;

    public FrequenciaService(AulaRepository aulaRepository,
            RegistroFrequenciaRepository registroRepository,
            MatriculaRepository matriculaRepository, TurmaService turmaService,
            AuditoriaService auditoriaService, AcademicProperties propriedades) {
        this.aulaRepository = aulaRepository;
        this.registroRepository = registroRepository;
        this.matriculaRepository = matriculaRepository;
        this.turmaService = turmaService;
        this.auditoriaService = auditoriaService;
        this.propriedades = propriedades;
    }

    @Transactional(readOnly = true)
    public List<AulaResponse> listarAulas(Long turmaId, Long usuarioId, PerfilUsuario perfil) {
        Turma turma = turmaService.validarAcessoGestao(turmaId, usuarioId, perfil);
        return aulaRepository.findByTurmaIdOrderByDataAsc(turma.getId()).stream()
                .map(AulaResponse::from)
                .toList();
    }

    @Transactional
    public AulaResponse criarAula(Long turmaId, AulaRequest request, Long usuarioId,
            PerfilUsuario perfil) {
        Turma turma = turmaService.validarAcessoGestao(turmaId, usuarioId, perfil);
        if (turma.isEncerrada() && perfil == PerfilUsuario.PROFESSOR) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Turma encerrada: somente coordenacao/secretaria pode registrar aulas");
        }
        Aula aula = new Aula();
        aula.setTurma(turma);
        aula.setData(request.data());
        aula.setConteudo(request.conteudo());
        aulaRepository.save(aula);
        auditoriaService.registrarAuditoria("CRIAR", "Aula", aula.getId(),
                aula.getData().toString());
        return AulaResponse.from(aula);
    }

    @Transactional
    public List<RegistroFrequenciaResponse> registrarFrequencia(Long aulaId,
            FrequenciaLoteRequest request, Long usuarioId, PerfilUsuario perfil) {
        Aula aula = aulaRepository.findById(aulaId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Aula nao encontrada"));
        Turma turma = turmaService.validarAcessoGestao(
                aula.getTurma().getId(), usuarioId, perfil);
        if (turma.isEncerrada() && perfil == PerfilUsuario.PROFESSOR) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Turma encerrada: alteracao de frequencia exige coordenacao/secretaria");
        }

        List<RegistroFrequenciaResponse> respostas = new ArrayList<>();
        for (FrequenciaLancamentoRequest item : request.registros()) {
            Matricula matricula = matriculaRepository.findById(item.matriculaId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                            "Matricula nao encontrada"));
            if (!matricula.getTurma().getId().equals(turma.getId())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "Matricula nao pertence a turma da aula");
            }
            RegistroFrequencia registro = registroRepository
                    .findByAulaIdAndMatriculaId(aula.getId(), matricula.getId())
                    .orElseGet(() -> {
                        RegistroFrequencia novo = new RegistroFrequencia();
                        novo.setAula(aula);
                        novo.setMatricula(matricula);
                        return novo;
                    });
            registro.setPresente(item.presente());
            registroRepository.save(registro);
            respostas.add(RegistroFrequenciaResponse.from(registro));
        }
        auditoriaService.registrarAuditoria("REGISTRAR_FREQUENCIA", "Aula", aulaId,
                respostas.size() + " registro(s)");
        return respostas;
    }

    @Transactional(readOnly = true)
    public TurmaFrequenciaResponse listarFrequenciaDaTurma(Long turmaId, Long usuarioId,
            PerfilUsuario perfil) {
        Turma turma = turmaService.validarAcessoGestao(turmaId, usuarioId, perfil);
        List<Aula> aulas = aulaRepository.findByTurmaIdOrderByDataAsc(turma.getId());
        List<RegistroFrequencia> todos = registroRepository.findByAulaTurmaId(turma.getId());
        Map<Long, List<RegistroFrequencia>> porMatricula = new HashMap<>();
        for (RegistroFrequencia registro : todos) {
            porMatricula.computeIfAbsent(registro.getMatricula().getId(),
                    chave -> new ArrayList<>()).add(registro);
        }

        List<TurmaFrequenciaResponse.AlunoFrequencia> alunos = new ArrayList<>();
        for (Matricula matricula : matriculaRepository.findByTurmaIdOrderByAlunoNomeAsc(
                turma.getId())) {
            if (matricula.getStatus() == StatusMatricula.CANCELADA) {
                continue;
            }
            List<RegistroFrequencia> registros =
                    porMatricula.getOrDefault(matricula.getId(), List.of());
            long presencas = registros.stream().filter(RegistroFrequencia::isPresente).count();
            alunos.add(new TurmaFrequenciaResponse.AlunoFrequencia(matricula.getId(),
                    matricula.getAluno().getId(), matricula.getAluno().getNome(),
                    matricula.getAluno().getNumeroMatricula(), presencas, registros.size(),
                    CalculoAcademico.frequencia(presencas, registros.size()),
                    registros.stream().map(RegistroFrequenciaResponse::from).toList()));
        }
        return new TurmaFrequenciaResponse(turma.getId(),
                aulas.stream().map(AulaResponse::from).toList(), alunos);
    }

    @Transactional(readOnly = true)
    public List<AlunoFrequenciaResponse> frequenciasDoAluno(Long alunoId) {
        List<AlunoFrequenciaResponse> respostas = new ArrayList<>();
        for (Matricula matricula : matriculaRepository
                .findByAlunoIdOrderByTurmaPeriodoLetivoDesc(alunoId)) {
            if (matricula.getStatus() == StatusMatricula.CANCELADA) {
                continue;
            }
            List<RegistroFrequencia> registros =
                    registroRepository.findByMatriculaId(matricula.getId());
            long presencas = registros.stream().filter(RegistroFrequencia::isPresente).count();
            long total = registros.size();
            BigDecimal percentual = CalculoAcademico.frequencia(presencas, total);
            respostas.add(new AlunoFrequenciaResponse(matricula.getId(),
                    matricula.getTurma().getDisciplina().getNome(),
                    matricula.getTurma().getProfessor().getNome(),
                    matricula.getTurma().getPeriodoLetivo(),
                    total, presencas, total - presencas, percentual,
                    propriedades.getFrequenciaMinima(),
                    CalculoAcademico.alertaFrequencia(percentual, propriedades)));
        }
        return respostas;
    }
}
