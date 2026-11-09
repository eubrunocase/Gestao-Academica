package br.ucsal.gestaoacademica.usuario;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    Optional<Aluno> findByNumeroMatricula(String numeroMatricula);

    boolean existsByNumeroMatricula(String numeroMatricula);
}
