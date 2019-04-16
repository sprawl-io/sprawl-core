package org.thomaschen.sprawl.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thomaschen.sprawl.model.UserDetail;
import org.thomaschen.sprawl.repository.UserDetailRepository;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    UserDetailRepository userDetailRepository;

    @GetMapping("/login")
    public ResponseEntity<?> login(Principal principal) {
        UserDetail userDetail = userDetailRepository.findByUsername(principal.getName());
        if (userDetail == null) {
            userDetailRepository.save(new UserDetail(principal.getName()));
        }
        return ResponseEntity.ok().build();
    }
}
