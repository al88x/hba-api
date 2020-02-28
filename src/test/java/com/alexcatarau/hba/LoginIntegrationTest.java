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
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.servlet.http.Cookie;
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
        jdbi.withHandle(handle -> handle.createUpdate("INSERT INTO members(id, username, password, roles, permissions, active)\n" +
                "values (1, 'alex_admin', '$2y$10$mbmAkdm6hi7LyVBaGRBwTOgu9I.rTxo80ZUcI/GSTimZN7unr0MbC', 'ADMIN', 'ADMIN', true);")
                .execute());

        jdbi.withHandle(handle -> handle.createUpdate("INSERT INTO members(id, username, password, roles, permissions, active)\n" +
                "values (2, 'alex_user', '$2y$10$mbmAkdm6hi7LyVBaGRBwTOgu9I.rTxo80ZUcI/GSTimZN7unr0MbC', 'USER', 'USER', true);")
                .execute());
    }

    @After
    public void deleteTestUser() {
        jdbi.withHandle(handle -> handle.createUpdate("DELETE FROM members where username = 'alex_admin';")
                .execute());
        jdbi.withHandle(handle -> handle.createUpdate("DELETE FROM members where username = 'alex_user';")
                .execute());
    }

    @Test
    public void givenValidUserAndPassword_thenAuthenticated() throws Exception {
        String jsonBody = objectMapper.writeValueAsString(new LoginRequestModel("alex_admin", "Password123"));
        mockMvc.perform(post("http://localhost:8080/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"));
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

        //given admin accessing /admin -> return is ok
        Cookie cookie = mvcResult.getResponse().getCookie("jwt");
        mockMvc.perform(get("http://localhost:8080/admin/test")
                .cookie(cookie))
                .andExpect(status().isOk());

        //given admin accessing /user -> return forbidden
        mockMvc.perform(get("http://localhost:8080/user/test")
                .cookie(cookie))
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

        // given a valid name of cookie ->return is ok
        Cookie cookie = mvcResult.getResponse().getCookie("jwt");
        mockMvc.perform(get("http://localhost:8080/user/test")
                .cookie(cookie))
                .andExpect(status().isOk());

        //given a cookie with invalid name ->return is Forbidden
        Cookie cookieWithInvalidName = createFakeCookieFromToken(cookie.getValue(), "invalidName");
        mockMvc.perform(get("http://localhost:8080/user/test")
                .cookie(cookieWithInvalidName))
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenTokenWithNoUsername_thenIsForbidden() throws Exception {

        mockMvc.perform(get("http://localhost:8080/admin/test"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenTokenWithUnknownUsername_thenIsForbidden() throws Exception {
        Cookie cookie = createFakeCookieFromToken(createFakeToken("FakeUsername"), "jwt");
        mockMvc.perform(get("http://localhost:8080/admin/test")
                .cookie(cookie))
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenNullUsername_thenIsForbidden() throws Exception {
        Cookie cookie = createFakeCookieFromToken(createFakeToken(null), "jwt");
        mockMvc.perform(get("http://localhost:8080/admin/test")
                .cookie(cookie))
                .andExpect(status().isForbidden());

    }

    @Test
    public void givenEmptyToken_thenIsForbidden() throws Exception {
        String token = TOKEN_PREFIX + "";
        Cookie cookie = createFakeCookieFromToken(token, "jwt");
        mockMvc.perform(get("http://localhost:8080/admin/test")
                .cookie(cookie))
                .andExpect(status().isForbidden());

    }

    @Test
    public void givenRequestWithBadHeader_thenIsForbidden() throws Exception {
        String token = "InvalidTokenPrefix" + JWT.create()
                .withSubject("alex_user")
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.ONE_DAY_EXPIRATION_TIME))
                .sign(HMAC512(JwtProperties.SECRET.getBytes()));
        Cookie cookie = createFakeCookieFromToken(token, "jwt");

        mockMvc.perform(get("http://localhost:8080/admin/test")
                .cookie(cookie))
                .andExpect(status().isForbidden());

    }


    @Test
    public void attemptAuthentication_whenReadingRequest_thenThrowIOexception() throws IOException {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authentication -> null);
        when(request.getInputStream()).thenThrow(IOException.class);
        Authentication authentication = jwtAuthenticationFilter.attemptAuthentication(request, response);
        assertNull(authentication);
    }

    private String createFakeToken(String username) {
        return TOKEN_PREFIX + JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.ONE_DAY_EXPIRATION_TIME))
                .sign(HMAC512(JwtProperties.SECRET.getBytes()));
    }

    private Cookie createFakeCookieFromToken(String token, String cookieName) {
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(86400);
        return cookie;
    }
}
