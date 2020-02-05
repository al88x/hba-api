package com.alexcatarau.hba.service;

import com.alexcatarau.hba.model.UserDatabaseModel;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private Jdbi jdbi;

    @Autowired
    public UserService(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public Optional<UserDatabaseModel> findByUsername(String username) {
        return jdbi.withHandle(handle -> handle.createQuery("SELECT * FROM users WHERE username = :username;")
                .bind("username", username)
                .mapToBean(UserDatabaseModel.class)
                .findFirst());
    }
}
