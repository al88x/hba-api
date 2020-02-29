package com.alexcatarau.hba.service;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MemberServiceTest {

    @Test
    public void incrementUsername(){
        MemberService memberService = new MemberService(null);
        assertEquals("alecat1", memberService.incrementUsername("alecat"));
        assertEquals("alecat2", memberService.incrementUsername("alecat1"));

    }
}
