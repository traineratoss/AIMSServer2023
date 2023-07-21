package com.atoss.idea.management.system.controller;

import com.atoss.idea.management.system.repository.dto.ChangePasswordDTO;
import com.atoss.idea.management.system.repository.dto.UserResponseDTO;
import com.atoss.idea.management.system.repository.dto.UserUpdateDTO;
import com.atoss.idea.management.system.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Transactional
    @PostMapping
    public ResponseEntity<UserResponseDTO> addUser(@RequestParam(name = "username") String username,
                                                   @RequestParam(name = "email") String email) {
        return new ResponseEntity<>(userService.addUser(username, email), HttpStatus.CREATED);
    }

    @Transactional
    @PatchMapping
    public UserResponseDTO updateUserByUsername(@RequestParam(value = "username") String username, @RequestBody UserUpdateDTO userUpdateDTO) {
        return userService.updateUserByUsername(username, userUpdateDTO);
    }

    @Transactional
    @GetMapping
    public UserResponseDTO getUserByUsername(@RequestParam(name = "username") String username) {
        return userService.getUserByUsername(username);
    }

    @Transactional
    @GetMapping("/all")
    public  ResponseEntity<Page<UserResponseDTO>> getAllUsers(@RequestParam(required = true) int pageSize,
                                                              @RequestParam(required = true) int pageNumber,
                                                              @RequestParam(required = true) String sortCategory) {
        return new ResponseEntity<>(
                userService.getAllUsers(
                        PageRequest.of(
                                pageNumber,
                                pageSize,
                                Sort.by(Sort.Direction.ASC, sortCategory)
                        )
                ),
                HttpStatus.OK
        );
    }

    @Transactional
    @GetMapping("/allByUsername")
    public Page<UserResponseDTO> getAllUserByUsername(@RequestParam(name = "username") String username) {
        return userService.getAllUsersByUsername(username);
    }

    @Transactional
    @GetMapping("/allByIsActive")
    public ResponseEntity<Page<UserResponseDTO>> getAllUserByIsActive(@RequestParam(name = "isActive") boolean isActive,
                                                                      @RequestParam(required = true) int pageSize,
                                                                      @RequestParam(required = true) int pageNumber,
                                                                      @RequestParam(required = true) String sortCategory) {
        return new ResponseEntity<>(
                userService.getAllPendingUsers(
                        isActive,
                        PageRequest.of(
                            pageNumber,
                            pageSize,
                            Sort.by(Sort.Direction.ASC, sortCategory)
                        )
                ),
                HttpStatus.OK
        );
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
        boolean passwordChanged = userService.changePassword(changePasswordDTO);

        if (passwordChanged) {
            return ResponseEntity.ok("Parola a fost actualizată cu succes!");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Parola veche este incorectă");
        }
    }

    @PostMapping("/send-email")
    public void sendEmail(@RequestBody String username) {
        userService.sendEmail(username);
    }
}
