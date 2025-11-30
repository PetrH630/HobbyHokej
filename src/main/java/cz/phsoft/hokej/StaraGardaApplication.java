package cz.phsoft.hokej;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableJpaRepositories // <-- Přidáním tohoto řádku "aktivujeme" JPA v našem Spring Boot projektu
public class StaraGardaApplication {
    public static void main(String[] args) {
        SpringApplication.run(StaraGardaApplication.class, args);


        }
    }
