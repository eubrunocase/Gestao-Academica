package br.ucsal.gestaoacademica.config;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "academico")
@Getter
@Setter
public class AcademicProperties {

    private BigDecimal mediaAprovacao = new BigDecimal("7.0");
    private int frequenciaMinima = 75;
    private int alertaFrequenciaMargem = 5;
    private int loginMaxTentativas = 5;
    private int loginBloqueioMinutos = 15;
}
