package br.com.api.controller;

import br.com.api.dto.ClientDto;
import br.com.api.dto.EmployeeDto;
import br.com.api.dto.JwtResponse;
import br.com.api.dto.UserDto;
import br.com.api.entities.Client;
import br.com.api.entities.Employee;
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

    @PostMapping(path = "create/client")
    public Client createClient(@Valid @RequestBody ClientDto clientDto) {

        return userService.createClient(clientDto);
    }

    @PostMapping(path = "create/employee")
    public Employee createEmployee(@Valid @RequestBody EmployeeDto employeeDto) {

        return userService.createEmployee(employeeDto);
    }

    @GetMapping(path = "find")
    public User findUserByUsername(@RequestParam String username) {

        return userService.findUserByUsername(username);
    }
}
