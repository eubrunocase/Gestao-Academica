package br.ucsal.gestaoacademica.academico;

import java.time.LocalDate;

public record CalendarioResponse(
        Long id,
        String periodoLetivo,
        LocalDate inicioMatricula,
        LocalDate fimMatricula,
        LocalDate fimPeriodo,
        boolean matriculaAberta) {

    public static CalendarioResponse from(CalendarioAcademico calendario) {
        LocalDate hoje = LocalDate.now();
        boolean aberta = !hoje.isBefore(calendario.getInicioMatricula())
                && !hoje.isAfter(calendario.getFimMatricula());
        return new CalendarioResponse(calendario.getId(), calendario.getPeriodoLetivo(),
                calendario.getInicioMatricula(), calendario.getFimMatricula(),
                calendario.getFimPeriodo(), aberta);
    }
}
