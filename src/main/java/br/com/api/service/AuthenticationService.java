package br.com.api.service;

import br.com.api.entities.Role;
import br.com.api.entities.User;
import br.com.api.exception.BadRequestException;
import br.com.api.repository.RoleRepository;
import br.com.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    /*

    TODO - corrigir para usar dto de user

    */

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public String authenticate(Authentication authentication) {

        return jwtService.generateToken(authentication);
    }

    public User createUser(User user) {

        List<Role> roleList = user.getRoles().stream()
                .map(role -> roleRepository.findById(role.getId())
                        .orElseThrow(() -> new BadRequestException("Role not found with id " + role.getId())))
                .toList();

        User userToSave = User.builder()
                .username(user.getUsername())
                .password(passwordEncoder.encode(user.getPassword()))
                .roles(roleList)
                .build();

        userRepository.save(userToSave);

        return userToSave;
    }
}
