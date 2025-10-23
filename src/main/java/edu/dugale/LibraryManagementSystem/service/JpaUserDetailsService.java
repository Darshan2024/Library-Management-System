package edu.dugale.LibraryManagementSystem.service;

import java.util.Collections;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import edu.dugale.LibraryManagementSystem.data.UserRepository;
import edu.dugale.LibraryManagementSystem.model.User;

@Service
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public JpaUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = users.findByName(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // No roles/authorities
        return new org.springframework.security.core.userdetails.User(
            u.getName(), u.getPassword(), true, true, true, true, Collections.emptyList()
        );
    }
}
