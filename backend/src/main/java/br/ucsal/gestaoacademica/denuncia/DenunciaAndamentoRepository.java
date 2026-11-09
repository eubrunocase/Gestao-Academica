package br.ucsal.gestaoacademica.denuncia;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DenunciaAndamentoRepository extends JpaRepository<DenunciaAndamento, Long> {

    List<DenunciaAndamento> findByDenunciaIdOrderByCriadoEmAsc(Long denunciaId);
}
