package com.alexcatarau.hba;


import com.alexcatarau.hba.config.IntegrationTestConfig;
import com.alexcatarau.hba.model.request.LoginRequestModel;
import com.alexcatarau.hba.security.JwtAuthenticationFilter;
import com.alexcatarau.hba.security.utils.JwtProperties;
import com.auth0.jwt.JWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jdbi.v3.core.Jdbi;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

import static com.alexcatarau.hba.security.utils.JwtProperties.TOKEN_PREFIX;
import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
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

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;


    @Before
    public void createTestUser() {
        jdbi.withHandle(handle -> handle.createUpdate("INSERT INTO users(id, username, password, roles, permissions, active)\n" +
                "values (1, 'alex_admin', '$2y$10$mbmAkdm6hi7LyVBaGRBwTOgu9I.rTxo80ZUcI/GSTimZN7unr0MbC', 'ADMIN', 'ADMIN', true);")
                .execute());

        jdbi.withHandle(handle -> handle.createUpdate("INSERT INTO users(id, username, password, roles, permissions, active)\n" +
                "values (2, 'alex_user', '$2y$10$mbmAkdm6hi7LyVBaGRBwTOgu9I.rTxo80ZUcI/GSTimZN7unr0MbC', 'USER', 'USER', true);")
                .execute());
    }

    @After
    public void deleteTestUser() {
        jdbi.withHandle(handle -> handle.createUpdate("DELETE FROM users where username = 'alex_admin';")
                .execute());
        jdbi.withHandle(handle -> handle.createUpdate("DELETE FROM users where username = 'alex_user';")
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
    public void givenValidAdminAndPassword_thenCheckAuthorizedPaths() throws Exception {
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

    @Test
    public void givenValidUserAndPassword_thenCheckAuthorizedPaths() throws Exception {
        String jsonBody = objectMapper.writeValueAsString(new LoginRequestModel("alex_user", "Password123"));
        MvcResult mvcResult = mockMvc.perform(post("http://localhost:8080/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk())
                .andReturn();

        String token = mvcResult.getResponse().getHeader("Authorization");

        mockMvc.perform(get("http://localhost:8080/user/test")
                .header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    public void givenTokenWithNoUsername_thenIsForbidden() throws Exception {

        mockMvc.perform(get("http://localhost:8080/admin/test"))
                .andExpect(status().isForbidden());

    }

    @Test
    public void givenTokenWithUnknownUsername_thenIsForbidden() throws Exception {
        String token = TOKEN_PREFIX + JWT.create()
                .withSubject("fake_username")
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.EXPIRATION_TIME))
                .sign(HMAC512(JwtProperties.SECRET.getBytes()));
        mockMvc.perform(get("http://localhost:8080/admin/test")
                .header("Authorization", token))
                .andExpect(status().isForbidden());

    }

    @Test
    public void givenNullUsername_thenIsForbidden() throws Exception {
        String token = TOKEN_PREFIX + JWT.create()
                .withSubject(null)
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.EXPIRATION_TIME))
                .sign(HMAC512(JwtProperties.SECRET.getBytes()));
        mockMvc.perform(get("http://localhost:8080/admin/test")
                .header("Authorization", token))
                .andExpect(status().isForbidden());

    }

    @Test
    public void givenEmptyToken_thenIsForbidden() throws Exception {
        String token = TOKEN_PREFIX + "";
        mockMvc.perform(get("http://localhost:8080/admin/test")
                .header("Authorization", token))
                .andExpect(status().isForbidden());

    }

    @Test
    public void givenRequestWithBadHeader_thenIsForbidden() throws Exception {
        String token = TOKEN_PREFIX + JWT.create()
                .withSubject("fake_username")
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.EXPIRATION_TIME))
                .sign(HMAC512(JwtProperties.SECRET.getBytes()));
        mockMvc.perform(get("http://localhost:8080/admin/test")
                .header("autoriz", token))
                .andExpect(status().isForbidden());

    }

    @Test
    public void givenNullToken_thenIsForbidden() throws Exception {
        mockMvc.perform(get("http://localhost:8080/admin/test")
                .header("Authorization", ""))
                .andExpect(status().isForbidden());

    }

    @Test
    public void attemptAuthentication_whenReadingRequest_thenThrowIOexception() throws IOException {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(new AuthenticationManager() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                return null;
            }
        });
        when(request.getInputStream()).thenThrow(IOException.class);
        Authentication authentication = jwtAuthenticationFilter.attemptAuthentication(request, response);
        assertNull(authentication);
    }


}
