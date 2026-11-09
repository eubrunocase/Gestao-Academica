package br.ucsal.gestaoacademica.auth;

import br.ucsal.gestaoacademica.usuario.PerfilUsuario;

public record LoginResponse(
        String token,
        String tipo,
        long expiraEmSegundos,
        UsuarioResumo usuario) {

    public record UsuarioResumo(Long id, String nome, String email, PerfilUsuario perfil) {
    }
}
