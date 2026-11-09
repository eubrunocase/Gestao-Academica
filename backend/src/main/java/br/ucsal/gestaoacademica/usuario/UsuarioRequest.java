package br.ucsal.gestaoacademica.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank(message = "Informe o nome") @Size(max = 120) String nome,
        @NotBlank(message = "Informe o documento") @Size(max = 30) String documento,
        @NotBlank(message = "Informe o e-mail") @Email(message = "E-mail invalido") String email,
        @NotBlank(message = "Informe a senha") @Size(min = 6, message = "A senha deve ter ao menos 6 caracteres") String senha,
        @NotNull(message = "Informe o perfil") PerfilUsuario perfil,
        String numeroMatricula,
        String registroFuncional,
        Boolean ativo) {
}
