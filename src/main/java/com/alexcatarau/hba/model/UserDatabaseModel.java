package com.alexcatarau.hba.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserDatabaseModel {

    private Long id;
    private String username;
    private String password;
    private String roles;
    private String permissions;
    private boolean active;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getPermission() {
        return permissions;
    }

    public void setPermission(String permission) {
        this.permissions = permission;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<String> getRoleList(){
        if(roles != null && roles.length() > 0){
            return Arrays.asList(this.roles.split(","));
        }
        return new ArrayList<>();
    }

    public List<String> getPermissionList(){
        if(permissions != null && permissions.length() > 0){
            return Arrays.asList(this.permissions.split(","));
        }
        return new ArrayList<>();
    }
}
