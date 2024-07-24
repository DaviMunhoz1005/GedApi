package br.com.api.service;

import br.com.api.dto.EmployeeResponse;
import br.com.api.dto.UserRequest;
import br.com.api.dto.JwtResponse;
import br.com.api.dto.UserResponse;

import br.com.api.entities.Client;
import br.com.api.entities.User;
import br.com.api.entities.UserClient;

import br.com.api.exception.BadRequestException;

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
import java.util.List;

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

        if(existingClient == null) {

            return createNewClient(userRequest);
        } else if(existingClient.getNameCorporateReason() != null) {

            return createNewEmployee(existingClient, userRequest);
        } else {

            throw new BadRequestException("It is not permitted to link to a natural person, only to " +
                    "legal entities");
        }
    }

    public UserResponse createNewClient(UserRequest userRequest) {

        User userToSave = User.builder()
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

        User userSaved = userRepository.save(userToSave);
        Client clientSaved = clientRepository.save(clientToSave);

        UserClient userClient = UserClient.builder()
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
    }

    public UserResponse createNewEmployee(Client existingClient, UserRequest userRequest) {

        User user = null;

        for(User finalUser : existingClient.getUsers()) {

            if(userClientRepository.findByUser(finalUser).getApprovedRequest() == null) {

                user = finalUser;
            }
        }

        assert user != null;
        if(Boolean.TRUE.equals(user.getExcluded())) {

            throw new BadRequestException("This user you are trying to link to has been deleted");
        }

        User userToSave = User.builder()
                .username(userRequest.username())
                .email(userRequest.email())
                .password(passwordEncoder.encode(userRequest.password()))
                .excluded(false)
                .roleList(roleRepository.findById(2L).stream().toList())
                .clients(new ArrayList<>())
                .build();

        User userSaved = userRepository.save(userToSave);

        UserClient userClient = UserClient.builder()
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

    public List<EmployeeResponse> listOfUsersWhoWantToLink(Client client) {

        List<EmployeeResponse> listOfUsersWhoWantToLink = new ArrayList<>();
        EmployeeResponse employeeResponse;

        for(User user : client.getUsers()) {

            if(Boolean.FALSE.equals(userClientRepository.findByUser(user).getApprovedRequest())) {

                employeeResponse = EmployeeResponse.builder()
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .excluded(user.getExcluded())
                        .approvedRequest(false)
                        .build();

                listOfUsersWhoWantToLink.add(employeeResponse);
            }
        }

        return listOfUsersWhoWantToLink;
    }

    public EmployeeResponse allowUserLinking(Client client, String username) {

        User userToSetApprovedRequest = null;

        for(EmployeeResponse response : listOfUsersWhoWantToLink(client)) {

            if(username.equals(response.username())) {

                userToSetApprovedRequest = userRepository.findByUsername(response.username());
            }
        }

        if(userToSetApprovedRequest != null) {

            userClientRepository.findByUser(userToSetApprovedRequest).setApprovedRequest(true);
            userRepository.save(userToSetApprovedRequest);

            return EmployeeResponse.builder()
                    .username(userToSetApprovedRequest.getUsername())
                    .email(userToSetApprovedRequest.getEmail())
                    .excluded(userToSetApprovedRequest.getExcluded())
                    .approvedRequest(true)
                    .build();
        } else {

            throw new BadRequestException("The username provided does not exist");
        }
    }

    public UserResponse deleteAccount(User user) {

        user.setExcluded(true);
        user.setEmail("EXCLUDED");
        userRepository.save(user);

        return UserResponse.builder()
                .userId(user.getUuid())
                .clientId(user.getClients().get(0).getUuid())
                .username(user.getUsername())
                .nameCorporateReason(user.getClients().get(0).getNameCorporateReason())
                .email(user.getEmail())
                .cnpjCpf(user.getClients().get(0).getCnpjCpf())
                .cnae(user.getClients().get(0).getCnae())
                .excluded(user.getExcluded())
                .role(user.getRoleList().get(0))
                .build();
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
