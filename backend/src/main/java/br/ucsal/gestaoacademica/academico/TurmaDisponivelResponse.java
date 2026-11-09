package br.ucsal.gestaoacademica.academico;

public record TurmaDisponivelResponse(
        Long id,
        Long disciplinaId,
        String disciplina,
        String curso,
        String professor,
        String periodoLetivo,
        int cargaHoraria,
        int limiteVagas,
        long vagasOcupadas,
        long vagasDisponiveis) {
}
