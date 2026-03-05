package cz.phsoft.hokej.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.phsoft.hokej.user.entities.AppUserEntity;
import cz.phsoft.hokej.user.enums.Role;
import cz.phsoft.hokej.user.repositories.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class AbstractIntegrationTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;

    @Autowired protected AppUserRepository appUserRepository;
    @Autowired protected PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDb() {
        // Pokud máš v projektu FK vazby, je lepší čistit cíleně.
        // Minimálně uživatele pro login testy.
        appUserRepository.deleteAll();
    }

    protected AppUserEntity createUser(String email, String rawPassword, Role role, boolean enabled) {
        AppUserEntity u = new AppUserEntity();
        u.setName("Test");
        u.setSurname("User");
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setRole(role);
        u.setEnabled(enabled);

        AppUserEntity saved = appUserRepository.save(u);
        assertThat(saved.getId()).isNotNull();
        return saved;
    }
}