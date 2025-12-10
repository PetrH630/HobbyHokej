package cz.phsoft.hokej;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories // JPA v našem Spring Boot projektu
@EnableScheduling
public class StaraGardaApplication {
    public static void main(String[] args) {
        SpringApplication.run(StaraGardaApplication.class, args);


        }
    }
