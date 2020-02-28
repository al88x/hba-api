package com.alexcatarau.hba.controller;

import com.alexcatarau.hba.config.IntegrationTestConfig;
import com.alexcatarau.hba.model.request.LoginRequestModel;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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

import javax.servlet.http.Cookie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
@ContextConfiguration(classes = IntegrationTestConfig.class)
public class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Jdbi jdbi;


    @Before
    public void createTestUser() {
        jdbi.withHandle(handle -> handle.createUpdate("INSERT INTO members(id, username, password, roles, permissions, active)\n" +
                "values (1, 'alex_admin', '$2y$10$mbmAkdm6hi7LyVBaGRBwTOgu9I.rTxo80ZUcI/GSTimZN7unr0MbC', 'ADMIN', 'ADMIN', true);")
                .execute());
    }

    @After
    public void deleteTestUser() {
        jdbi.withHandle(handle -> handle.createUpdate("DELETE FROM members where username = 'alex_admin';")
                .execute());
    }

    @Test
    public void getUserInfo_givenValidCookie_thenReturnUserInfo() throws Exception {
        String jsonBody = objectMapper.writeValueAsString(new LoginRequestModel("alex_admin", "Password123"));
        MvcResult mvcResult = mockMvc.perform(post("http://localhost:8080/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk())
                .andReturn();

        Cookie cookie = mvcResult.getResponse().getCookie("jwt");
        mockMvc.perform(get("http://localhost:8080")
                .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alex_admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
}
