package cz.phsoft.hokej.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Konfigurace systémového času pro aplikaci.
 *
 * Poskytuje aplikační Clock, který se používá pro práci s časem
 * napříč aplikací. Použití Clock namísto přímého volání statických
 * metod LocalDateTime.now umožňuje snadné testování a jednotné
 * nastavení časové zóny.
 */
@Configuration
public class TimeConfig {

    /**
     * Vytváří a registruje Clock pro časovou zónu Europe/Prague.
     *
     * Tento bean se používá v komponentech, které pracují s aktuálním
     * časem, a zajišťuje konzistentní časové údaje v celé aplikaci.
     *
     * @return systémový Clock pro zónu Europe/Prague
     */
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Europe/Prague"));
    }
}