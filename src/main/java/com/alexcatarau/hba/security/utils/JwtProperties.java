package com.alexcatarau.hba.security.utils;

public class JwtProperties {
    public static final String SECRET = "SomeSecretForJWTGeneration";
    public static final int EXPIRATION_TIME = 900_000; // 15min
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
}
