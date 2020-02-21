package com.alexcatarau.hba.security.utils;

import com.auth0.jwt.JWT;

import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

public class JwtProperties {
    public static final String SECRET = "SomeSecretForJWTGeneration";
    public static final int EXPIRATION_TIME = 86_400_000; // 1 day in milliseconds
    public static final String TOKEN_PREFIX = "Bearer ";

//    public static String getUsernameFromToken(String token){
//        return JWT.require(HMAC512(SECRET.getBytes()))
//                .build()
//                .verify(token)
//                .getSubject();
//    }
//
//    public static String createJwtToken(String username, Integer expirationTimeInMilliseconds){
//       return JWT.create()
//                .withSubject(username)
//                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTimeInMilliseconds))
//                .sign(HMAC512(JwtProperties.SECRET.getBytes()));
//    }
}
