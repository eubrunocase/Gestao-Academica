package br.ucsal.gestaoacademica.relatorio;

import br.ucsal.gestaoacademica.avaliacao.BoletimAlunoResponse;

public record BoletimRelatorioResponse(
        Long alunoId,
        String aluno,
        String numeroMatricula,
        BoletimAlunoResponse boletim) {
}
