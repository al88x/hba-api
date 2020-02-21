package com.alexcatarau.hba.controller;

import com.alexcatarau.hba.model.request.SetupPasswordRequestModel;
import com.alexcatarau.hba.security.utils.JwtUtils;
import com.alexcatarau.hba.service.MemberService;
import com.alexcatarau.hba.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("register")
public class RegistrationController {

    private MemberService memberService;
    private RegistrationService registrationService;

    @Autowired
    public RegistrationController(MemberService memberService, RegistrationService registrationService) {
        this.memberService = memberService;
        this.registrationService = registrationService;
    }

    @GetMapping("/confirm")
    @ResponseBody
    public ResponseEntity getMemberIdFromToken(@RequestParam String token, @RequestParam(required = false) String employeeNumber) {
        if (token != null) {
            Long id = Long.valueOf(JwtUtils.getMemberDetailsFromToken(token));
                System.out.println("id: " + id + ", employeeNumber: " + employeeNumber);
            if (employeeNumber != null) {
                if (memberService.isEmployeeNumberValid(employeeNumber, id)) {
                    return ResponseEntity.ok().build();
                }
            } else {
                if (memberService.isMemberPendingConfirmation(id)) {
                    return ResponseEntity.ok().body(Collections.singletonMap("memberId", Long.valueOf(id)));
                }
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("confirm/pageTwo")
    public ResponseEntity setupNewPassword(@RequestBody SetupPasswordRequestModel model) {
        Long id = Long.valueOf(JwtUtils.getMemberDetailsFromToken(model.getToken()));
        if (memberService.isMemberPendingConfirmation(id)) {
            registrationService.saveNewPassword(id, model.getPassword());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
}
