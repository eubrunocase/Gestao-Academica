package br.ucsal.gestaoacademica.academico;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "calendario_academico")
@Getter
@Setter
@NoArgsConstructor
public class CalendarioAcademico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "periodo_letivo", nullable = false, unique = true, length = 10)
    private String periodoLetivo;

    @Column(name = "inicio_matricula", nullable = false)
    private LocalDate inicioMatricula;

    @Column(name = "fim_matricula", nullable = false)
    private LocalDate fimMatricula;

    @Column(name = "fim_periodo", nullable = false)
    private LocalDate fimPeriodo;
}
