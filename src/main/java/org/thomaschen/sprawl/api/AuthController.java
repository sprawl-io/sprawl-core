package org.thomaschen.sprawl.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thomaschen.sprawl.model.User;
import org.thomaschen.sprawl.repository.UserRepository;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    UserRepository userRepository;

    @GetMapping("/login")
    public ResponseEntity<?> login(Principal principal) {
        User user = userRepository.findByUsername(principal.getName());
        if (user == null) {
            userRepository.save(new User(principal.getName()));
        }
        return ResponseEntity.ok().build();
    }
}
