package br.com.api.service;

import br.com.api.dto.ClientDto;
import br.com.api.dto.EmployeeDto;
import br.com.api.dto.JwtResponse;
import br.com.api.entities.Client;
import br.com.api.entities.Employee;
import br.com.api.entities.User;
import br.com.api.exception.BadRequestException;
import br.com.api.repository.ClientRepository;
import br.com.api.repository.EmployeeRepository;
import br.com.api.repository.RoleRepository;
import br.com.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;

    public JwtResponse authenticate(Authentication authentication) {

        Instant  instant = Instant.now().plusSeconds(3600L);

        LocalTime expiresIn = instant.atZone(ZoneId.systemDefault()).toLocalTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        String formattedTime = expiresIn.format(formatter);

        return new JwtResponse(jwtService.generateToken(authentication), formattedTime);
    }

    public Client createClient(ClientDto clientDto) {

        Client clientToSave = Client.builder()
                .username(clientDto.username())
                .password(passwordEncoder.encode(clientDto.password()))
                .roleList(roleRepository.findById(clientDto.roleInt()).stream().toList())
                .build();

        clientRepository.save(clientToSave);
        return clientToSave;
    }

    public Employee createEmployee(EmployeeDto employeeDto) {

        Employee employeeToSave = Employee.builder()
                .username(employeeDto.username())
                .password(passwordEncoder.encode(employeeDto.password()))
                .roleList(roleRepository.findById(employeeDto.roleInt()).stream().toList())
                .client(clientRepository.findByUsername(employeeDto.clientUsername()))
                .build();

        employeeRepository.save(employeeToSave);
        return employeeToSave;
    }

    public User findUserByUsername(String username) {

        User user = employeeRepository.findByUsername(username);

        if(user == null) {

            user = clientRepository.findByUsername(username);
        }

        if(user == null) {

            throw new BadRequestException("There is no client linked to the username: " + username);
        }

        return user;
    }
}
