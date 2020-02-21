package com.alexcatarau.hba.controller;

import com.alexcatarau.hba.model.database.UserDatabaseModel;
import com.alexcatarau.hba.model.response.UserResponseModel;
import com.alexcatarau.hba.security.utils.JwtUtils;
import com.alexcatarau.hba.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.alexcatarau.hba.security.utils.JwtProperties.TOKEN_PREFIX;

@RestController
@RequestMapping("/")
public class HomePageController {

    private UserService userService;

    @Autowired
    public HomePageController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity getUserInfo(@CookieValue("jwt") String tokenWithPrefix) {
        String token = tokenWithPrefix.replace(TOKEN_PREFIX, "");
        String username = JwtUtils.getMemberDetailsFromToken(token);
        UserDatabaseModel user = userService.findByUsername(username).get();

        return ResponseEntity.ok(new UserResponseModel(user.getUsername(), user.getRoleList().get(0)));
    }

}
