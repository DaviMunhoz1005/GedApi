package br.com.api.controller;

import br.com.api.dto.EmployeeResponse;
import br.com.api.dto.UserRequest;
import br.com.api.dto.JwtResponse;
import br.com.api.dto.UserResponse;

import br.com.api.entities.Client;

import br.com.api.exception.BadRequestException;

import br.com.api.repository.UserClientRepository;
import br.com.api.repository.UserRepository;

import br.com.api.service.JwtService;
import br.com.api.service.UserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("user")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;
    private final UserClientRepository userClientRepository;
    private final UserRepository userRepository;

    @PostMapping(path = "token")
    public JwtResponse authenticate(Authentication authentication) {

        return userService.authenticate(authentication);
    }

    @PostMapping(path = "create")
    public UserResponse createUser(@Valid @RequestBody UserRequest userRequest) {

        return userService.createUser(userRequest);
    }

    @GetMapping(path = "allowUserLink")
    public ResponseEntity<List<EmployeeResponse>> listOfUsersWhoWantToLink() {

        if(jwtService.tokenIsStillValid(jwtService.getExpiryFromAuthentication())) {

            throw new BadRequestException(jwtService.returnIfTokenIsNoLongerValid());
        }

        String username = jwtService.getSubjectFromAuthentication();
        Client client = userClientRepository.findByUser(userRepository.findByUsername(username)).getClient();

        return new ResponseEntity<>(userService.listOfUsersWhoWantToLink(client), HttpStatus.OK);
    }

    @PutMapping(path = "allowUserLink")
    public ResponseEntity<EmployeeResponse> allowUserLinking(@RequestParam String usernameToAllowLinking) {

        if(jwtService.tokenIsStillValid(jwtService.getExpiryFromAuthentication())) {

            throw new BadRequestException(jwtService.returnIfTokenIsNoLongerValid());
        }

        String username = jwtService.getSubjectFromAuthentication();
        Client client = userClientRepository.findByUser(userRepository.findByUsername(username)).getClient();

        return new ResponseEntity<>(userService.allowUserLinking(client, usernameToAllowLinking),
                HttpStatus.OK);
    }

    @GetMapping(path = "find")
    public UserResponse findUserByUsername(@RequestParam String username) {

        return userService.findUserByUsername(username);
    }
}