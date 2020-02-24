package com.alexcatarau.hba.security.utils;

public class JwtProperties {
    public static final String SECRET = "SomeSecretForJWTGeneration";
    public static final int ONE_DAY_EXPIRATION_TIME = 86_400_000; // 1 day in milliseconds
    public static final String TOKEN_PREFIX = "Bearer ";

}
