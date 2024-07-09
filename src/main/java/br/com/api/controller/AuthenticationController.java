package br.com.api.controller;

import br.com.api.entities.User;
import br.com.api.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("authenticate")
public class AuthenticationController {

    /*

    TODO - corrigir para usar dto de user

    */

    private final AuthenticationService authenticationService;

    @PostMapping
    public String authenticate(Authentication authentication) {

        return authenticationService.authenticate(authentication);
    }

    @PostMapping(path = "create")
    public User createUser(@RequestBody User user) {

        return authenticationService.createUser(user);
    }
}
