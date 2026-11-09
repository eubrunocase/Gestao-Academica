package br.ucsal.gestaoacademica.common;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Erro especifico da matricula (HTTP 422), com os dados que a UI usa para
 * sugerir disciplinas pendentes e turmas alternativas.
 */
public record ErroMatricula(
        LocalDateTime timestamp,
        int status,
        String mensagem,
        String motivo,
        List<DisciplinaPendente> disciplinasPendentes,
        List<TurmaAlternativa> turmasAlternativas) {

    public static ErroMatricula of(String motivo, String mensagem,
            List<DisciplinaPendente> pendentes, List<TurmaAlternativa> alternativas) {
        return new ErroMatricula(LocalDateTime.now(), 422, mensagem, motivo,
                pendentes == null ? List.of() : pendentes,
                alternativas == null ? List.of() : alternativas);
    }

    public record DisciplinaPendente(Long id, String nome) {
    }

    public record TurmaAlternativa(Long id, String disciplina, String professor,
            String periodoLetivo, long vagasDisponiveis) {
    }
}
