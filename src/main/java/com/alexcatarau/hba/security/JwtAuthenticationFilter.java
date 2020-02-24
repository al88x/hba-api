package com.alexcatarau.hba.security;

import com.alexcatarau.hba.model.request.LoginRequestModel;
import com.alexcatarau.hba.security.utils.JwtProperties;
import com.alexcatarau.hba.security.utils.JwtUtils;
import com.auth0.jwt.JWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;


public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    // Triggered when we issue POST request to /login
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        // Grab credentials and map them to loginDatabaseModel
        LoginRequestModel credentials = null;

        try {
            credentials = new ObjectMapper().readValue(request.getInputStream(), LoginRequestModel.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }


        // Create login token
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                credentials.getUsername(),
                credentials.getPassword(),
                new ArrayList<>());

        // Authenticate user
        Authentication auth = authenticationManager.authenticate(authenticationToken);

        return auth;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult){
        UserPrincipal principal = (UserPrincipal) authResult.getPrincipal();
        String token = JwtUtils.createJwtToken(principal.getUsername(), JwtProperties.EXPIRATION_TIME);
        Cookie cookie = JwtUtils.createCookieWithToken(token);

        response.addCookie(cookie);
    }
}
