package br.ucsal.gestaoacademica.usuario;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByDocumento(String documento);

    Optional<Usuario> findByDocumento(String documento);

    List<Usuario> findAllByOrderByNomeAsc();

    Page<Usuario> findByPerfil(PerfilUsuario perfil, Pageable pageable);

    Page<Usuario> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
