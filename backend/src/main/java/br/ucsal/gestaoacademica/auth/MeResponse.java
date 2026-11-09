package br.ucsal.gestaoacademica.auth;

import br.ucsal.gestaoacademica.usuario.PerfilUsuario;

public record MeResponse(
        Long id,
        String nome,
        String documento,
        String email,
        PerfilUsuario perfil,
        boolean ativo,
        String numeroMatricula,
        String registroFuncional) {
}
