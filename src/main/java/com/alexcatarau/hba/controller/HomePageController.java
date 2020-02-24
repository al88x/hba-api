package com.alexcatarau.hba.controller;

import com.alexcatarau.hba.model.database.UserDatabaseModel;
import com.alexcatarau.hba.model.response.UserResponseModel;
import com.alexcatarau.hba.security.utils.JwtProperties;
import com.alexcatarau.hba.security.utils.JwtUtils;
import com.alexcatarau.hba.service.EmailService;
import com.alexcatarau.hba.service.MemberService;
import com.alexcatarau.hba.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.alexcatarau.hba.security.utils.JwtProperties.TOKEN_PREFIX;

@RestController
@RequestMapping()
public class HomePageController {

    private UserService userService;
    private MemberService memberService;
    private EmailService emailService;

    @Autowired
    public HomePageController(UserService userService, MemberService memberService, EmailService emailService) {
        this.userService = userService;
        this.memberService = memberService;
        this.emailService = emailService;
    }

    @GetMapping("/")
    @ResponseBody
    public ResponseEntity getUserInfo(@CookieValue("jwt") String tokenWithPrefix) {
        String token = tokenWithPrefix.replace(TOKEN_PREFIX, "");
        String username = JwtUtils.getMemberDetailsFromToken(token);
        UserDatabaseModel user = userService.findByUsername(username).get();

        return ResponseEntity.ok(new UserResponseModel(user.getUsername(), user.getRoleList().get(0)));
    }

    @GetMapping("/forgot-password")
    public ResponseEntity resetPassword(@RequestParam String email){
        if(memberService.emailExistsInDatabase(email)){
            String token = JwtUtils.createJwtToken(email, JwtProperties.ONE_DAY_EXPIRATION_TIME);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    emailService.sendResetPasswordEmail(email, token);
                    memberService.lockAccountAndSetAccountToConfirmationPending(email);
                }
            });
            thread.start();
        }
        return ResponseEntity.ok().build();
    }

}
