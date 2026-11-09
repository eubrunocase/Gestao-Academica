package br.ucsal.gestaoacademica.security;

import br.ucsal.gestaoacademica.usuario.PerfilUsuario;
import br.ucsal.gestaoacademica.usuario.Usuario;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UsuarioPrincipal implements UserDetails {

    private final Long id;
    private final String nome;
    private final String email;
    private final String senha;
    private final PerfilUsuario perfil;
    private final boolean ativo;
    private final boolean bloqueado;

    public UsuarioPrincipal(Long id, String nome, String email, String senha,
            PerfilUsuario perfil, boolean ativo, boolean bloqueado) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.perfil = perfil;
        this.ativo = ativo;
        this.bloqueado = bloqueado;
    }

    public static UsuarioPrincipal from(Usuario usuario) {
        boolean bloqueado = usuario.getBloqueadoAte() != null
                && usuario.getBloqueadoAte().isAfter(LocalDateTime.now());
        return new UsuarioPrincipal(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getSenhaHash(),
                usuario.getPerfil(),
                usuario.isAtivo(),
                bloqueado);
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !bloqueado;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }
}
