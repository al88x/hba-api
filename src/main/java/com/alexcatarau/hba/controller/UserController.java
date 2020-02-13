package com.alexcatarau.hba.controller;

import com.alexcatarau.hba.model.database.UserDatabaseModel;
import com.alexcatarau.hba.model.response.UserResponseModel;
import com.alexcatarau.hba.security.utils.JwtProperties;
import com.alexcatarau.hba.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

import static com.alexcatarau.hba.security.utils.JwtProperties.TOKEN_PREFIX;

@RestController
@RequestMapping("user")
public class UserController {

    @GetMapping("/test")
    @ResponseBody
    public ResponseEntity userPage(){
        return ResponseEntity.ok().body(Collections.singletonMap("Success", "Only user role can access this endpoint"));
    }


}
