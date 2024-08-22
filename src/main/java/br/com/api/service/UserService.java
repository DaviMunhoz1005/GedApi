package br.com.api.service;

import br.com.api.domain.dto.*;

import br.com.api.domain.entities.Clients;
import br.com.api.domain.entities.Users;
import br.com.api.domain.entities.UserClient;

import br.com.api.exception.BadRequestException;

import br.com.api.repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final UserClientRepository userClientRepository;

    public JwtResponse authenticate(JwtRequest jwtRequest) {

        Instant instant = Instant.now().plusSeconds(3600);
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        String formattedTime = zonedDateTime.format(formatter);

        Users user = userRepository.findByUsername(jwtRequest.username());

        return new JwtResponse(jwtService.generateToken(jwtRequest.username(), user.getRoleList().get(0).getRoleName()),
                formattedTime);
    }

    public UserResponse createUser(UserRequest userRequest) {

        Clients existingClient = clientRepository.findByCnpjCpf(userRequest.cnpjCpf());

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

        Users userToSave = new Users(userRequest, passwordEncoder.encode(userRequest.password()),
                roleRepository.findById(1L).stream().toList());

        Clients clientToSave = new Clients(userRequest);

        Users userSaved = userRepository.save(userToSave);
        Clients clientSaved = clientRepository.save(clientToSave);

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

    public UserResponse createNewEmployee(Clients existingClient, UserRequest userRequest) {

        Users user = null;

        for(Users finalUser : existingClient.getUsers()) {

            if(userClientRepository.findByUser(finalUser).getApprovedRequest() == null) {

                user = finalUser;
            }
        }

        assert user != null;
        if(Boolean.TRUE.equals(user.getExcluded())) {

            throw new BadRequestException("This user you are trying to link to has been deleted");
        }

        Users userToSave = new Users(userRequest, passwordEncoder.encode(userRequest.password()),
                roleRepository.findById(2L).stream().toList());

        Users userSaved = userRepository.save(userToSave);

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

    public List<EmployeeResponse> listOfUsersWhoWantToLink(Clients client) {

        List<EmployeeResponse> listOfUsersWhoWantToLink = new ArrayList<>();
        EmployeeResponse employeeResponse;

        for(Users user : client.getUsers()) {

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

    public EmployeeResponse allowUserLinking(Clients client, String username) {

        Users userToSetApprovedRequest = null;

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

    public UserResponse deleteAccount(Users user) {

        user.setExcluded(true);
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

        Users user = userRepository.findByUsername(username);
        Clients client = userClientRepository.findByUser(user).getClient();

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

    public List<String> listCnpjCpf() {

        return clientRepository.findAll().stream()
                .map(Clients::getCnpjCpf)
                .filter(Objects::nonNull)
                .toList();
    }

    public List<String> listCnae() {

        return clientRepository.findAll().stream()
                .map(Clients::getCnae)
                .filter(Objects::nonNull)
                .toList();
    }

    public List<String> listNameCorporateReason() {

        return clientRepository.findAll().stream()
                .map(Clients::getNameCorporateReason)
                .filter(Objects::nonNull)
                .toList();
    }
}
