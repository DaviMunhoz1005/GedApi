package br.com.api.controller;

import br.com.api.entities.User;
import br.com.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("user")
public class UserController {

    /*

    TODO - Corrigir para usar dto de user;

    */

    private final UserService userService;

    @PostMapping(path = "token")
    public String authenticate(Authentication authentication) {

        return userService.authenticate(authentication);
    }

    @PostMapping(path = "create")
    public User createUser(@RequestBody User user) {

        return userService.createUser(user);
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
