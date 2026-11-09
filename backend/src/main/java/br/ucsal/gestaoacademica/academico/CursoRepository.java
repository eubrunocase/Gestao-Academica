package br.ucsal.gestaoacademica.academico;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoRepository extends JpaRepository<Curso, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    List<Curso> findAllByOrderByNomeAsc();
}
