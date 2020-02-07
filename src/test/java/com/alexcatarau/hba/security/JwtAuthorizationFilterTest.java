package com.alexcatarau.hba.security;

import com.alexcatarau.hba.service.UserService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public class JwtAuthorizationFilterTest {


    private UserService userService;



    public void getUsernamePasswordAuthentication_givenEmptyToken_returnNull(){

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", null);


    }
}
