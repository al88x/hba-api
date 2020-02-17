package com.alexcatarau.hba.service;

import com.alexcatarau.hba.model.database.MemberDatabaseModel;
import com.alexcatarau.hba.model.request.MemberRequestFilter;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    private Jdbi jdbi;

    @Autowired
    public MemberService(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    private final String GET_ALL_SUSPECTS_WITH_FILTER = "SELECT id, first_name, middle_name, last_name, username from members " +
            "where roles = 'USER' order by first_name LIMIT :limit OFFSET :offset;";

    public Integer countMembers() {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM members where roles = 'USER';")
                        .mapTo(Integer.class)
                        .one());
    }

    public List<MemberDatabaseModel> getAllMembers(MemberRequestFilter filter) {
        return jdbi.withHandle(handle -> handle.createQuery(GET_ALL_SUSPECTS_WITH_FILTER)
        .bind("limit", filter.getPageSize())
        .bind("offset", filter.getOffset())
        .mapToBean(MemberDatabaseModel.class)
        .list());
    }
}
