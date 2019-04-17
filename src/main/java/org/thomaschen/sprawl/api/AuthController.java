package org.thomaschen.sprawl.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.thomaschen.sprawl.exception.ResourceNotFoundException;
import org.thomaschen.sprawl.model.User;
import org.thomaschen.sprawl.repository.UserRepository;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    UserRepository userRepository;

    @GetMapping("/login")
    public ResponseEntity<?> login() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails)principal).getUsername();
        } else {
            username =  principal.toString();
        }

        User target = userRepository.findByUsername(username)
                .orElseThrow( () -> new ResourceNotFoundException("User", "username", username));

        if (target == null) {
            userRepository.save(new User(username));
        }
        return ResponseEntity.ok().build();
    }
}
