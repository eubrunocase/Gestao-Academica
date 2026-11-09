package br.ucsal.gestaoacademica.frequencia;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AulaRepository extends JpaRepository<Aula, Long> {

    List<Aula> findByTurmaIdOrderByDataAsc(Long turmaId);

    boolean existsByTurmaId(Long turmaId);
}
