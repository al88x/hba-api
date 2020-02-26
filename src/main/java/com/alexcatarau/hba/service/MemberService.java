package com.alexcatarau.hba.service;

import com.alexcatarau.hba.model.database.MemberDatabaseModel;
import com.alexcatarau.hba.model.request.MemberCreateRequestModel;
import com.alexcatarau.hba.model.request.MemberRequestFilter;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    private Jdbi jdbi;

    @Autowired
    public MemberService(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    private final String GET_ALL_MEMBERS_WITH_FILTER = "SELECT id, first_name, last_name, username, active from members " +
            "where roles = 'USER' order by first_name LIMIT :limit OFFSET :offset;";

    public Integer countMembers() {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM members where roles = 'USER';")
                        .mapTo(Integer.class)
                        .one());
    }

    public List<MemberDatabaseModel> getAllMembers(MemberRequestFilter filter) {
        return jdbi.withHandle(handle -> handle.createQuery(GET_ALL_MEMBERS_WITH_FILTER)
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

    public List<MemberDatabaseModel> getMemberByEmployeeNumber(String employeeNumber) {
        return jdbi.withHandle(handle -> handle.createQuery("select * from members where employee_number = :employeeNumber;")
                .bind("employeeNumber", employeeNumber)
                .mapToBean(MemberDatabaseModel.class)
                .list());
    }

    public Optional<MemberDatabaseModel> getMemberById(Long id) {
        return jdbi.withHandle(handle -> handle.createQuery("select * from members where id = :id;")
                .bind("id", id)
                .mapToBean(MemberDatabaseModel.class)
                .findFirst());
    }

    public Long createMember(MemberCreateRequestModel memberCreateRequestModel) {
        return jdbi.withHandle(handle -> handle.createQuery("insert into members (first_name, last_name, employee_number, username, email, password, roles, active, pending_account_registration) " +
                "values (:firstName, :lastName, :employeeNumber, :username, :email, :password, :roles, :active, :pendingAccountRegistration) RETURNING id;")
                .bind("firstName", memberCreateRequestModel.getFirstName())
                .bind("lastName", memberCreateRequestModel.getLastName())
                .bind("employeeNumber", memberCreateRequestModel.getEmployeeNumber())
                .bind("username", createUniqueUsername(memberCreateRequestModel.getUsernameFromName()))
                .bind("email", memberCreateRequestModel.getEmail())
                .bind("password", System.getenv("DEFAULT_USER_PASSWORD"))
                .bind("roles", "USER")
                .bind("active", false)
                .bind("pendingAccountRegistration", true)
                .mapTo(Long.class)
                .one());
    }


    private String createUniqueUsername(String username) {
        boolean usernameExists = isUsernameDuplicate(username);
        if (!usernameExists) {
            return username;
        }

        String uniqueUsername = username;
        while (usernameExists) {
            uniqueUsername = incrementUsername(uniqueUsername);
            usernameExists = isUsernameDuplicate(uniqueUsername);
        }
        return uniqueUsername;
    }

    private String incrementUsername(String uniqueUsername) {
        if (uniqueUsername.length() == 6) {
            return uniqueUsername + 1;
        }
        int count = Integer.parseInt(uniqueUsername.substring(6));
        return uniqueUsername.substring(0, 6) + (count + 1);
    }


    private boolean isUsernameDuplicate(String username) {
        return jdbi.withHandle(handle -> handle.createQuery("select exists(select 1 from members where username = :username);")
                .bind("username", username)
                .mapTo(Boolean.class)
                .one());
    }

    public Optional<Long> emailExistsInDatabase(String email) {
        return jdbi.withHandle(handle -> handle.createQuery("select id from members where email = :email;")
                .bind("email", email)
                .mapTo(Long.class)
                .findOne());
    }

    public boolean checkDuplicateEmployeeNumber(String employeeNumber) {
        return jdbi.withHandle(handle -> handle.createQuery("select exists(select 1 from members where employee_number = :employeeNumber);")
                .bind("employeeNumber", employeeNumber)
                .mapTo(Boolean.class)
                .one());
    }

    public boolean isMemberPendingAccountRegistration(Long id) {
        return jdbi.withHandle(handle -> handle.createQuery("select exists(select 1 from members where (id =:id and pending_account_registration = true));")
                .bind("id", id)
                .mapTo(Boolean.class)
                .one());
    }

    public boolean isEmployeeNumberValid(String employeeNumber, Long id) {
        return jdbi.withHandle(handle -> handle.createQuery("select exists(select 1 from members where (id =:id and employee_number = :employeeNumber));")
                .bind("id", id)
                .bind("employeeNumber", employeeNumber)
                .mapTo(Boolean.class)
                .one());
    }

    public void setRegistrationMailSent(boolean b, Long id) {
        jdbi.withHandle(handle -> handle.createUpdate("UPDATE members SET registration_mail_sent  = :sent where id=:id ;")
                .bind("sent", b)
                .bind("id", id)
                .execute());
    }

    public void lockAccountAndSetAccountToResetPasswordPending(String email) {
        jdbi.withHandle(handle -> handle.createUpdate("UPDATE members SET pending_reset_password = true, active = false where email=:email;")
                .bind("email", email)
                .execute());
    }

    public void unlockAccountAndSetResetPasswordToNotPending(Long id) {
        jdbi.withHandle(handle -> handle.createUpdate("UPDATE members SET pending_reset_password = false, active = true where id=:id;")
                .bind("id", id)
                .execute());
    }

    public boolean isMemberPendingResetPassword(Long id) {
        return jdbi.withHandle(handle -> handle.createQuery("select exists(select 1 from members where (id =:id and pending_reset_password = true));")
                .bind("id", id)
                .mapTo(Boolean.class)
                .one());
    }

    public void lockAccount(Long id) {
        jdbi.withHandle(handle -> handle.createUpdate("UPDATE members SET active = false where id=:id;")
                .bind("id", id)
                .execute());
    }

    public void activateAccount(long id) {
        jdbi.withHandle(handle -> handle.createUpdate("UPDATE members SET active = true where id=:id;")
                .bind("id", id)
                .execute());
    }
}
