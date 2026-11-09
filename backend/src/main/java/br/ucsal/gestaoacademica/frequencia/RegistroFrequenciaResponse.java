package br.ucsal.gestaoacademica.frequencia;

public record RegistroFrequenciaResponse(Long id, Long aulaId, Long matriculaId, boolean presente) {

    public static RegistroFrequenciaResponse from(RegistroFrequencia registro) {
        return new RegistroFrequenciaResponse(registro.getId(),
                registro.getAula().getId(), registro.getMatricula().getId(),
                registro.isPresente());
    }
}
