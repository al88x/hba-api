package com.alexcatarau.hba.service;

import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private PasswordEncoder passwordEncoder;
    private Jdbi jdbi;

    @Autowired
    public RegistrationService(PasswordEncoder passwordEncoder, Jdbi jdbi) {
        this.passwordEncoder = passwordEncoder;
        this.jdbi = jdbi;
    }


    public void saveNewPassword(Long id, String password) {
        jdbi.withHandle(handle -> handle.createUpdate("UPDATE members SET password = :password where id=:id;")
                .bind("password", passwordEncoder.encode(password))
                .bind("id", id)
                .execute());
    }
}