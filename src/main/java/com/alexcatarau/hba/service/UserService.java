package com.alexcatarau.hba.service;

import com.alexcatarau.hba.model.database.UserDatabaseModel;
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
        return jdbi.withHandle(handle -> handle.createQuery("SELECT * FROM members WHERE username = :username;")
                .bind("username", username)
                .mapToBean(UserDatabaseModel.class)
                .findFirst());
    }

    public Optional<UserDatabaseModel> getUsernameOfActiveUser(String username) {
        return jdbi.withHandle(handle -> handle.createQuery("SELECT * FROM members WHERE username = :username and active = true;")
                .bind("username", username)
                .mapToBean(UserDatabaseModel.class)
                .findFirst());
    }
}
