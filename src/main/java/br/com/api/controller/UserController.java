package br.com.api.controller;

import br.com.api.domain.dto.*;

import br.com.api.domain.entities.Clients;

import br.com.api.domain.entities.Users;
import br.com.api.exception.BadRequestException;

import br.com.api.repository.UserClientRepository;
import br.com.api.repository.UserRepository;

import br.com.api.service.JwtService;
import br.com.api.service.UserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
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
    public JwtResponse authenticate(@RequestBody JwtRequest jwtRequest) {

        return userService.authenticate(jwtRequest);
    }

    @PostMapping(path = "create")
    public UserResponse createUser(@Valid @RequestBody UserRequest userRequest) {

        return userService.createUser(userRequest);
    }

    @GetMapping(path = "allowUserLink")
    @PreAuthorize("hasAnyAuthority('SCOPE_CLIENT')")
    public ResponseEntity<List<EmployeeResponse>> listOfUsersWhoWantToLink() {

        jwtService.checkIfTokenIsValid();

        String username = jwtService.getSubjectFromAuthentication();
        Users user = userRepository.findByUsername(username);

        jwtService.checkIfUserWasDeleted(user);

        Clients client = userClientRepository.findByUser(user).getClient();

        return new ResponseEntity<>(userService.listOfUsersWhoWantToLink(client), HttpStatus.OK);
    }

    @PutMapping(path = "allowUserLink")
    @PreAuthorize("hasAnyAuthority('SCOPE_CLIENT')")
    public ResponseEntity<EmployeeResponse> allowUserLinking(@RequestParam String usernameToAllowLinking) {

        jwtService.checkIfTokenIsValid();

        String username = jwtService.getSubjectFromAuthentication();
        Users user = userRepository.findByUsername(username);

        jwtService.checkIfUserWasDeleted(user);

        Clients client = userClientRepository.findByUser(user).getClient();

        return new ResponseEntity<>(userService.allowUserLinking(client, usernameToAllowLinking),
                HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<UserResponse> deleteAccount() {

        jwtService.checkIfTokenIsValid();

        String username = jwtService.getSubjectFromAuthentication();
        Users user = userRepository.findByUsername(username);

        if(Boolean.TRUE.equals(user.getExcluded())) {

            throw new BadRequestException("This user has been deleted");
        }

        return new ResponseEntity<>(userService.deleteAccount(user), HttpStatus.OK);
    }

    @GetMapping(path = "find")
    public UserResponse findUserByUsername(@RequestParam String username) {

        return userService.findUserByUsername(username);
    }
}