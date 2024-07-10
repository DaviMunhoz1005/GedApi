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
@RequestMapping("user")
public class AuthenticationController {

    /*

    TODO - Corrigir para usar dto de user;
           Criar para listar usuários por nome;
           Criar para listar usuários por id;

    */

    private final AuthenticationService authenticationService;

    @PostMapping(path = "token")
    public String authenticate(Authentication authentication) {

        return authenticationService.authenticate(authentication);
    }

    @PostMapping(path = "create")
    public User createUser(@RequestBody User user) {

        return authenticationService.createUser(user);
    }
}
