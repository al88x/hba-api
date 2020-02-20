package com.alexcatarau.hba.model.response;

import com.alexcatarau.hba.model.database.MemberDatabaseModel;
import com.alexcatarau.hba.model.request.MemberRequestFilter;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class MemberListResponseModel extends ListResponseModel<MemberDatabaseModel, MemberRequestFilter> {

    public MemberListResponseModel(List<MemberDatabaseModel> memberList, MemberRequestFilter filter, int numberOfMembers) {
        super(memberList, filter, numberOfMembers);
    }

    @Override
    protected String getRootUrl() {
        return "/members";
    }
}
