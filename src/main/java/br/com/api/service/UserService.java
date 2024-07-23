package br.com.api.service;

import br.com.api.dto.UserRequest;
import br.com.api.dto.JwtResponse;
import br.com.api.dto.UserResponse;
import br.com.api.entities.Client;
import br.com.api.entities.User;
import br.com.api.entities.UserClient;
import br.com.api.repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final UserClientRepository userClientRepository;

    public JwtResponse authenticate(Authentication authentication) {

        Instant instant = Instant.now().plusSeconds(3600);

        LocalTime expiresIn = instant.atZone(ZoneId.systemDefault()).toLocalTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        String formattedTime = expiresIn.format(formatter);

        return new JwtResponse(jwtService.generateToken(authentication), formattedTime);
    }

    public UserResponse createUser(UserRequest userRequest) {

        Client existingClient = clientRepository.findByCnpjCpf(userRequest.cnpjCpf());
        User userToSave;
        User userSaved;
        UserClient userClient;

        if(existingClient == null) {

            userToSave = User.builder()
                    .username(userRequest.username())
                    .email(userRequest.email())
                    .password(passwordEncoder.encode(userRequest.password()))
                    .excluded(false)
                    .roleList(roleRepository.findById(1L).stream().toList())
                    .clients(new ArrayList<>())
                    .build();

            Client clientToSave = Client.builder()
                    .nameCorporateReason(userRequest.nameCorporateReason())
                    .cnpjCpf(userRequest.cnpjCpf())
                    .cnae(userRequest.cnae())
                    .users(new ArrayList<>())
                    .build();

            userSaved = userRepository.save(userToSave);
            Client clientSaved = clientRepository.save(clientToSave);

            userClient = UserClient.builder()
                    .user(userSaved)
                    .client(clientSaved)
                    .approvedRequest(null)
                    .build();

            userClientRepository.save(userClient);

            userSaved.getClients().add(clientSaved);
            clientSaved.getUsers().add(userSaved);

            return UserResponse.builder()
                    .userId(userSaved.getUuid())
                    .clientId(clientSaved.getUuid())
                    .username(userRequest.username())
                    .nameCorporateReason(userRequest.nameCorporateReason())
                    .email(userRequest.email())
                    .cnpjCpf(userRequest.cnpjCpf())
                    .cnae(userRequest.cnae())
                    .excluded(userToSave.getExcluded())
                    .role(userToSave.getRoleList().get(0))
                    .build();
        } else {

            userToSave = User.builder()
                    .username(userRequest.username())
                    .email(userRequest.email())
                    .password(passwordEncoder.encode(userRequest.password()))
                    .excluded(false)
                    .roleList(roleRepository.findById(2L).stream().toList())
                    .clients(new ArrayList<>())
                    .build();

            userSaved = userRepository.save(userToSave);

            userClient = UserClient.builder()
                    .user(userSaved)
                    .client(existingClient)
                    .approvedRequest(false)
                    .build();

            userClientRepository.save(userClient);

            userSaved.getClients().add(existingClient);
            existingClient.getUsers().add(userSaved);

            return UserResponse.builder()
                    .userId(userSaved.getUuid())
                    .clientId(existingClient.getUuid())
                    .username(userRequest.username())
                    .nameCorporateReason(userRequest.nameCorporateReason())
                    .email(userRequest.email())
                    .cnpjCpf(userRequest.cnpjCpf())
                    .cnae(userRequest.cnae())
                    .excluded(userToSave.getExcluded())
                    .role(userToSave.getRoleList().get(0))
                    .build();
        }
    }

    public UserResponse findUserByUsername(String username) {

        User user = userRepository.findByUsername(username);
        Client client = userClientRepository.findByUser(user).getClient();

        return UserResponse.builder()
                .userId(user.getUuid())
                .clientId(client.getUuid())
                .username(username)
                .nameCorporateReason(client.getNameCorporateReason())
                .email(user.getEmail())
                .cnpjCpf(client.getCnpjCpf())
                .cnae(client.getCnae())
                .excluded(user.getExcluded())
                .role(user.getRoleList().get(0))
                .build();
    }
}
