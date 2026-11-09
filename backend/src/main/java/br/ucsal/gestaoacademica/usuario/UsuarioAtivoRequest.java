package br.ucsal.gestaoacademica.usuario;

import jakarta.validation.constraints.NotNull;

public record UsuarioAtivoRequest(@NotNull(message = "Informe ativo") Boolean ativo) {
}
