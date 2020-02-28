package com.alexcatarau.hba.controller;

import com.alexcatarau.hba.config.IntegrationTestConfig;
import com.alexcatarau.hba.model.request.LoginRequestModel;
import com.alexcatarau.hba.model.request.MemberCreateRequestModel;
import com.alexcatarau.hba.model.request.MemberUpdateRequestModel;
import com.alexcatarau.hba.service.EmailService;
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

import javax.servlet.http.Cookie;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
@ContextConfiguration(classes = IntegrationTestConfig.class)
public class MemberControllerTest {

    @MockBean
    private EmailService emailService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Jdbi jdbi;

    private Cookie cookie;

    @Before
    public void createTestUser() throws Exception {
        jdbi.withHandle(handle -> handle.createUpdate("INSERT INTO members(id, first_name, last_name, username, email, employee_number,password, roles, permissions, active)\n" +
                "values (30, 'alex', 'alexandru', 'aleale', 'alex92@mail.com', 10011092,'$2y$10$mbmAkdm6hi7LyVBaGRBwTOgu9I.rTxo80ZUcI/GSTimZN7unr0MbC', 'ADMIN', 'ADMIN', true);")
                .execute());
        jdbi.withHandle(handle -> handle.createUpdate("INSERT INTO members(id, first_name, last_name, email, username, employee_number,password, roles, permissions, active)\n" +
                "values (31, 'john', 'doe' ,'john99@mail.com','johdoe', 10011093,'$2y$10$mbmAkdm6hi7LyVBaGRBwTOgu9I.rTxo80ZUcI/GSTimZN7unr0MbC', 'USER', 'USER', true);")
                .execute());
        String jsonBody = objectMapper.writeValueAsString(new LoginRequestModel("aleale", "Password123"));
        MvcResult mvcResult = mockMvc.perform(post("http://localhost:8080/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk())
                .andReturn();

        cookie = mvcResult.getResponse().getCookie("jwt");
    }

    @After
    public void deleteTestUser() {
        jdbi.withHandle(handle -> handle.createUpdate("DELETE FROM members where username = 'aleale';")
                .execute());
        jdbi.withHandle(handle -> handle.createUpdate("DELETE FROM members where username = 'johdoe';")
                .execute());
    }

    @Test
    public void getMembersList_givenFilterInUrl_thenReturnMemberList() throws Exception {
        mockMvc.perform(get("http://localhost:8080/admin/members?page=1&pageSize=10")
                .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.[0].username").value("johdoe"))
                .andExpect(jsonPath("$.totalNumberOfItems").value(1));

    }

    @Test
    public void getMemberById_givenId_thenReturnMember() throws Exception {
        mockMvc.perform(get("http://localhost:8080/admin/members/searchById?id=30")
                .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("aleale"));
    }

    @Test
    public void getMemberBySearchValue_givenSearchParam_andNameFilter_thenReturnMember() throws Exception {
        mockMvc.perform(get("http://localhost:8080/admin/members/search?value=john&filter=name")
                .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].username").value("johdoe"));
    }

        @Test
        public void getMemberBySearchValue_givenSearchParam_andEmployeeNumberFilter_thenReturnMember() throws Exception {
                mockMvc.perform(get("http://localhost:8080/admin/members/search?value=10011093&filter=employee-number")
                        .cookie(cookie))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("[0].username").value("johdoe"));
        }

        @Test
        public void createNewMember_givenModelWithNullFirstName_returnBadRequest() throws Exception {
                MemberCreateRequestModel memberCreateRequestModel = new MemberCreateRequestModel();
                memberCreateRequestModel.setFirstName(null);
                memberCreateRequestModel.setLastName("Doe");
                memberCreateRequestModel.setEmployeeNumber("10011099");
                memberCreateRequestModel.setEmail("john99@mail.com");

                String jsonBody = objectMapper.writeValueAsString(memberCreateRequestModel);


                mockMvc.perform(post("http://localhost:8080/admin/members/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(cookie)
                        .content(jsonBody))
                        .andExpect(status().isBadRequest());
        }

        @Test
        public void createNewMember_givenModelWithDuplicateEmployeeNumber_returnBadRequest() throws Exception {
                MemberCreateRequestModel memberCreateRequestModel = new MemberCreateRequestModel();
                memberCreateRequestModel.setFirstName("John");
                memberCreateRequestModel.setLastName("Doe");
                memberCreateRequestModel.setEmployeeNumber("10011093");
                memberCreateRequestModel.setEmail("john929@mail.com");

                String jsonBody = objectMapper.writeValueAsString(memberCreateRequestModel);


                mockMvc.perform(post("http://localhost:8080/admin/members/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(cookie)
                        .content(jsonBody))
                        .andExpect(status().isBadRequest());
        }

        @Test
        public void createNewMember_givenModelWithDuplicateEmail_returnBadRequest() throws Exception {
                MemberCreateRequestModel memberCreateRequestModel = new MemberCreateRequestModel();
                memberCreateRequestModel.setFirstName("John");
                memberCreateRequestModel.setLastName("Doe");
                memberCreateRequestModel.setEmployeeNumber("10011095");
                memberCreateRequestModel.setEmail("john99@mail.com");

                String jsonBody = objectMapper.writeValueAsString(memberCreateRequestModel);


                mockMvc.perform(post("http://localhost:8080/admin/members/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(cookie)
                        .content(jsonBody))
                        .andExpect(status().isBadRequest());
        }


    @Test
    public void createNewMember_givenValidModel_returnStatusOk() throws Exception {
        MemberCreateRequestModel memberCreateRequestModel = new MemberCreateRequestModel();
        memberCreateRequestModel.setFirstName("John");
        memberCreateRequestModel.setLastName("Doe");
        memberCreateRequestModel.setEmployeeNumber("10011095");
        memberCreateRequestModel.setEmail("john939@mail.com");
        String jsonBody = objectMapper.writeValueAsString(memberCreateRequestModel);

        doNothing().when(emailService).sendEmailNewAccountLink(any(),any(), any());

        mockMvc.perform(post("http://localhost:8080/admin/members/create")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie)
                .content(jsonBody))
                .andExpect(status().isOk());
    }

    @Test
    public void updateMember_givenUpdateWithDuplicateEmployeeNumber_theReturnBadRequest() throws Exception {
        MemberUpdateRequestModel memberUpdateRequestModel = new MemberUpdateRequestModel();
        memberUpdateRequestModel.setId("31");
        memberUpdateRequestModel.setFirstName("John");
        memberUpdateRequestModel.setLastName("Doe");
        memberUpdateRequestModel.setEmployeeNumber("10011092");
        memberUpdateRequestModel.setEmail("john99@mail.com");
        memberUpdateRequestModel.setUsername("johdoe");
        memberUpdateRequestModel.setArea("PACKING_HALL");
        memberUpdateRequestModel.setDepartment("RICHTEA");
        memberUpdateRequestModel.setJobRole("ATM");
        memberUpdateRequestModel.setShift("DAYS2");

        String jsonBody = objectMapper.writeValueAsString(memberUpdateRequestModel);

        mockMvc.perform(post("http://localhost:8080/admin/members/update")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie)
                .content(jsonBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateMember_givenUpdateWithDuplicateEmail_theReturnBadRequest() throws Exception {
        MemberUpdateRequestModel memberUpdateRequestModel = new MemberUpdateRequestModel();
        memberUpdateRequestModel.setId("31");
        memberUpdateRequestModel.setFirstName("John");
        memberUpdateRequestModel.setLastName("Doe");
        memberUpdateRequestModel.setEmployeeNumber("10011093");
        memberUpdateRequestModel.setEmail("alex92@mail.com");
        memberUpdateRequestModel.setUsername("johdoe");
        memberUpdateRequestModel.setArea("PACKING_HALL");
        memberUpdateRequestModel.setDepartment("RICHTEA");
        memberUpdateRequestModel.setJobRole("ATM");
        memberUpdateRequestModel.setShift("DAYS2");

        String jsonBody = objectMapper.writeValueAsString(memberUpdateRequestModel);

        mockMvc.perform(post("http://localhost:8080/admin/members/update")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie)
                .content(jsonBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateMember_givenUpdateWithDuplicateUsername_theReturnBadRequest() throws Exception {
        MemberUpdateRequestModel memberUpdateRequestModel = new MemberUpdateRequestModel();
        memberUpdateRequestModel.setId("31");
        memberUpdateRequestModel.setFirstName("John");
        memberUpdateRequestModel.setLastName("Doe");
        memberUpdateRequestModel.setEmployeeNumber("10011093");
        memberUpdateRequestModel.setEmail("john99@mail.com");
        memberUpdateRequestModel.setUsername("aleale");
        memberUpdateRequestModel.setArea("PACKING_HALL");
        memberUpdateRequestModel.setDepartment("RICHTEA");
        memberUpdateRequestModel.setJobRole("ATM");
        memberUpdateRequestModel.setShift("DAYS2");


        String jsonBody = objectMapper.writeValueAsString(memberUpdateRequestModel);

        mockMvc.perform(post("http://localhost:8080/admin/members/update")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie)
                .content(jsonBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateMember_givenValidUpdateModel_theReturnIsOk() throws Exception {
        MemberUpdateRequestModel memberUpdateRequestModel = new MemberUpdateRequestModel();
        memberUpdateRequestModel.setId("31");
        memberUpdateRequestModel.setFirstName("John-edited");
        memberUpdateRequestModel.setLastName("Doe");
        memberUpdateRequestModel.setEmployeeNumber("10011093");
        memberUpdateRequestModel.setEmail("john99@mail.com");
        memberUpdateRequestModel.setUsername("johdoe");
        memberUpdateRequestModel.setArea("PACKING_HALL");
        memberUpdateRequestModel.setDepartment("RICHTEA");
        memberUpdateRequestModel.setJobRole("ATM");
        memberUpdateRequestModel.setShift("DAYS2");


       String firstNameBeforeUpdate =  jdbi.withHandle(handle -> handle.createQuery("select first_name from members where id =31")
                .mapTo(String.class)
                .first());

        String jsonBody = objectMapper.writeValueAsString(memberUpdateRequestModel);

        mockMvc.perform(post("http://localhost:8080/admin/members/update")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie)
                .content(jsonBody))
                .andExpect(status().isOk());

        String firstNameAfterUpdate =  jdbi.withHandle(handle -> handle.createQuery("select first_name from members where id =31")
                .mapTo(String.class)
                .first());

        assertEquals("john", firstNameBeforeUpdate);
        assertEquals("John-edited", firstNameAfterUpdate);

    }

    @Test
    public void lockAccount_givenId_thenReturnIsOk() throws Exception {
        boolean isActiveBeforeLockAccount = jdbi.withHandle(handle -> handle.createQuery("select active from members where id =31")
                .mapTo(Boolean.class)
                .first());

        mockMvc.perform(post("http://localhost:8080/admin/members/lock-account?id=31")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie))
                .andExpect(status().isOk());

        boolean isActiveAfterLockAccount = jdbi.withHandle(handle -> handle.createQuery("select active from members where id =31")
                .mapTo(Boolean.class)
                .first());


        mockMvc.perform(post("http://localhost:8080/admin/members/activate-account?id=31")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie))
                .andExpect(status().isOk());

        boolean isActiveAfterActivateAccount = jdbi.withHandle(handle -> handle.createQuery("select active from members where id =31")
                .mapTo(Boolean.class)
                .first());

        assertTrue(isActiveBeforeLockAccount);
        assertFalse(isActiveAfterLockAccount);
        assertTrue(isActiveAfterActivateAccount);

    }

    @Test
    public void sendRegistrationEmail_givenId_thenResponseIsOk() throws Exception {
        jdbi.withHandle(handle -> handle.createUpdate("UPDATE members SET pending_account_registration = true, active = false where id=31;")
                .execute());

        doNothing().when(emailService).sendEmailNewAccountLink(any(),any(), any());

        mockMvc.perform(post("http://localhost:8080/admin/members/send-registration-email?id=31")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie))
                .andExpect(status().isOk());
    }

    @Test
    public void sendRegistrationEmail_givenIdForMemberNotPendingRegistration_thenResponseIsBadRequest() throws Exception {
        jdbi.withHandle(handle -> handle.createUpdate("UPDATE members SET pending_account_registration = false, active = true where id=31;")
                .execute());

        doNothing().when(emailService).sendEmailNewAccountLink(any(),any(), any());

        mockMvc.perform(post("http://localhost:8080/admin/members/send-registration-email?id=31")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie))
                .andExpect(status().isBadRequest());
    }


}
