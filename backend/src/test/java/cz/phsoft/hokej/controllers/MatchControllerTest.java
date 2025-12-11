package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.StaraGardaApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = StaraGardaApplication.class
)
@AutoConfigureMockMvc
public class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ---------------------------------------------------------
    // GET /api/matches – pouze MANAGER + ADMIN
    // ---------------------------------------------------------

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getAllMatches_asAdmin_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/matches"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"MANAGER"})
    void getAllMatches_asManager_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/matches"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void getAllMatches_asPlayer_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/matches"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void getAllMatches_unauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/matches"))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------
    // GET /api/matches/next – musí být přihlášený kdokoliv
    // ---------------------------------------------------------

    @Test
    @WithMockUser
    void getNextMatch_authenticated_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/matches/next"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void getNextMatch_unauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/matches/next"))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------
    // POST /api/matches – ADMIN
    // ---------------------------------------------------------

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createMatch_asAdmin_shouldReturn200() throws Exception {

        String json = """
                {
                    "dateTime": "2030-01-01T18:00:00",
                    "location": "Brno",
                    "description": "Testovací zápas",
                    "maxPlayers": 20,
                    "price": 150
                }
                """;

        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"MANAGER"})
    void createMatch_asManager_shouldReturn403() throws Exception {

        String json = """
                {
                    "dateTime": "2030-01-01T18:00:00",
                    "location": "Brno",
                    "description": "Testovací zápas",
                    "maxPlayers": 20,
                    "price": 150
                }
                """;

        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void createMatch_unauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
