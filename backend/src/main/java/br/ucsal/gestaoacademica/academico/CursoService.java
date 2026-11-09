package br.ucsal.gestaoacademica.academico;

import br.ucsal.gestaoacademica.auditoria.AuditoriaService;
import br.ucsal.gestaoacademica.common.BusinessException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CursoService {

    private final CursoRepository cursoRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final TurmaRepository turmaRepository;
    private final AuditoriaService auditoriaService;

    public CursoService(CursoRepository cursoRepository, DisciplinaRepository disciplinaRepository,
            TurmaRepository turmaRepository, AuditoriaService auditoriaService) {
        this.cursoRepository = cursoRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.turmaRepository = turmaRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<CursoResponse> listar() {
        return cursoRepository.findAllByOrderByNomeAsc().stream().map(CursoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CursoResponse buscar(Long id) {
        return CursoResponse.from(obter(id));
    }

    @Transactional
    public CursoResponse criar(CursoRequest request) {
        if (cursoRepository.existsByNomeIgnoreCase(request.nome())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Curso ja cadastrado");
        }
        Curso curso = new Curso();
        curso.setNome(request.nome());
        cursoRepository.save(curso);
        auditoriaService.registrarAuditoria("CRIAR", "Curso", curso.getId(), curso.getNome());
        return CursoResponse.from(curso);
    }

    @Transactional
    public CursoResponse atualizar(Long id, CursoRequest request) {
        Curso curso = obter(id);
        if (!curso.getNome().equalsIgnoreCase(request.nome())
                && cursoRepository.existsByNomeIgnoreCase(request.nome())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Curso ja cadastrado");
        }
        curso.setNome(request.nome());
        cursoRepository.save(curso);
        auditoriaService.registrarAuditoria("ATUALIZAR", "Curso", id, curso.getNome());
        return CursoResponse.from(curso);
    }

    @Transactional
    public void excluir(Long id) {
        Curso curso = obter(id);
        if (disciplinaRepository.countByCursoId(id) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "Curso possui disciplinas vinculadas e nao pode ser excluido");
        }
        if (turmaRepository.existsByDisciplinaCursoId(id)) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "Curso possui turmas vinculadas e nao pode ser excluido");
        }
        cursoRepository.delete(curso);
        auditoriaService.registrarAuditoria("EXCLUIR", "Curso", id, curso.getNome());
    }

    Curso obter(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Curso nao encontrado"));
    }
}
