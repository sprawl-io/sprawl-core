package org.thomaschen.sprawl.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thomaschen.sprawl.exception.ResourceNotFoundException;
import org.thomaschen.sprawl.model.UserDetail;
import org.thomaschen.sprawl.repository.UserDetailRepository;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    UserDetailRepository userDetailRepository;

    // Get all UserDetails
    @GetMapping("/")
    public List<UserDetail> getAllUsers() {
        return userDetailRepository.findAll();
    }

    // Create new UserDetail
    @PostMapping("/")
    public UserDetail createUserDetail(@Valid @RequestBody UserDetail userDetail) {
        return userDetailRepository.save(userDetail);
    }

    // Update UserDetail using UUID
    @PutMapping("/{id}")
    public UserDetail updateUserDetail(@PathVariable(value = "id") UUID id,
                                   @Valid @RequestBody UserDetail updatedDetails) {
        UserDetail userDetail = userDetailRepository.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("UserDetail", "id", id));

        userDetail.setEmail(updatedDetails.getEmail());
        userDetail.setName(updatedDetails.getName());


        UserDetail updatedUserDetail = userDetailRepository.save(userDetail);
        return updatedUserDetail;
    }

    // Get Specifc UserDetail using UUID
    @GetMapping("/{id}")
    public UserDetail getUserDetailById(@PathVariable(value = "id") UUID id) {

        UserDetail userDetail = userDetailRepository.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("UserDetail", "id", id));

        return userDetail;
    }

    // Get Specifc UserDetail using UUID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable(value = "id") UUID id) {

        UserDetail userDetail = userDetailRepository.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("UserDetail", "id", id));

        userDetailRepository.delete(userDetail);

        return ResponseEntity.ok().build();
    }

}
