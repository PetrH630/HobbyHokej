package cz.phsoft.hokej.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Aplikační konfigurace pro sdílené Spring beany.
 *
 * V této třídě se definují technické komponenty,
 * které se používají napříč aplikací.
 *
 * V současné době se zde vytváří instance {@link RestTemplate},
 * která slouží pro volání externích HTTP služeb.
 */
@Configuration
public class AppConfig {

    /**
     * Vytváří instanci aplikační konfigurace.
     *
     * Konstruktor je prázdný, protože konfigurace je spravována
     * Spring kontejnerem a neobsahuje žádnou vlastní logiku inicializace.
     */
    public AppConfig() {
        // bez vlastní logiky
    }

    /**
     * Vytváří a registruje instanci {@link RestTemplate}.
     *
     * Tato instance se používá pro synchronní volání
     * externích HTTP API z aplikační logiky,
     * typicky ze service vrstvy.
     *
     * @return nová instance {@link RestTemplate} spravovaná Spring kontejnerem
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
