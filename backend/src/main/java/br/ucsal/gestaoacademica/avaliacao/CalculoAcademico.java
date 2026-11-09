package br.ucsal.gestaoacademica.avaliacao;

import br.ucsal.gestaoacademica.academico.StatusMatricula;
import br.ucsal.gestaoacademica.config.AcademicProperties;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Regras de calculo compartilhadas entre notas, frequencia e relatorios.
 */
public final class CalculoAcademico {

    private CalculoAcademico() {
    }

    public static Map<Long, Nota> indexarPorAvaliacao(List<Nota> notas) {
        Map<Long, Nota> indice = new HashMap<>();
        for (Nota nota : notas) {
            indice.put(nota.getAvaliacao().getId(), nota);
        }
        return indice;
    }

    public static BigDecimal media(List<Avaliacao> avaliacoes, Map<Long, Nota> notasPorAvaliacao) {
        BigDecimal somaPonderada = BigDecimal.ZERO;
        BigDecimal somaPesos = BigDecimal.ZERO;
        for (Avaliacao avaliacao : avaliacoes) {
            Nota nota = notasPorAvaliacao.get(avaliacao.getId());
            if (nota == null) {
                continue;
            }
            somaPonderada = somaPonderada.add(nota.getValor().multiply(avaliacao.getPeso()));
            somaPesos = somaPesos.add(avaliacao.getPeso());
        }
        if (somaPesos.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return somaPonderada.divide(somaPesos, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal notaNecessaria(List<Avaliacao> avaliacoes,
            Map<Long, Nota> notasPorAvaliacao, BigDecimal mediaAlvo) {
        BigDecimal totalPesos = BigDecimal.ZERO;
        BigDecimal pesosLancados = BigDecimal.ZERO;
        BigDecimal somaPonderada = BigDecimal.ZERO;
        for (Avaliacao avaliacao : avaliacoes) {
            totalPesos = totalPesos.add(avaliacao.getPeso());
            Nota nota = notasPorAvaliacao.get(avaliacao.getId());
            if (nota != null) {
                pesosLancados = pesosLancados.add(avaliacao.getPeso());
                somaPonderada = somaPonderada.add(nota.getValor().multiply(avaliacao.getPeso()));
            }
        }
        BigDecimal pesoRestante = totalPesos.subtract(pesosLancados);
        if (totalPesos.compareTo(BigDecimal.ZERO) == 0
                || pesoRestante.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        BigDecimal necessario = mediaAlvo.multiply(totalPesos).subtract(somaPonderada)
                .divide(pesoRestante, 2, RoundingMode.HALF_UP);
        return necessario;
    }

    public static BigDecimal frequencia(long presencas, long registros) {
        if (registros <= 0) {
            return null;
        }
        return BigDecimal.valueOf(presencas)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(registros), 1, RoundingMode.HALF_UP);
    }

    public static StatusMatricula situacao(BigDecimal media, BigDecimal frequencia,
            AcademicProperties propriedades) {
        BigDecimal minima = BigDecimal.valueOf(propriedades.getFrequenciaMinima());
        if (frequencia != null && frequencia.compareTo(minima) < 0) {
            return StatusMatricula.REPROVADO_FALTA;
        }
        if (media == null) {
            return StatusMatricula.ATIVA;
        }
        if (media.compareTo(propriedades.getMediaAprovacao()) >= 0) {
            return StatusMatricula.APROVADO;
        }
        return StatusMatricula.REPROVADO_NOTA;
    }

    public static boolean alertaFrequencia(BigDecimal frequencia, AcademicProperties propriedades) {
        if (frequencia == null) {
            return false;
        }
        BigDecimal limite = BigDecimal.valueOf(propriedades.getFrequenciaMinima());
        BigDecimal margem = BigDecimal.valueOf(propriedades.getAlertaFrequenciaMargem());
        return frequencia.compareTo(limite.add(margem)) < 0;
    }
}
