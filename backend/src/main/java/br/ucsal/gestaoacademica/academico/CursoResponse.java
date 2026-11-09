package br.ucsal.gestaoacademica.academico;

public record CursoResponse(Long id, String nome) {

    public static CursoResponse from(Curso curso) {
        return new CursoResponse(curso.getId(), curso.getNome());
    }
}
