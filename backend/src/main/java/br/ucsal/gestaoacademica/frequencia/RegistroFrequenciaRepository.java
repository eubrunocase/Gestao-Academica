package br.ucsal.gestaoacademica.frequencia;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroFrequenciaRepository extends JpaRepository<RegistroFrequencia, Long> {

    List<RegistroFrequencia> findByMatriculaId(Long matriculaId);

    List<RegistroFrequencia> findByMatriculaIdIn(Collection<Long> matriculaIds);

    List<RegistroFrequencia> findByAulaId(Long aulaId);

    List<RegistroFrequencia> findByAulaTurmaId(Long turmaId);

    Optional<RegistroFrequencia> findByAulaIdAndMatriculaId(Long aulaId, Long matriculaId);

    boolean existsByMatriculaId(Long matriculaId);
}
