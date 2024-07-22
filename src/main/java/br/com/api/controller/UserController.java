package br.com.api.controller;

import br.com.api.dto.UserRequest;
import br.com.api.dto.JwtResponse;
import br.com.api.dto.UserResponse;
import br.com.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("user")
public class UserController {

    private final UserService userService;

    @PostMapping(path = "token")
    public JwtResponse authenticate(Authentication authentication) {

        return userService.authenticate(authentication);
    }

    @PostMapping(path = "create")
    public UserResponse createUser(@Valid @RequestBody UserRequest userRequest) {

        return userService.createUser(userRequest);
    }

    @GetMapping(path = "find")
    public UserResponse findUserByUsername(@RequestParam String username) {

        return userService.findUserByUsername(username);
    }
}