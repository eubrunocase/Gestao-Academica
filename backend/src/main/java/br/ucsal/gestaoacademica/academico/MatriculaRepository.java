package br.ucsal.gestaoacademica.academico;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    List<Matricula> findByAlunoIdOrderByTurmaPeriodoLetivoDesc(Long alunoId);

    List<Matricula> findByTurmaIdOrderByAlunoNomeAsc(Long turmaId);

    List<Matricula> findByTurmaIdAndStatus(Long turmaId, StatusMatricula status);

    List<Matricula> findByTurmaIdAndStatusNot(Long turmaId, StatusMatricula status);

    List<Matricula> findByTurmaIdIn(Collection<Long> turmaIds);

    List<Matricula> findByAlunoIdAndStatus(Long alunoId, StatusMatricula status);

    Optional<Matricula> findByAlunoIdAndTurmaId(Long alunoId, Long turmaId);

    boolean existsByAlunoIdAndTurmaId(Long alunoId, Long turmaId);

    boolean existsByAlunoIdAndTurmaIdAndStatusNot(
            Long alunoId, Long turmaId, StatusMatricula status);

    boolean existsByAlunoIdAndTurmaDisciplinaIdAndStatus(
            Long alunoId, Long disciplinaId, StatusMatricula status);

    long countByTurmaIdAndStatus(Long turmaId, StatusMatricula status);

    boolean existsByTurmaId(Long turmaId);

    boolean existsByTurmaDisciplinaId(Long disciplinaId);
}
