package cz.phsoft.hokej.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Konfigurace systémového času.
 *
 * Poskytuje aplikační Clock, který se používá pro práci s časem
 * napříč aplikací, aby bylo možné čas snadno testovat a udržet
 * konzistentní nastavení časové zóny.
 */
@Configuration
public class TimeConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Europe/Prague"));
    }
}