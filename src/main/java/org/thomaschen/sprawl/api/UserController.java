package org.thomaschen.sprawl.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.bind.annotation.*;
import org.thomaschen.sprawl.exception.ResourceNotFoundException;
import org.thomaschen.sprawl.model.User;
import org.thomaschen.sprawl.repository.UserRepository;
import org.thomaschen.sprawl.security.Role;
import org.thomaschen.sprawl.security.SprawlDataWebSecurityConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;


import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    UserRepository userRepository;

    private final InMemoryUserDetailsManager inMemoryUserDetailsManager;

    @Autowired
    public UserController(InMemoryUserDetailsManager inMemoryUserDetailsManager) {
        this.inMemoryUserDetailsManager = inMemoryUserDetailsManager;
    }

    // Get all Users
    @GetMapping("/")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Create new User
    @PostMapping("/register")
    public User createUserDetail(@Valid @RequestBody User user) {

        inMemoryUserDetailsManager.createUser(org.springframework.security.core.userdetails.User.withUsername(user.getUsername()).password(new BCryptPasswordEncoder().encode(user.getPassword())).roles("USER").build());
        return userRepository.save(user);
    }

    // Update User using UUID
    @PutMapping("/{id}")
    public User updateUserDetail(@PathVariable(value = "id") UUID id,
                                 @Valid @RequestBody User updatedDetails) {
        User user = userRepository.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("User", "id", id));

        user.setEmail(updatedDetails.getEmail());
        user.setName(updatedDetails.getName());
        user.setPassword(updatedDetails.getPassword());

        User updatedUser = userRepository.save(user);
        return updatedUser;
    }

    // Get Specifc User using UUID
    @GetMapping("/{id}")
    public User getUserDetailById(@PathVariable(value = "id") UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("User", "id", id));

        return user;
    }

    // Get Specifc User using UUID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable(value = "id") UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("User", "id", id));

        userRepository.delete(user);

        return ResponseEntity.ok().build();
    }

}
