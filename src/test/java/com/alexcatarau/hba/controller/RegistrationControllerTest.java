package com.alexcatarau.hba.controller;

import com.alexcatarau.hba.config.IntegrationTestConfig;
import com.alexcatarau.hba.model.request.MemberDetailsRequestModel;
import com.alexcatarau.hba.model.request.SetupPasswordRequestModel;
import com.alexcatarau.hba.security.utils.JwtProperties;
import com.alexcatarau.hba.security.utils.JwtUtils;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
@ContextConfiguration(classes = IntegrationTestConfig.class)
public class RegistrationControllerTest {

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
    public void getMemberIdFromToken_givenToken_thenReturnIsOk() throws Exception {
        String token = JwtUtils.createJwtToken("31", JwtProperties.ONE_DAY_EXPIRATION_TIME);
        jdbi.withHandle(handle -> handle.createUpdate("UPDATE members SET pending_account_registration = true, active = false where id=31;")
                .execute());

        mockMvc.perform(get("http://localhost:8080/register/confirm?token=" + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(31));
    }

    @Test
    public void getMemberIdFromToken_givenTokenForMemberNotPendingRegistration_thenReturnIsNotFound() throws Exception {
        String token = JwtUtils.createJwtToken("31", JwtProperties.ONE_DAY_EXPIRATION_TIME);
        jdbi.withHandle(handle -> handle.createUpdate("UPDATE members SET pending_account_registration = false, active = active where id=31;")
                .execute());

        mockMvc.perform(get("http://localhost:8080/register/confirm?token=" + token))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getMemberIdFromToken_givenTokenAndEmployeeNumber_thenReturnIsOk() throws Exception {
        String token = JwtUtils.createJwtToken("31", JwtProperties.ONE_DAY_EXPIRATION_TIME);
        mockMvc.perform(get("http://localhost:8080/register/confirm?employeeNumber=10011093&token=" + token))
                .andExpect(status().isOk());
    }


    @Test
    public void setupNewPassword_givenValidToken_thenReturnIsOk() throws Exception {
        String token = JwtUtils.createJwtToken("31", JwtProperties.ONE_DAY_EXPIRATION_TIME);
        jdbi.withHandle(handle -> handle.createUpdate("UPDATE members SET pending_account_registration = true, active = false where id=31;")
                .execute());
        SetupPasswordRequestModel model = new SetupPasswordRequestModel();
        model.setToken(token);
        model.setPassword("NewPassword1");

        String jsonBody = objectMapper.writeValueAsString(model);

        mockMvc.perform(post("http://localhost:8080/register/confirm/pageTwo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk());

    }
    @Test
    public void setupNewPassword_givenAccountNotPendingRegistration_thenReturnIsBarRequest() throws Exception {
        String token = JwtUtils.createJwtToken("31", JwtProperties.ONE_DAY_EXPIRATION_TIME);
        jdbi.withHandle(handle -> handle.createUpdate("UPDATE members SET pending_account_registration = false, active = true where id=31;")
                .execute());
        SetupPasswordRequestModel model = new SetupPasswordRequestModel();
        model.setToken(token);
        model.setPassword("NewPassword1");

        String jsonBody = objectMapper.writeValueAsString(model);

        mockMvc.perform(post("http://localhost:8080/register/confirm/pageTwo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void saveMemberDetails_givenValidToken_thenReturnIsOk() throws Exception {
        String token = JwtUtils.createJwtToken("31", JwtProperties.ONE_DAY_EXPIRATION_TIME);
        jdbi.withHandle(handle -> handle.createUpdate("UPDATE members SET pending_account_registration = true, active = false where id=31;")
                .execute());

        MemberDetailsRequestModel model = new MemberDetailsRequestModel();
        model.setArea("PACKING_HALL");
        model.setDepartment("RICHTEA");
        model.setJobRole("ATM");
        model.setShift("DAYS2");
        model.setToken(token);

        String jsonBody = objectMapper.writeValueAsString(model);

        mockMvc.perform(post("http://localhost:8080/register/confirm/pageThree")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk());
    }

    @Test
    public void saveMemberDetails_givenAccountNotPendingRegistration_thenReturnIsBadRequest() throws Exception {
        String token = JwtUtils.createJwtToken("31", JwtProperties.ONE_DAY_EXPIRATION_TIME);
        jdbi.withHandle(handle -> handle.createUpdate("UPDATE members SET pending_account_registration = false, active = true where id=31;")
                .execute());

        MemberDetailsRequestModel model = new MemberDetailsRequestModel();
        model.setArea("PACKING_HALL");
        model.setDepartment("RICHTEA");
        model.setJobRole("ATM");
        model.setShift("DAYS2");
        model.setToken(token);

        String jsonBody = objectMapper.writeValueAsString(model);

        mockMvc.perform(post("http://localhost:8080/register/confirm/pageThree")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isBadRequest());
    }

}
