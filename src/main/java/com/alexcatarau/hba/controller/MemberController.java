package com.alexcatarau.hba.controller;

import com.alexcatarau.hba.model.database.MemberDatabaseModel;
import com.alexcatarau.hba.model.request.MemberRequestFilter;
import com.alexcatarau.hba.model.response.MemberListResponseModel;
import com.alexcatarau.hba.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/members")
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
        if (filter.equals("name")) {
            List<MemberDatabaseModel> membersList = memberService.getMemberByName(value);
            if (membersList.size() > 0) {
                return ResponseEntity.ok().body(membersList);
            }
        }
        if (filter.equals("employee-number")) {
            Integer employeeNumber = null;
            try {
                employeeNumber = Integer.valueOf(value);
            }catch (NumberFormatException e){
                return ResponseEntity.badRequest().build();
            }
            Optional<MemberDatabaseModel> member = memberService.getMemberByEmployeeNumber(employeeNumber);
            if (member.isPresent()) {
                return ResponseEntity.ok().body(member);
            }
        }
        return ResponseEntity.notFound().build();
    }
}
