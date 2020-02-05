package com.alexcatarau.hba.security;

import com.alexcatarau.hba.model.UserDatabaseModel;
import com.alexcatarau.hba.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class UserPrincipalDetailsService implements UserDetailsService {

    private UserService userService;

    @Autowired
    public UserPrincipalDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserDatabaseModel> userOptional = userService.findByUsername(username);
        if(userOptional.isPresent()){
            return new UserPrincipal(userOptional.get());
        }
        throw new UsernameNotFoundException(String.format("User with username: %s not found", username));
    }
}
