package cz.phsoft.hokej;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Hlavní spouštěcí třída aplikace Hokej – Stará Garda.
 *
 * Odpovědnost třídy:
 * - startuje Spring Boot aplikaci,
 * - aktivuje Spring kontext,
 * - zapíná podporu JPA repozitářů, plánování úloh a asynchronního zpracování.
 *
 * Třída neobsahuje žádnou business logiku. Slouží pouze jako vstupní bod
 * pro JVM a konfiguraci základních Spring funkcí na úrovni aplikace.
 */
@SpringBootApplication
@EnableJpaRepositories // aktivuje podporu JPA repozitářů v aplikaci
@EnableScheduling      // umožňuje spouštění plánovaných úloh (cron, fixedRate apod.)
@EnableAsync           // umožňuje asynchronní metody označené anotací @Async
public class StaraGardaApplication {

    /**
     * Hlavní vstupní metoda aplikace.
     *
     * Spustí Spring Boot, inicializuje aplikační kontext
     * a nahodí všechny nakonfigurované komponenty.
     *
     * @param args argumenty příkazové řádky předané aplikaci
     */
    public static void main(String[] args) {
        SpringApplication.run(StaraGardaApplication.class, args);
    }
}
