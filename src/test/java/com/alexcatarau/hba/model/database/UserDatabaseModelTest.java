package com.alexcatarau.hba.model.database;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UserDatabaseModelTest {

    @Test
    public void testUserModel(){
        UserDatabaseModel user = new UserDatabaseModel();
        user.setId(1L);
        user.setActive(true);
        user.setPassword("Password");
        user.setPermission("USER,ADMIN");
        user.setRoles("USER,ADMIN");
        user.setUsername("alex");

        assertEquals("Password", user.getPassword());
        assertEquals(1L, (long) user.getId());
        assertEquals(true, user.isActive());
        assertEquals("USER,ADMIN", user.getPermission());
        assertEquals("USER,ADMIN", user.getRoles());
        assertEquals("alex", user.getUsername());
        assertEquals(2, user.getPermissionList().size());
        assertEquals(2, user.getRoleList().size());

        user.setPermission("");
        user.setRoles("");

        assertEquals(0, user.getPermissionList().size());
        assertEquals(0, user.getRoleList().size());

        user.setPermission(null);
        user.setRoles(null);

        assertEquals(0, user.getPermissionList().size());
        assertEquals(0, user.getRoleList().size());

    }
}
