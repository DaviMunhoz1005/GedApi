package br.com.api.controller;

import br.com.api.dto.UserResponse;
import br.com.api.entities.Document;
import br.com.api.entities.Role;
import br.com.api.entities.enums.RoleName;
import br.com.api.exception.BadRequestException;
import br.com.api.service.DocumentService;

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

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final UserService userService;
    private final JwtService jwtService;

    @GetMapping(path = "find")
    public ResponseEntity<List<Document>> listDocuments() {

        if(tokenIsStillValid(jwtService.getExpiryFromAuthentication())) {

            throw new BadRequestException(returnIfTokenIsNoLongerValid());
        }

        String username = jwtService.getSubjectFromAuthentication();
        return new ResponseEntity<>(documentService.listAllDocumentsFromUsername(username), HttpStatus.OK);
    }

    @GetMapping(path = "findName")
    public ResponseEntity<List<Document>> listDocumentsByName(@Valid @RequestParam String documentName) {

        if(tokenIsStillValid(jwtService.getExpiryFromAuthentication())) {

            throw new BadRequestException(returnIfTokenIsNoLongerValid());
        }

        String username = jwtService.getSubjectFromAuthentication();

        UserResponse user = getTheUserRole(username);
        String documentNameRenamed = documentName + "-" + user.username();

        return new ResponseEntity<>(documentService.listDocumentsByName(documentNameRenamed, username), HttpStatus.OK);
    }

    public UserResponse getTheUserRole(String username) {

        UserResponse user = userService.findUserByUsername(username);
        Role role = user.role();

        if(role.getRoleName() == RoleName.EMPLOYEE) {

            user = null;
        }

        return user;
    }

    @PostMapping(path = "upload", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyAuthority('SCOPE_CLIENT')")
    public ResponseEntity<Document> addNewDocument(@RequestPart("document") MultipartFile document,
                                               @RequestPart("documentDto") FileDto documentDto)
            throws IOException {

        if(tokenIsStillValid(jwtService.getExpiryFromAuthentication())) {

            throw new BadRequestException(returnIfTokenIsNoLongerValid());
        }

        String username = jwtService.getSubjectFromAuthentication();
        return new ResponseEntity<>(documentService.addNewDocument(document, documentDto, username), HttpStatus.CREATED);
    }

    @PutMapping(path = "upload", consumes = {"multipart/form-data"})
    @PreAuthorize("hasAnyAuthority('SCOPE_CLIENT')")
    public ResponseEntity<Document> updateDocument(@Valid @RequestPart("document") MultipartFile document,
                                               @RequestPart("json") FileDto fileDto) throws IOException {

        if(tokenIsStillValid(jwtService.getExpiryFromAuthentication())) {

            throw new BadRequestException(returnIfTokenIsNoLongerValid());
        }

        String username = jwtService.getSubjectFromAuthentication();
        return new ResponseEntity<>(documentService.updateDocument(document, fileDto, username), HttpStatus.OK);
    }

    @DeleteMapping(path = "previousVersion")
    @PreAuthorize("hasAnyAuthority('SCOPE_CLIENT')")
    public ResponseEntity<Void> usePreviousVersion(@Valid @RequestParam String documentName) {

        if(tokenIsStillValid(jwtService.getExpiryFromAuthentication())) {

            throw new BadRequestException(returnIfTokenIsNoLongerValid());
        }

        String username = jwtService.getSubjectFromAuthentication();
        documentService.usePreviousVersion(documentName, username);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_CLIENT')")
    public ResponseEntity<Void> deleteDocumentByName(@Valid @RequestParam String documentName) {

        if(tokenIsStillValid(jwtService.getExpiryFromAuthentication())) {

            throw new BadRequestException(returnIfTokenIsNoLongerValid());
        }

        String username = jwtService.getSubjectFromAuthentication();
        documentService.deleteAllDocumentWithName(documentName, username);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public boolean tokenIsStillValid(Instant expiresAtToken) {

        return expiresAtToken.isBefore(Instant.now());
    }

    public String returnIfTokenIsNoLongerValid() {

        return "Your token has run out of time, please log in again";
    }

    @GetMapping(path = "download/{documentName:.+}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable String documentName,
                                                 HttpServletRequest httpServletRequest) {
        try {

            Resource resource = documentService.downloadDocument(documentName);

            String contentType = httpServletRequest.getServletContext()
                    .getMimeType(resource.getFile().getAbsolutePath());

            if (contentType == null) {

                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; documentName=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch(Exception ex) {

            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}