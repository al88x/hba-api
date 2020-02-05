package com.alexcatarau.hba.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("admin")
public class AdminController {

    @GetMapping("/test")
    @ResponseBody
    public ResponseEntity adminPage(){
        return ResponseEntity.ok().body(Collections.singletonMap("Success", "Only admin role can access this endpoint"));
    }
}
