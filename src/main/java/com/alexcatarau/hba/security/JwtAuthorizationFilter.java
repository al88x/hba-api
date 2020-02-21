package com.alexcatarau.hba.security;

import com.alexcatarau.hba.model.database.UserDatabaseModel;
import com.alexcatarau.hba.security.utils.JwtUtils;
import com.alexcatarau.hba.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
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
        // Get token from cookie
        String token = null;
        if (request.getCookies() != null)
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("jwt")) {
                    token = cookie.getValue();
                }
            }

        // If token does not contain BEARER or is null delegate to Spring impl and exit
        if (token == null || !token.startsWith(TOKEN_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        // If token is present, try grab user principal from database and perform authorization
        Authentication authentication = getUsernamePasswordAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Continue filter execution
        chain.doFilter(request, response);
    }

    private Authentication getUsernamePasswordAuthentication(String tokenWithPrefix) {
        String token = tokenWithPrefix.replace(TOKEN_PREFIX, "");

        if (!token.isEmpty()) {
            // parse the token and validate it


            String userName = JwtUtils.getMemberDetailsFromToken(token);


            // Search in the DB if we find the user by token subject (username)
            // If so, then grab user details and create spring auth token using username, pass, authorities/roles
            if (userName != null) {
                Optional<UserDatabaseModel> user = userService.findByUsername(userName);
                if (user.isPresent()) {
                    UserPrincipal userPrincipal = new UserPrincipal(user.get());
                    return new UsernamePasswordAuthenticationToken(userName, null, userPrincipal.getAuthorities());
                }
            }
            return null;
        }
        return null;
    }
}

