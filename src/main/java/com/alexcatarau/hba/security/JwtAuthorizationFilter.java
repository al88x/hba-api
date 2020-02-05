package com.alexcatarau.hba.security;

import com.alexcatarau.hba.model.UserDatabaseModel;
import com.alexcatarau.hba.service.UserService;
import com.auth0.jwt.JWT;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static com.alexcatarau.hba.security.utils.JwtProperties.*;
import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private UserService userService;


    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserService userService) {
        super(authenticationManager);
        this.userService = userService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // Read the Authorization header, where the JWT token should be
        String header = request.getHeader(HEADER_STRING);

        // If header does not contain BEARER or is null delegate to Spring impl and exit
        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        // If header is present, try grab user principal from database and perform authorization
        Authentication authentication = getUsernamePasswordAuthentication(request);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Continue filter execution
        chain.doFilter(request, response);
    }

    private Authentication getUsernamePasswordAuthentication(HttpServletRequest request) {
        String token = request.getHeader(HEADER_STRING)
                .replace(TOKEN_PREFIX,"");

        if (token != null) {
            // parse the token and validate it
            String userName = JWT.require(HMAC512(SECRET.getBytes()))
                    .build()
                    .verify(token)
                    .getSubject();

            // Search in the DB if we find the user by token subject (username)
            // If so, then grab user details and create spring auth token using username, pass, authorities/roles
            if (userName != null) {
                Optional<UserDatabaseModel> user = userService.findByUsername(userName);
                if(user.isPresent()){
                    UserPrincipal userPrincipal = new UserPrincipal(user.get());

                    return new UsernamePasswordAuthenticationToken(userName, null, userPrincipal.getAuthorities());
                }
            }
            return null;
        }
        return null;
    }
}
