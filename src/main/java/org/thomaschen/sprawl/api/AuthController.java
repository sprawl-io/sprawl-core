package org.thomaschen.sprawl.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thomaschen.sprawl.exception.ResourceNotFoundException;
import org.thomaschen.sprawl.model.UserDetail;
import org.thomaschen.sprawl.repository.UserDetailRepository;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

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
        System.out.println("SUCCESSFUL LOGIN");
        return ResponseEntity.ok().build();
    }
}
