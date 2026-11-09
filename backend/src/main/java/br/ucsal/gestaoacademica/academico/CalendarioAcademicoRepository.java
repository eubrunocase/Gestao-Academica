package br.ucsal.gestaoacademica.academico;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalendarioAcademicoRepository extends JpaRepository<CalendarioAcademico, Long> {

    List<CalendarioAcademico> findAllByOrderByPeriodoLetivoDesc();

    Optional<CalendarioAcademico> findByPeriodoLetivo(String periodoLetivo);

    Optional<CalendarioAcademico> findByInicioMatriculaLessThanEqualAndFimMatriculaGreaterThanEqual(
            LocalDate inicio, LocalDate fim);

    Optional<CalendarioAcademico> findFirstByOrderByFimPeriodoDesc();
}
