package com.alexcatarau.hba.controller;

import com.alexcatarau.hba.config.IntegrationTestConfig;
import com.alexcatarau.hba.model.database.UserDatabaseModel;
import com.alexcatarau.hba.model.request.LoginRequestModel;
import com.alexcatarau.hba.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Optional;

import static org.mockito.Mockito.when;
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
    private UserService userService;

    @Test
    public void getUserInfo_givenValidCookie_thenReturnUserInfo() throws Exception {
        String jsonBody = objectMapper.writeValueAsString(new LoginRequestModel("alex_admin", "Password123"));
        MvcResult mvcResult = mockMvc.perform(post("http://localhost:8080/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk())
                .andReturn();

        UserDatabaseModel userDatabaseModel = new UserDatabaseModel();
        userDatabaseModel.setUsername("alex_admin");
        userDatabaseModel.setRoles("ADMIN");
        when(userService.findByUsername("alex_admin")).thenReturn(Optional.of(userDatabaseModel));
        Cookie cookie = mvcResult.getResponse().getCookie("jwt");
        mockMvc.perform(get("http://localhost:8080")
                .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alex_admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
}
