package br.com.api.controller;

import br.com.api.dto.JwtResponse;
import br.com.api.dto.UserDto;
import br.com.api.entities.User;
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
    public User createUser(@Valid @RequestBody UserDto userDto) {

        return userService.createUser(userDto);
    }

    @GetMapping(path = "{id}")
    public User findUserById(@PathVariable Long id) {

        return userService.findUserById(id);
    }

    @GetMapping(path = "find")
    public User findUserById(@RequestParam String username) {

        return userService.findUserByUsername(username);
    }
}
