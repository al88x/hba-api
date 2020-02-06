package com.alexcatarau.hba.security;


import com.alexcatarau.hba.config.IntegrationTestConfig;
import com.alexcatarau.hba.model.request.LoginRequestModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jdbi.v3.core.Jdbi;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
@ContextConfiguration(classes = IntegrationTestConfig.class)
public class LoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Jdbi jdbi;

    @Before
    public void createTestUser() {
        jdbi.withHandle(handle -> handle.createUpdate("INSERT INTO users(username, password, roles, permissions, active)\n" +
                "values ('alex_admin', '$2y$10$mbmAkdm6hi7LyVBaGRBwTOgu9I.rTxo80ZUcI/GSTimZN7unr0MbC', 'ADMIN', '', true);")
                .execute());
    }

    @After
    public void deleteTestUser() {
        jdbi.withHandle(handle -> handle.createUpdate("DELETE FROM users where username = 'alex_admin';")
                .execute());
    }

    @Test
    public void givenValidUserAndPassword_thenAuthenticated() throws Exception {
        String jsonBody = objectMapper.writeValueAsString(new LoginRequestModel("alex_admin", "Password123"));
        mockMvc.perform(post("http://localhost:8080/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"));
    }

    @Test
    public void givenUnknownUserAndPassword_thenUnauthorized() throws Exception {
        String jsonBody = objectMapper.writeValueAsString(new LoginRequestModel("alex_admin2", "Password123"));
        mockMvc.perform(post("http://localhost:8080/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void givenValidUserAndPassword_thenCheckAuthorizedPaths() throws Exception {
        String jsonBody = objectMapper.writeValueAsString(new LoginRequestModel("alex_admin", "Password123"));
        MvcResult mvcResult = mockMvc.perform(post("http://localhost:8080/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk())
                .andReturn();

        String token = mvcResult.getResponse().getHeader("Authorization");
        mockMvc.perform(get("http://localhost:8080/admin/test")
                .header("Authorization", token))
                .andExpect(status().isOk());

        mockMvc.perform(get("http://localhost:8080/user/test")
                .header("Authorization", token))
                .andExpect(status().isForbidden());
    }
}
