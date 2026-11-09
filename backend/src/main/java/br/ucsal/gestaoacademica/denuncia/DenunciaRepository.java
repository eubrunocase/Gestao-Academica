package br.ucsal.gestaoacademica.denuncia;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DenunciaRepository extends JpaRepository<Denuncia, Long> {

    Optional<Denuncia> findByProtocoloIgnoreCase(String protocolo);

    boolean existsByProtocolo(String protocolo);

    List<Denuncia> findAllByOrderByCriadoEmDesc();

    long countByAnonimaTrue();

    @Query("select d.status, count(d) from Denuncia d group by d.status")
    List<Object[]> contarPorStatus();

    @Query("select d.categoria, count(d) from Denuncia d group by d.categoria order by count(d) desc")
    List<Object[]> contarPorCategoria();
}
