package cz.phsoft.hokej.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Aplikační konfigurace pro sdílené Spring beany.
 *
 * V této třídě se definují technické komponenty, které jsou využívány
 * napříč aplikací. V současné době se zde vytváří instance {@link RestTemplate}
 * pro volání externích HTTP služeb.
 */
@Configuration
public class AppConfig {

    /**
     * Vytváří instanci {@link RestTemplate}.
     *
     * Tato instance se používá pro synchronní volání externích HTTP API
     * z jiných částí aplikace (například z service vrstvy).
     *
     * @return nová instance RestTemplate spravovaná Spring kontejnereem
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
