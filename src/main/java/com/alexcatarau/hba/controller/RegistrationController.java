package com.alexcatarau.hba.controller;

import com.alexcatarau.hba.model.request.MemberDetailsRequestModel;
import com.alexcatarau.hba.model.request.SetupPasswordRequestModel;
import com.alexcatarau.hba.security.utils.JwtProperties;
import com.alexcatarau.hba.security.utils.JwtUtils;
import com.alexcatarau.hba.service.MemberService;
import com.alexcatarau.hba.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
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
            if (employeeNumber != null) {
                if (memberService.isEmployeeNumberValid(employeeNumber, id)) {
                    return ResponseEntity.ok().build();
                }
            } else {
                if (memberService.isMemberPendingAccountRegistration(id)) {
                    return ResponseEntity.ok().body(Collections.singletonMap("memberId", Long.valueOf(id)));
                }
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("confirm/pageTwo")
    public ResponseEntity setupNewPassword(@RequestBody SetupPasswordRequestModel model) {
        Long id = Long.valueOf(JwtUtils.getMemberDetailsFromToken(model.getToken()));
        if (memberService.isMemberPendingAccountRegistration(id)) {
            registrationService.saveNewPassword(id, model.getPassword());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/confirm/pageThree")
    public ResponseEntity saveMemberDetails(@RequestBody MemberDetailsRequestModel model, HttpServletResponse response) {
        Long id = Long.valueOf(JwtUtils.getMemberDetailsFromToken(model.getToken()));
        if (memberService.isMemberPendingAccountRegistration(id)) {
            String username = registrationService.saveMemberDetails(id, model);
            String jwtToken = JwtUtils.createJwtToken(username, JwtProperties.ONE_DAY_EXPIRATION_TIME);
            Cookie cookie = JwtUtils.createCookieWithToken(jwtToken);
            response.addCookie(cookie);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
}
