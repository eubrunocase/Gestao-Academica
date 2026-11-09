package br.ucsal.gestaoacademica.academico;

import java.util.Comparator;
import java.util.List;

public record DisciplinaResponse(
        Long id,
        String nome,
        Integer cargaHoraria,
        CursoResponse curso,
        List<PrerequisitoResumo> prerequisitos) {

    public static DisciplinaResponse from(Disciplina disciplina) {
        List<PrerequisitoResumo> prerequisitos = disciplina.getPrerequisitos().stream()
                .sorted(Comparator.comparing(Disciplina::getNome))
                .map(p -> new PrerequisitoResumo(p.getId(), p.getNome()))
                .toList();
        return new DisciplinaResponse(disciplina.getId(), disciplina.getNome(),
                disciplina.getCargaHoraria(),
                CursoResponse.from(disciplina.getCurso()), prerequisitos);
    }

    public record PrerequisitoResumo(Long id, String nome) {
    }
}
