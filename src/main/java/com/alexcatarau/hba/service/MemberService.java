package com.alexcatarau.hba.service;

import com.alexcatarau.hba.model.database.MemberDatabaseModel;
import com.alexcatarau.hba.model.request.MemberCreateRequestModel;
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

    public List<MemberDatabaseModel> getMemberByName(String value) {
        return jdbi.withHandle(handle -> handle.createQuery("select * from members where roles = 'USER' AND (lower(concat(first_name, ' ', last_name)) like :searchValue or lower(concat(last_name, ' ', first_name)) like :searchValue);")
                .bind("searchValue", value.toLowerCase() + "%")
                .mapToBean(MemberDatabaseModel.class)
                .list());
    }

    public List<MemberDatabaseModel> getMemberByEmployeeNumber(Integer employeeNumber) {
        return jdbi.withHandle(handle -> handle.createQuery("select * from members where employee_number = :employeeNumber;")
                .bind("employeeNumber", employeeNumber)
                .mapToBean(MemberDatabaseModel.class)
                .list());
    }

    public Long createMember(MemberCreateRequestModel memberCreateRequestModel) {

        return jdbi.withHandle(handle -> handle.createQuery("insert into members (first_name, last_name, employee_number, username, email, password, roles, active, pending_confirmation) " +
                "values (:firstName, :lastName, :employeeNumber, :username, :email, :password, :roles, :active, :pendingConfirmation) RETURNING id;")
                .bind("firstName", memberCreateRequestModel.getFirstName())
                .bind("lastName", memberCreateRequestModel.getLastName())
                .bind("employeeNumber", memberCreateRequestModel.getEmployeeNumber())
                .bind("username", createUniqueUsername(memberCreateRequestModel.getUsernameFromName()))
                .bind("email", memberCreateRequestModel.getEmail())
                .bind("password", System.getenv("DEFAULT_USER_PASSWORD"))
                .bind("roles", "USER")
                .bind("active", false)
                .bind("pendingConfirmation", true)
                .mapTo(Long.class)
                .one());
    }


    private String createUniqueUsername(String username) {
        boolean usernameExists = isUsernameDuplicate(username);
        if(!usernameExists){
            return username;
        }

        String uniqueUsername = username;
        while(usernameExists){
         uniqueUsername = incrementUsername(uniqueUsername);
         usernameExists = isUsernameDuplicate(uniqueUsername);
        }
        return uniqueUsername;
    }

    private String incrementUsername(String uniqueUsername) {
        if(uniqueUsername.length() == 6){
            return uniqueUsername + 1;
        }
        int count = Integer.parseInt(uniqueUsername.substring(6));
        return uniqueUsername.substring(0,6) + (count+1);
    }


    private boolean isUsernameDuplicate(String username){
        return jdbi.withHandle(handle -> handle.createQuery("select exists(select 1 from members where username = :username);")
                .bind("username", username)
                .mapTo(Boolean.class)
                .one());
    }

    public boolean checkDuplicateEmail(String email) {
        return jdbi.withHandle(handle -> handle.createQuery("select exists(select 1 from members where email = :email);")
                .bind("email", email)
                .mapTo(Boolean.class)
                .one());
    }

    public boolean checkDuplicateEmployeeNumber(String employeeNumber) {
        return jdbi.withHandle(handle -> handle.createQuery("select exists(select 1 from members where employee_number = :employeeNumber);")
                .bind("employeeNumber", employeeNumber)
                .mapTo(Boolean.class)
                .one());
    }
}
