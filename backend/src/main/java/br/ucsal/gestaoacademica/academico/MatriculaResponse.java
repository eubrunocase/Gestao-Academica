package br.ucsal.gestaoacademica.academico;

import java.time.LocalDate;

public record MatriculaResponse(
        Long id,
        Long turmaId,
        String disciplina,
        String professor,
        String periodoLetivo,
        LocalDate data,
        StatusMatricula status) {

    public static MatriculaResponse from(Matricula matricula) {
        return new MatriculaResponse(matricula.getId(),
                matricula.getTurma().getId(),
                matricula.getTurma().getDisciplina().getNome(),
                matricula.getTurma().getProfessor().getNome(),
                matricula.getTurma().getPeriodoLetivo(),
                matricula.getData(),
                matricula.getStatus());
    }
}
