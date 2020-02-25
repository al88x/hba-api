package com.alexcatarau.hba.controller;

import com.alexcatarau.hba.model.database.MemberDatabaseModel;
import com.alexcatarau.hba.model.request.MemberCreateRequestModel;
import com.alexcatarau.hba.model.request.MemberRequestFilter;
import com.alexcatarau.hba.model.response.MemberListResponseModel;
import com.alexcatarau.hba.security.utils.JwtUtils;
import com.alexcatarau.hba.service.EmailService;
import com.alexcatarau.hba.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
        System.out.println(newMemberId.toString());
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String confirmationToken = JwtUtils.createJwtToken(newMemberId.toString(), 259200000);//3days
                    emailService.sendEmailNewAccountLink(memberCreateRequestModel.getFirstName(), memberCreateRequestModel.getEmail(), confirmationToken);
                    memberService.setConfirmationMailSent(true, newMemberId);
                } catch (Exception e) {
                    e.printStackTrace();
                    memberService.setConfirmationMailSent(false, newMemberId);
                }
            }
        });
        thread.start();
        return ResponseEntity.ok().body(Collections.singletonMap("userId", newMemberId));
    }
}
