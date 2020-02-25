package com.alexcatarau.hba.controller;

import com.alexcatarau.hba.model.database.MemberDatabaseModel;
import com.alexcatarau.hba.model.database.UserDatabaseModel;
import com.alexcatarau.hba.model.request.SetupPasswordRequestModel;
import com.alexcatarau.hba.model.response.UserResponseModel;
import com.alexcatarau.hba.security.utils.JwtProperties;
import com.alexcatarau.hba.security.utils.JwtUtils;
import com.alexcatarau.hba.service.EmailService;
import com.alexcatarau.hba.service.MemberService;
import com.alexcatarau.hba.service.RegistrationService;
import com.alexcatarau.hba.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static com.alexcatarau.hba.security.utils.JwtProperties.TOKEN_PREFIX;

@RestController
@RequestMapping()
public class HomePageController {

    private UserService userService;
    private MemberService memberService;
    private EmailService emailService;
    private RegistrationService registrationService;

    @Autowired
    public HomePageController(UserService userService, MemberService memberService, EmailService emailService, RegistrationService registrationService) {
        this.userService = userService;
        this.memberService = memberService;
        this.emailService = emailService;
        this.registrationService = registrationService;
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
    public ResponseEntity resetPassword(@RequestParam String email) {
        Optional<Long> id = memberService.emailExistsInDatabase(email);
        if (id.isPresent()) {
            String token = JwtUtils.createJwtToken(id.get().toString(), JwtProperties.ONE_DAY_EXPIRATION_TIME);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    emailService.sendResetPasswordEmail(email, token);
                    memberService.lockAccountAndSetAccountToResetPasswordPending(email);
                }
            });
            thread.start();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/resetPassword")
    public ResponseEntity setupNewPassword(@RequestBody SetupPasswordRequestModel model, HttpServletResponse response) {
        Long id = Long.valueOf(JwtUtils.getMemberDetailsFromToken(model.getToken()));
        if (memberService.isMemberPendingResetPassword(id)) {
            registrationService.saveNewPassword(id, model.getPassword());
            memberService.unlockAccountAndSetResetPasswordToNotPending(id);
            MemberDatabaseModel memberById = memberService.getMemberById(id).get();

            String loginToken = JwtUtils.createJwtToken(memberById.getUsername(), JwtProperties.ONE_DAY_EXPIRATION_TIME);
            Cookie cookie = JwtUtils.createCookieWithToken(loginToken);
            response.addCookie(cookie);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

}
