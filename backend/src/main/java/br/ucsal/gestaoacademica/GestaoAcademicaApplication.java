package br.ucsal.gestaoacademica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GestaoAcademicaApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestaoAcademicaApplication.class, args);
    }
}
