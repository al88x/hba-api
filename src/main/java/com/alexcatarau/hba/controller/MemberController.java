package com.alexcatarau.hba.controller;

import com.alexcatarau.hba.model.database.MemberDatabaseModel;
import com.alexcatarau.hba.model.request.MemberCreateRequestModel;
import com.alexcatarau.hba.model.request.MemberRequestFilter;
import com.alexcatarau.hba.model.response.MemberListResponseModel;
import com.alexcatarau.hba.security.utils.JwtProperties;
import com.alexcatarau.hba.security.utils.JwtUtils;
import com.alexcatarau.hba.service.EmailService;
import com.alexcatarau.hba.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("admin/members")
public class MemberController {

    private MemberService memberService;
    private EmailService emailService;

    @Autowired
    public MemberController(MemberService memberService, EmailService emailService) {
        this.memberService = memberService;
        this.emailService = emailService;
    }

    @GetMapping
    public ResponseEntity getMembersList(MemberRequestFilter filter) {
        int numberOfMembers = memberService.countMembers();
        List<MemberDatabaseModel> memberList = memberService.getAllMembers(filter);
        return ResponseEntity.ok().body(new MemberListResponseModel(memberList, filter, numberOfMembers));
    }

    @GetMapping("/searchById")
    public ResponseEntity getMemberById(@RequestParam String id) {
        Optional<MemberDatabaseModel> memberById = memberService.getMemberById(Long.parseLong(id));
        if (memberById.isPresent()) {
            return ResponseEntity.ok().body(memberById);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public ResponseEntity getMemberBySearchValue(@RequestParam String value, @RequestParam String filter) {
        List<MemberDatabaseModel> membersList = null;
        if (filter.equals("name")) {
            membersList = memberService.getMemberByName(value);
        }
        if (filter.equals("employee-number")) {
            membersList = memberService.getMemberByEmployeeNumber(value);
        }
        if (membersList != null && membersList.size() > 0) {
            return ResponseEntity.ok().body(membersList);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity createNewMember(@RequestBody @Valid MemberCreateRequestModel memberCreateRequestModel, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        boolean isDuplicateEmployeeNumber = memberService.checkDuplicateEmployeeNumber(memberCreateRequestModel.getEmployeeNumber());
        if (isDuplicateEmployeeNumber) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("employeeNumber", "Employee Number already being used."));
        }

        Optional<Long> existingMemberId = memberService.emailExistsInDatabase(memberCreateRequestModel.getEmail());
        if (existingMemberId.isPresent()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("email", "Email already being used."));
        }

        Long newMemberId = memberService.createMember(memberCreateRequestModel);
        sendRegistrationEmailOnSeparateThread(newMemberId.toString(), memberCreateRequestModel.getFirstName(), memberCreateRequestModel.getEmail());
        return ResponseEntity.ok().body(Collections.singletonMap("userId", newMemberId));
    }

    @PostMapping("/lock-account")
    public ResponseEntity lockAccount(@RequestParam String id){
        memberService.lockAccount(Long.parseLong(id));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/activate-account")
    public ResponseEntity activateAccount(@RequestParam String id){
        memberService.activateAccount(Long.parseLong(id));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-registration-email")
    public ResponseEntity sendRegistrationEmail(@RequestParam String id){
        if(memberService.isMemberPendingAccountRegistration(Long.parseLong(id))){
            MemberDatabaseModel member = memberService.getMemberById(Long.parseLong(id)).get();
            sendRegistrationEmailOnSeparateThread(id, member.getFirstName(), member.getEmail());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    private void sendRegistrationEmailOnSeparateThread(String id, String firstName, String email){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String confirmationToken = JwtUtils.createJwtToken(id, 259200000);//3days
                    emailService.sendEmailNewAccountLink(firstName, email, confirmationToken);
                    memberService.setRegistrationMailSent(true, Long.parseLong(id));
                } catch (Exception e) {
                    e.printStackTrace();
                    memberService.setRegistrationMailSent(false, Long.parseLong(id));
                }
            }
        });
        thread.start();
    }
}
