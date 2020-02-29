package com.alexcatarau.hba.controller;

import com.alexcatarau.hba.config.IntegrationTestConfig;
import com.alexcatarau.hba.model.request.LoginRequestModel;

import com.alexcatarau.hba.model.request.SetupPasswordRequestModel;
import com.alexcatarau.hba.security.utils.JwtProperties;
import com.alexcatarau.hba.security.utils.JwtUtils;
import com.alexcatarau.hba.service.EmailService;
import com.alexcatarau.hba.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import javax.servlet.http.Cookie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
@ContextConfiguration(classes = IntegrationTestConfig.class)
public class HomeControllerTest {

    @MockBean
    private EmailService emailService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Jdbi jdbi;


    @Before
    public void createTestUser() {
        jdbi.withHandle(handle -> handle.createUpdate("INSERT INTO members(id, first_name, last_name, username, email, employee_number,password, roles, permissions, active)\n" +
                "values (30, 'alex', 'alexandru', 'aleale', 'alex92@mail.com', 10011092,'$2y$10$mbmAkdm6hi7LyVBaGRBwTOgu9I.rTxo80ZUcI/GSTimZN7unr0MbC', 'ADMIN', 'ADMIN', true);")
                .execute());
        jdbi.withHandle(handle -> handle.createUpdate("INSERT INTO members(id, first_name, last_name, email, username, employee_number,password, roles, permissions, active)\n" +
                "values (31, 'john', 'doe' ,'john99@mail.com','johdoe', 10011093,'$2y$10$mbmAkdm6hi7LyVBaGRBwTOgu9I.rTxo80ZUcI/GSTimZN7unr0MbC', 'USER', 'USER', true);")
                .execute());
    }

    @After
    public void deleteTestUser() {
        jdbi.withHandle(handle -> handle.createUpdate("DELETE FROM members where username = 'aleale';")
                .execute());
        jdbi.withHandle(handle -> handle.createUpdate("DELETE FROM members where username = 'johdoe';")
                .execute());
    }

    @Test
    public void getUserInfo_givenValidCookie_thenReturnUserInfo() throws Exception {
        String jsonBody = objectMapper.writeValueAsString(new LoginRequestModel("aleale", "Password123"));
        MvcResult mvcResult = mockMvc.perform(post("http://localhost:8080/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk())
                .andReturn();

        Cookie cookie = mvcResult.getResponse().getCookie("jwt");
        mockMvc.perform(get("http://localhost:8080")
                .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("aleale"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    public  void resetPassword_givenEmail_thenLockAccountAndResetPasswordPending() throws Exception {
        doNothing().when(emailService).sendEmailNewAccountLink(any(),any(), any());
        mockMvc.perform(get("http://localhost:8080/forgot-password?email=john99@mail.com"))
                .andExpect(status().isOk());

    }

    @Test
    public void setupNewPassword_givenSetupPasswordModel_thenReturnIsOk() throws Exception {
        memberService.lockAccountAndSetAccountToResetPasswordPending("john99@mail.com");
        SetupPasswordRequestModel model = new SetupPasswordRequestModel();
        String token = JwtUtils.createJwtToken("31", JwtProperties.ONE_DAY_EXPIRATION_TIME);
        model.setToken(token);
        model.setPassword("newPassword");

        String jsonBody = objectMapper.writeValueAsString(model);

        mockMvc.perform(post("http://localhost:8080/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk());
    }

    @Test
    public void setupNewPassword_givenAccountNotPendingResetPassword_thenReturnIsBadRequest() throws Exception {
        SetupPasswordRequestModel model = new SetupPasswordRequestModel();
        String token = JwtUtils.createJwtToken("31", JwtProperties.ONE_DAY_EXPIRATION_TIME);
        model.setToken(token);
        model.setPassword("newPassword");

        String jsonBody = objectMapper.writeValueAsString(model);

        mockMvc.perform(post("http://localhost:8080/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void validateToken_givenAccountPendingResetPasswordAndValidToken_thenReturnIsOk() throws Exception {
        memberService.lockAccountAndSetAccountToResetPasswordPending("john99@mail.com");
        String token = JwtUtils.createJwtToken("31", JwtProperties.ONE_DAY_EXPIRATION_TIME);

        mockMvc.perform(get("http://localhost:8080/validate-reset-password-token?token=" + token))
                .andExpect(status().isOk());
    }

    @Test
    public void validateToken_givenAccountNotPendingResetPassword_thenReturnIsNotFound() throws Exception {
        String token = JwtUtils.createJwtToken("31", JwtProperties.ONE_DAY_EXPIRATION_TIME);

        mockMvc.perform(get("http://localhost:8080/validate-reset-password-token?token=" + token))
                .andExpect(status().isNotFound());
    }

}
