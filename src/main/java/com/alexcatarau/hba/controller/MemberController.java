package com.alexcatarau.hba.controller;

import com.alexcatarau.hba.model.database.MemberDatabaseModel;
import com.alexcatarau.hba.model.request.MemberCreateRequestModel;
import com.alexcatarau.hba.model.request.MemberRequestFilter;
import com.alexcatarau.hba.model.response.MemberListResponseModel;
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

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public ResponseEntity getMembersList(MemberRequestFilter filter) {
        int numberOfMembers = memberService.countMembers();
        List<MemberDatabaseModel> memberList = memberService.getAllMembers(filter);
        return ResponseEntity.ok().body(new MemberListResponseModel(memberList, filter, numberOfMembers));
    }

    @GetMapping("/search")
    public ResponseEntity getMemberBySearchValue(@RequestParam String value, @RequestParam String filter) {
        List<MemberDatabaseModel> membersList = null;
        if (filter.equals("name")) {
            membersList = memberService.getMemberByName(value);
        }
        if (filter.equals("employee-number")) {
            Integer employeeNumber = null;
            try {
                employeeNumber = Integer.valueOf(value);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().build();
            }
            membersList = memberService.getMemberByEmployeeNumber(employeeNumber);
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

        boolean isDuplicateEmail = memberService.checkDuplicateEmail(memberCreateRequestModel.getEmail());
        if (isDuplicateEmail) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("email", "Email already being used."));
        }

        Long id = memberService.createMember(memberCreateRequestModel);
        return ResponseEntity.ok().body(Collections.singletonMap("userId", id));
    }
}
