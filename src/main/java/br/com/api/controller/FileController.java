package br.com.api.controller;

import br.com.api.entities.Role;
import br.com.api.entities.User;
import br.com.api.entities.enums.RoleName;
import br.com.api.repository.EmployeeRepository;
import br.com.api.service.FileService;

import br.com.api.service.JwtService;
import br.com.api.service.UserService;
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
    public ResponseEntity<List<File_>> listFiles() {


        String username = jwtService.getSubjectFromAuthentication();
        return new ResponseEntity<>(fileService.listAllFilesFromUsername(username), HttpStatus.OK);
    }

    @GetMapping(path = "findName")
    public ResponseEntity<List<File_>> listFilesByName(@Valid @RequestParam String name) {

        String username = jwtService.getSubjectFromAuthentication();

        User user = getTheUserRole(username);
        String baseNameRenamed = name + "-" + user.getUsername();

        return new ResponseEntity<>(fileService.listFilesByName(baseNameRenamed, username), HttpStatus.OK);
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
    public ResponseEntity<File_> addNewFile(@RequestPart("file") MultipartFile file,
                                             @RequestPart("fileDto") FileDto fileDto) throws IOException {

        String username = jwtService.getSubjectFromAuthentication();
        return new ResponseEntity<>(fileService.addNewFile(file, fileDto, username), HttpStatus.CREATED);
    }

    @PutMapping(path = "upload", consumes = {"multipart/form-data"})
    @PreAuthorize("hasAnyAuthority('SCOPE_CLIENT')")
    public ResponseEntity<File_> updateFile(@Valid @RequestPart("file") MultipartFile file,
                                            @RequestPart("json") FileDto fileDto) throws IOException {

        String username = jwtService.getSubjectFromAuthentication();
        return new ResponseEntity<>(fileService.updateFile(file, fileDto, username), HttpStatus.OK);
    }

    @GetMapping(path = "download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName,
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

    @DeleteMapping(path = "previousVersion")
    @PreAuthorize("hasAnyAuthority('SCOPE_CLIENT')")
    public ResponseEntity<Void> usePreviousVersion(@Valid @RequestParam String filename) {

        String username = jwtService.getSubjectFromAuthentication();
        fileService.usePreviousVersion(filename, username);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_CLIENT')")
    public ResponseEntity<Void> deleteFileByName(@Valid @RequestParam String name) {

        String username = jwtService.getSubjectFromAuthentication();
        fileService.deleteAllFilesWithName(name, username);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}