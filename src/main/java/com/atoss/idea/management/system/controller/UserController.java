package com.atoss.idea.management.system.controller;

import com.atoss.idea.management.system.repository.dto.UserRequestDTO;
import com.atoss.idea.management.system.repository.dto.UserResponseDTO;
import com.atoss.idea.management.system.repository.dto.UserUpdateDTO;
import com.atoss.idea.management.system.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
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
    public UserResponseDTO addUser(@RequestParam(name = "username") String username,
                                   @RequestParam(name = "email") String email) {
        return userService.addUser(username, email);
    }

    @Transactional
    @PutMapping("/password")
    public UserRequestDTO updateUserPassword(@RequestParam(name = "username") String username,
                                             @RequestParam(name = "password") String password) {
        return userService.updateUserPassword(username, password);
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
    public Page<UserResponseDTO> getAllUsers(@PageableDefault(sort = "username", direction = Sort.Direction.ASC, size = 2, page = 2)
                                                 Pageable pageable) {
        return userService.getAllUsers(PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "username")));
    }

    @Transactional
    @GetMapping("/allByUsername")
    public Page<UserResponseDTO> getAllUserByUsername(@RequestParam(name = "username") String username) {
        return userService.getAllUsersByUsername(username);
    }
}
