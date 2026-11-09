package br.ucsal.gestaoacademica.comunicacao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvisoRepository extends JpaRepository<Aviso, Long> {

    List<Aviso> findAllByOrderByCriadoEmDesc();
}
