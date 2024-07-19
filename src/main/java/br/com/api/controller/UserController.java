package br.com.api.controller;

import br.com.api.dto.ClientDto;
import br.com.api.dto.EmployeeDto;
import br.com.api.dto.JwtResponse;
import br.com.api.entities.Client;
import br.com.api.entities.Employee;
import br.com.api.entities.User;
import br.com.api.exception.BadRequestExceptionDetails;
import br.com.api.exception.UnauthorizedExceptionDetails;
import br.com.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "Authenticate User", description = "Operation to authenticate the user and return a " +
            "Token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token created and returned successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = JwtResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "No authorization to take a token, please check " +
                    "the information provided.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UnauthorizedExceptionDetails.class))
            )
    })
    public JwtResponse authenticate(Authentication authentication) {

        return userService.authenticate(authentication);
    }

    @PostMapping(path = "create/client")
    @Operation(summary = "Create Client", description = "Operation that receives a ClientDto and creates a user" +
            " of type Client, returning the object.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client created and returned successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Client.class))
            ),
            @ApiResponse(responseCode = "401", description = "Without authorization to create a client, check " +
                    "the information provided.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UnauthorizedExceptionDetails.class))
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true, content = @Content(mediaType = "application/json",
            examples = @ExampleObject(name = "New Client",
                    value = "{ " +
                            "\"name\": \"client\", " +
                            "\"password\": \"password123\", " +
                            "\"roleInt\": \"1\" }"
            ))
    )
    public Client createClient(@Valid @RequestBody ClientDto clientDto) {

        return userService.createClient(clientDto);
    }

    @PostMapping(path = "create/employee")
    @Operation(summary = "Create Employee", description = "Operation that receives a EmployeeDto and creates a " +
            "user of type Employee, returning the object.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee created and returned successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Employee.class))
            ),
            @ApiResponse(responseCode = "401", description = "Without authorization to create a employee, check " +
                    "the information provided.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UnauthorizedExceptionDetails.class))
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true, content = @Content(mediaType = "application/json",
            examples = @ExampleObject(name = "New Client",
                    value = "{ " +
                            "\"name\": \"employee\", " +
                            "\"password\": \"password123\", " +
                            "\"roleInt\": \"2\", " +
                            "\"clientUsername\": \"client\" " +
                            "}"
            ))
    )
    public Employee createEmployee(@Valid @RequestBody EmployeeDto employeeDto) {

        return userService.createEmployee(employeeDto);
    }

    @GetMapping(path = "find")
    @Operation(summary = "Find user by username", description = "Operation that receives a username parameter " +
            "and returns a user if it exists in the database, if it does not exist it returns an exception.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found and returned successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "There is no user with the given name.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BadRequestExceptionDetails.class))
            )
    })
    public User findUserByUsername(@Parameter(description = "Username") @RequestParam String username) {

        return userService.findUserByUsername(username);
    }
}