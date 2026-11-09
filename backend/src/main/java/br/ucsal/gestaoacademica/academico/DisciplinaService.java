package br.ucsal.gestaoacademica.academico;

import br.ucsal.gestaoacademica.auditoria.AuditoriaService;
import br.ucsal.gestaoacademica.common.BusinessException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DisciplinaService {

    private final DisciplinaRepository disciplinaRepository;
    private final CursoRepository cursoRepository;
    private final TurmaRepository turmaRepository;
    private final AuditoriaService auditoriaService;

    public DisciplinaService(DisciplinaRepository disciplinaRepository,
            CursoRepository cursoRepository, TurmaRepository turmaRepository,
            AuditoriaService auditoriaService) {
        this.disciplinaRepository = disciplinaRepository;
        this.cursoRepository = cursoRepository;
        this.turmaRepository = turmaRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<DisciplinaResponse> listar(Long cursoId) {
        List<Disciplina> disciplinas = cursoId == null
                ? disciplinaRepository.findAll()
                : disciplinaRepository.findByCursoIdOrderByNomeAsc(cursoId);
        return disciplinas.stream()
                .sorted(java.util.Comparator.comparing(Disciplina::getNome))
                .map(DisciplinaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DisciplinaResponse buscar(Long id) {
        return DisciplinaResponse.from(obter(id));
    }

    @Transactional
    public DisciplinaResponse criar(DisciplinaRequest request) {
        Curso curso = buscarCurso(request.cursoId());
        if (disciplinaRepository.existsByNomeIgnoreCaseAndCursoId(request.nome(), curso.getId())) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "Ja existe disciplina com este nome no curso");
        }
        Disciplina disciplina = new Disciplina();
        aplicar(disciplina, request, curso);
        disciplinaRepository.save(disciplina);
        auditoriaService.registrarAuditoria("CRIAR", "Disciplina", disciplina.getId(),
                disciplina.getNome());
        return DisciplinaResponse.from(disciplina);
    }

    @Transactional
    public DisciplinaResponse atualizar(Long id, DisciplinaRequest request) {
        Disciplina disciplina = obter(id);
        Curso curso = buscarCurso(request.cursoId());
        if (!disciplina.getNome().equalsIgnoreCase(request.nome())
                && disciplinaRepository.existsByNomeIgnoreCaseAndCursoId(
                        request.nome(), curso.getId())) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "Ja existe disciplina com este nome no curso");
        }
        aplicar(disciplina, request, curso);
        disciplinaRepository.save(disciplina);
        auditoriaService.registrarAuditoria("ATUALIZAR", "Disciplina", id, disciplina.getNome());
        return DisciplinaResponse.from(disciplina);
    }

    @Transactional
    public void excluir(Long id) {
        Disciplina disciplina = obter(id);
        if (turmaRepository.existsByDisciplinaId(id)) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "Disciplina possui turmas vinculadas e nao pode ser excluida");
        }
        if (disciplinaRepository.countByPrerequisitosId(id) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "Disciplina e pre-requisito de outra disciplina e nao pode ser excluida");
        }
        disciplinaRepository.delete(disciplina);
        auditoriaService.registrarAuditoria("EXCLUIR", "Disciplina", id, disciplina.getNome());
    }

    Disciplina obter(Long id) {
        return disciplinaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "Disciplina nao encontrada"));
    }

    private Curso buscarCurso(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Curso nao encontrado"));
    }

    private void aplicar(Disciplina disciplina, DisciplinaRequest request, Curso curso) {
        disciplina.setNome(request.nome());
        disciplina.setCargaHoraria(request.cargaHoraria());
        disciplina.setCurso(curso);

        Set<Disciplina> prerequisitos = new HashSet<>();
        if (request.prerequisitoIds() != null) {
            for (Long prerequisitoId : request.prerequisitoIds()) {
                if (prerequisitoId == null) {
                    continue;
                }
                if (disciplina.getId() != null && disciplina.getId().equals(prerequisitoId)) {
                    throw new BusinessException("Uma disciplina nao pode ser pre-requisito de si mesma");
                }
                prerequisitos.add(obter(prerequisitoId));
            }
        }
        disciplina.setPrerequisitos(prerequisitos);

        for (Disciplina prerequisito : prerequisitos) {
            if (alcanca(prerequisito, disciplina.getId(), new HashSet<>())) {
                throw new BusinessException(
                        "Pre-requisitos formam um ciclo e nao podem ser cadastrados");
            }
        }
    }

    private boolean alcanca(Disciplina atual, Long alvo, Set<Long> visitados) {
        if (atual.getId() != null && atual.getId().equals(alvo)) {
            return true;
        }
        if (!visitados.add(atual.getId())) {
            return false;
        }
        for (Disciplina prerequisito : atual.getPrerequisitos()) {
            if (alcanca(prerequisito, alvo, visitados)) {
                return true;
            }
        }
        return false;
    }
}
