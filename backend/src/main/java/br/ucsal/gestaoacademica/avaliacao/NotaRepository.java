package br.ucsal.gestaoacademica.avaliacao;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotaRepository extends JpaRepository<Nota, Long> {

    List<Nota> findByMatriculaId(Long matriculaId);

    List<Nota> findByMatriculaIdIn(Collection<Long> matriculaIds);

    List<Nota> findByAvaliacaoTurmaId(Long turmaId);

    Optional<Nota> findByAvaliacaoIdAndMatriculaId(Long avaliacaoId, Long matriculaId);

    boolean existsByMatriculaId(Long matriculaId);

    boolean existsByAvaliacaoId(Long avaliacaoId);
}
