package br.ucsal.gestaoacademica.usuario;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    Optional<Professor> findByRegistroFuncional(String registroFuncional);

    boolean existsByRegistroFuncional(String registroFuncional);
}
