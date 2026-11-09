package br.ucsal.gestaoacademica.usuario;

public record UsuarioResponse(
        Long id,
        String nome,
        String documento,
        String email,
        PerfilUsuario perfil,
        boolean ativo,
        String numeroMatricula,
        String registroFuncional) {

    public static UsuarioResponse from(Usuario usuario) {
        String numeroMatricula = usuario instanceof Aluno aluno ? aluno.getNumeroMatricula() : null;
        String registroFuncional = usuario instanceof Professor professor
                ? professor.getRegistroFuncional() : null;
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getDocumento(),
                usuario.getEmail(), usuario.getPerfil(), usuario.isAtivo(),
                numeroMatricula, registroFuncional);
    }
}
