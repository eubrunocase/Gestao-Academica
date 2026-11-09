package br.ucsal.gestaoacademica.academico;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TurmaRepository extends JpaRepository<Turma, Long> {

    List<Turma> findByProfessorIdOrderByPeriodoLetivoDesc(Long professorId);

    List<Turma> findByPeriodoLetivoAndEncerradaFalse(String periodoLetivo);

    List<Turma> findByDisciplinaIdAndPeriodoLetivoAndEncerradaFalse(
            Long disciplinaId, String periodoLetivo);

    List<Turma> findAllByOrderByPeriodoLetivoDescIdAsc();

    boolean existsByDisciplinaId(Long disciplinaId);

    boolean existsByProfessorId(Long professorId);

    boolean existsByDisciplinaCursoId(Long cursoId);
}
