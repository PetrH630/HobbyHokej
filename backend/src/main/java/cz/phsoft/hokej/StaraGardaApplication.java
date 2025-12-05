package cz.phsoft.hokej;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories // JPA v našem Spring Boot projektu
public class StaraGardaApplication {
    public static void main(String[] args) {
        SpringApplication.run(StaraGardaApplication.class, args);


        }
    }
