package cz.phsoft.hokej.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Aplikační konfigurace pro sdílené Spring beany.
 *
 * V této třídě se definují technické komponenty, které jsou spravovány
 * Spring kontejnerem a používají se napříč aplikací. Třída neobsahuje
 * business logiku a slouží výhradně pro registraci infrastrukturních bean.
 */
@Configuration
public class AppConfig {

    /**
     * Vytváří instanci aplikační konfigurace.
     *
     * Konstruktor je prázdný, protože inicializace probíhá prostřednictvím
     * Spring kontejneru a není vyžadována žádná vlastní logika.
     */
    public AppConfig() {
        // bez vlastní logiky
    }

    /**
     * Vytváří a registruje instanci RestTemplate.
     *
     * Tato instance se používá pro synchronní volání externích HTTP API
     * z aplikační logiky, typicky ze service vrstvy.
     *
     * @return instance RestTemplate spravovaná Spring kontejnerem
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}