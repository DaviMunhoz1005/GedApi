package br.com.api.controller;

import br.com.api.entities.Role;
import br.com.api.entities.User;
import br.com.api.entities.enums.RoleName;
import br.com.api.exception.BadRequestException;
import br.com.api.repository.EmployeeRepository;
import br.com.api.service.FileService;

import br.com.api.service.JwtService;
import br.com.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;

import br.com.api.dto.FileDto;
import br.com.api.entities.File_;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final UserService userService;
    private final EmployeeRepository employeeRepository;
    private final JwtService jwtService;

    @GetMapping(path = "find")
    @Operation(summary = "List the files the user owns", description = "Operation that lists all files that " +
            "the user has, returns a list, which may be empty.")
    public ResponseEntity<List<File_>> listFiles() {

        if(tokenIsStillValid(jwtService.getExpiryFromAuthentication())) {

            throw new BadRequestException(returnIfTokenIsNoLongerValid());
        }

        String username = jwtService.getSubjectFromAuthentication();
        return new ResponseEntity<>(fileService.listAllFilesFromUsername(username), HttpStatus.OK);
    }

    @GetMapping(path = "findName")
    @Operation(summary = "List a file according to the given name", description = "Operation that lists all " +
            "user files according to the name provided by the filename parameter, returns a list, which can" +
            " be empty.")
    public ResponseEntity<List<File_>> listFilesByName(@Parameter(description = "File name")
                                                           @Valid @RequestParam String fileName) {

        if(tokenIsStillValid(jwtService.getExpiryFromAuthentication())) {

            throw new BadRequestException(returnIfTokenIsNoLongerValid());
        }

        String username = jwtService.getSubjectFromAuthentication();

        User user = getTheUserRole(username);
        String fileNameRenamed = fileName + "-" + user.getUsername();

        return new ResponseEntity<>(fileService.listFilesByName(fileNameRenamed, username), HttpStatus.OK);
    }

    public User getTheUserRole(String username) {

        User user = userService.findUserByUsername(username);
        Role role = user.getRoleList().get(0);

        if(role.getRoleName() == RoleName.EMPLOYEE) {

            user = userService.findUserByUsername(employeeRepository
                    .findByUsername(user.getUsername())
                    .getClient()
                    .getUsername());
        }

        return user;
    }

    @PostMapping(path = "upload", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyAuthority('SCOPE_CLIENT')")
    @Operation(summary = "Create a new file", description = "Operation that creates a new file, receives a " +
            "Multipartfile and a FileDto and returns the file persisted in the database. The application " +
            "renames the sent file to save it in the database with the indexed user name, only Client type " +
            "users can do this this request.")
    public ResponseEntity<File_> addNewFile(@RequestPart("file") MultipartFile file,
                                             @RequestPart("fileDto") FileDto fileDto) throws IOException {

        if(tokenIsStillValid(jwtService.getExpiryFromAuthentication())) {

            throw new BadRequestException(returnIfTokenIsNoLongerValid());
        }

        String username = jwtService.getSubjectFromAuthentication();
        return new ResponseEntity<>(fileService.addNewFile(file, fileDto, username), HttpStatus.CREATED);
    }

    @PutMapping(path = "upload", consumes = {"multipart/form-data"})
    @PreAuthorize("hasAnyAuthority('SCOPE_CLIENT')")
    @Operation(summary = "Update a file", description = "Operation that updates a new file, receives a " +
            "Multipartfile and a FileDto and returns the updated persisted file in the database. The " +
            "application renames the file prior to the update to maintain a version save, only Client type " +
            "users can make this request.")
    public ResponseEntity<File_> updateFile(@Valid @RequestPart("file") MultipartFile file,
                                            @RequestPart("json") FileDto fileDto) throws IOException {

        if(tokenIsStillValid(jwtService.getExpiryFromAuthentication())) {

            throw new BadRequestException(returnIfTokenIsNoLongerValid());
        }

        String username = jwtService.getSubjectFromAuthentication();
        return new ResponseEntity<>(fileService.updateFile(file, fileDto, username), HttpStatus.OK);
    }

    @DeleteMapping(path = "previousVersion")
    @PreAuthorize("hasAnyAuthority('SCOPE_CLIENT')")
    @Operation(summary = "Use the previous version of the given file", description = "Operation that " +
            "deletes the current version of the file and takes the previous version, if it exists, " +
            "receives a parameter filename returns void, the application renames the file before the " +
            "current one to maintain a version save, only Client type users can make this request.")
    public ResponseEntity<Void> usePreviousVersion(@Parameter(description = "File name")
                                                       @Valid @RequestParam String filename) {

        if(tokenIsStillValid(jwtService.getExpiryFromAuthentication())) {

            throw new BadRequestException(returnIfTokenIsNoLongerValid());
        }

        String username = jwtService.getSubjectFromAuthentication();
        fileService.usePreviousVersion(filename, username);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_CLIENT')")
    @Operation(summary = "Deletes all files with a given name", description = "Operation that deletes " +
            "all files according to the filename informed in the parameter, only Client type users can" +
            " make this request.")
    public ResponseEntity<Void> deleteFileByName(@Parameter(description = "File name")
                                                     @Valid @RequestParam String fileName) {

        if(tokenIsStillValid(jwtService.getExpiryFromAuthentication())) {

            throw new BadRequestException(returnIfTokenIsNoLongerValid());
        }

        String username = jwtService.getSubjectFromAuthentication();
        fileService.deleteAllFilesWithName(fileName, username);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public boolean tokenIsStillValid(Instant expiresAtToken) {

        return expiresAtToken.isBefore(Instant.now());
    }

    public String returnIfTokenIsNoLongerValid() {

        return "Your token has run out of time, please log in again";
    }

    @GetMapping(path = "download/{fileName:.+}")
    @Operation(summary = "Download a file", description = "Operation that downloads a file according to" +
            " the name and extension passed by url.")
    public ResponseEntity<Resource> downloadFile(@Parameter(description = "File name")
                                                     @PathVariable String fileName,
                                                 HttpServletRequest httpServletRequest) {
        try {

            Resource resource = fileService.downloadFile(fileName);

            String contentType = httpServletRequest.getServletContext()
                    .getMimeType(resource.getFile().getAbsolutePath());

            if (contentType == null) {

                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch(Exception ex) {

            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}