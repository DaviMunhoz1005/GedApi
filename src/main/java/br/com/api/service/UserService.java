package br.com.api.service;

import br.com.api.dto.JwtResponse;
import br.com.api.dto.UserDto;
import br.com.api.entities.User;
import br.com.api.exception.BadRequestException;
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

    public JwtResponse authenticate(Authentication authentication) {

        Instant  instant = Instant.now().plusSeconds(3600L);

        LocalTime expiresIn = instant.atZone(ZoneId.systemDefault()).toLocalTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        String formattedTime = expiresIn.format(formatter);

        return new JwtResponse(jwtService.generateToken(authentication), formattedTime);
    }

    public User createUser(UserDto userDto) {

        User user = User.builder()
                .username(userDto.username())
                .password(userDto.password())
                .roles(roleRepository.findById(userDto.roleInt()).stream().toList())
                .build();

        User userToSave = User.builder()
                .username(user.getUsername())
                .password(passwordEncoder.encode(user.getPassword()))
                .roles(user.getRoles())
                .build();

        userRepository.save(userToSave);

        return userToSave;
    }

    public User findUserById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("There is no user linked to the id: " + id));
    }

    public User findUserByUsername(String username) {

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new
                        BadRequestException("There is no user linked to the username: " + username));
    }
}
