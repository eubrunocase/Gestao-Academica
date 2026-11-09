package br.ucsal.gestaoacademica.academico;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {

    List<Disciplina> findByCursoIdOrderByNomeAsc(Long cursoId);

    List<Disciplina> findByIdIn(Collection<Long> ids);

    long countByCursoId(Long cursoId);

    boolean existsByNomeIgnoreCaseAndCursoId(String nome, Long cursoId);

    long countByPrerequisitosId(Long disciplinaId);
}
