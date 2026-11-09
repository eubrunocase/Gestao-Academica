package br.ucsal.gestaoacademica.avaliacao;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    List<Avaliacao> findByTurmaIdOrderByDescricaoAsc(Long turmaId);

    List<Avaliacao> findByIdIn(Collection<Long> ids);

    Optional<Avaliacao> findByIdAndTurmaId(Long id, Long turmaId);

    boolean existsByTurmaId(Long turmaId);
}
