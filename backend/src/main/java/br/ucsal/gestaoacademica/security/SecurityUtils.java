package br.ucsal.gestaoacademica.security;

import br.ucsal.gestaoacademica.common.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UsuarioPrincipal principal() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || !(autenticacao.getPrincipal() instanceof UsuarioPrincipal principal)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Nao autenticado");
        }
        return principal;
    }

    public static Long usuarioId() {
        return principal().getId();
    }

    public static boolean isStaff() {
        return hasAnyRole("ADMIN", "SECRETARIA", "COORDENADOR");
    }

    public static boolean hasAnyRole(String... perfis) {
        UsuarioPrincipal principal = principal();
        for (String perfil : perfis) {
            if (principal.getPerfil().name().equals(perfil)) {
                return true;
            }
        }
        return false;
    }
}
