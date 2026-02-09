package cz.phsoft.hokej;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Hlavní spouštěcí třída aplikace Hokej – HobbyHokej.
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
@EnableJpaRepositories
@EnableScheduling
@EnableAsync
public class HobbyHokejApplication {

    /**
     * Hlavní vstupní metoda aplikace.
     *
     * Spustí Spring Boot, inicializuje aplikační kontext
     * a nahodí všechny nakonfigurované komponenty.
     *
     * @param args argumenty příkazové řádky předané aplikaci
     */
    public static void main(String[] args) {
        SpringApplication.run(HobbyHokejApplication.class, args);
    }
}
