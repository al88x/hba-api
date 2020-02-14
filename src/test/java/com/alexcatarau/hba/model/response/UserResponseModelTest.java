package com.alexcatarau.hba.model.response;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class UserResponseModelTest {

    @Test
    public void userResponseModel(){
        UserResponseModel userResponseModel = new UserResponseModel();
        userResponseModel.setUsername("Alex");
        userResponseModel.setRole("ADMIN");

        assertEquals("Alex", userResponseModel.getUsername());
        assertEquals("ADMIN", userResponseModel.getRole());

    }
}
