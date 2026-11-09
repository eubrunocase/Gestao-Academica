package br.ucsal.gestaoacademica.common;

import java.util.List;
import lombok.Getter;

@Getter
public class MatriculaException extends RuntimeException {

    private final Motivo motivo;
    private final List<ErroMatricula.DisciplinaPendente> disciplinasPendentes;
    private final List<ErroMatricula.TurmaAlternativa> turmasAlternativas;

    public MatriculaException(Motivo motivo, String mensagem,
            List<ErroMatricula.DisciplinaPendente> disciplinasPendentes,
            List<ErroMatricula.TurmaAlternativa> turmasAlternativas) {
        super(mensagem);
        this.motivo = motivo;
        this.disciplinasPendentes = disciplinasPendentes == null ? List.of() : disciplinasPendentes;
        this.turmasAlternativas = turmasAlternativas == null ? List.of() : turmasAlternativas;
    }

    public enum Motivo {
        PREREQUISITO,
        SEM_VAGA,
        FORA_DO_PRAZO,
        TURMA_ENCERRADA,
        JA_MATRICULADO
    }
}
