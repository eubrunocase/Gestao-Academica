package br.ucsal.gestaoacademica.academico;

public record TurmaResponse(
        Long id,
        DisciplinaResumo disciplina,
        ProfessorResumo professor,
        String periodoLetivo,
        int limiteVagas,
        long vagasOcupadas,
        long vagasDisponiveis,
        boolean encerrada) {

    public static TurmaResponse of(Turma turma, long vagasOcupadas) {
        return new TurmaResponse(turma.getId(),
                new DisciplinaResumo(turma.getDisciplina().getId(),
                        turma.getDisciplina().getNome(),
                        turma.getDisciplina().getCargaHoraria()),
                new ProfessorResumo(turma.getProfessor().getId(),
                        turma.getProfessor().getNome()),
                turma.getPeriodoLetivo(), turma.getLimiteVagas(), vagasOcupadas,
                Math.max(0, turma.getLimiteVagas() - vagasOcupadas), turma.isEncerrada());
    }

    public record DisciplinaResumo(Long id, String nome, Integer cargaHoraria) {
    }

    public record ProfessorResumo(Long id, String nome) {
    }
}
