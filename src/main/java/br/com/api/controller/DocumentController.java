package br.com.api.controller;

import br.com.api.dto.DocumentResponse;
import br.com.api.dto.DocumentRequest;

import br.com.api.entities.Client;
import br.com.api.entities.Document;
import br.com.api.entities.User;

import br.com.api.exception.BadRequestException;

import br.com.api.repository.UserClientRepository;
import br.com.api.repository.UserRepository;

import br.com.api.service.DocumentService;
import br.com.api.service.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

import java.io.IOException;

import java.util.List;

@RestController
@RequestMapping("/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final JwtService jwtService;
    private final UserClientRepository userClientRepository;
    private final UserRepository userRepository;

    @GetMapping(path = "find")
    public ResponseEntity<List<Document>> listDocuments() {

        if(jwtService.tokenIsStillValid(jwtService.getExpiryFromAuthentication())) {

            throw new BadRequestException(jwtService.returnIfTokenIsNoLongerValid());
        }

        String username = jwtService.getSubjectFromAuthentication();
        User user = userRepository.findByUsername(username);

        if(Boolean.TRUE.equals(user.getExcluded())) {

            throw new BadRequestException("This user has been deleted");
        }

        Boolean userBindingState = userClientRepository.findByUser(user).getApprovedRequest();

        if(Boolean.TRUE.equals(userBindingState) || userBindingState == null) {

            return new ResponseEntity<>(documentService.listAllDocumentsFromUsername(username), HttpStatus.OK);
        } else {

            throw new BadRequestException("Your link has not yet been permitted by the legal entity");
        }
    }

    @GetMapping(path = "findName")
    public ResponseEntity<List<Document>> listDocumentsByName(@Valid @RequestParam String documentName) {

        if(jwtService.tokenIsStillValid(jwtService.getExpiryFromAuthentication())) {

            throw new BadRequestException(jwtService.returnIfTokenIsNoLongerValid());
        }

        String username = jwtService.getSubjectFromAuthentication();
        User user = userRepository.findByUsername(username);

        if(Boolean.TRUE.equals(user.getExcluded())) {

            throw new BadRequestException("This user has been deleted");
        }

        String guideName = documentName + "-" + getTheCustomerOriginalUsername(username);
        Boolean userBindingState = userClientRepository.findByUser(user).getApprovedRequest();

        if(Boolean.TRUE.equals(userBindingState) || userBindingState == null) {

            return new ResponseEntity<>(documentService.listDocumentsByName(guideName, username),
                    HttpStatus.OK);
        } else {

            throw new BadRequestException("Your link has not yet been permitted by the legal entity");
        }
    }

    public String getTheCustomerOriginalUsername(String username) {

        String customerOriginalUsername = "";

        User user = userRepository.findByUsername(username);

        if(Boolean.TRUE.equals(user.getExcluded())) {

            throw new BadRequestException("This user has been deleted");
        }

        Client client = userClientRepository.findByUser(user).getClient();

        for(User finalUser : client.getUsers()) {

            if(userClientRepository.findByUser(finalUser).getApprovedRequest() == null) {

                customerOriginalUsername = finalUser.getUsername();
            }
        }

        return customerOriginalUsername;
    }

    @PostMapping(path = "upload", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyAuthority('SCOPE_CLIENT')")
    public ResponseEntity<DocumentResponse> addNewDocument(@RequestPart("document") MultipartFile document,
                                                           @RequestPart("documentRequest") DocumentRequest request)
            throws IOException {

        if(jwtService.tokenIsStillValid(jwtService.getExpiryFromAuthentication())) {

            throw new BadRequestException(jwtService.returnIfTokenIsNoLongerValid());
        }

        String username = jwtService.getSubjectFromAuthentication();
        return new ResponseEntity<>(documentService.addNewDocument(document, request, username),
                HttpStatus.CREATED);
    }

    @PutMapping(path = "upload", consumes = {"multipart/form-data"})
    @PreAuthorize("hasAnyAuthority('SCOPE_CLIENT')")
    public ResponseEntity<DocumentResponse> updateDocument(@Valid @RequestPart("document") MultipartFile document,
                                               @RequestPart("documentRequest") DocumentRequest documentRequest)
            throws IOException {

        if(jwtService.tokenIsStillValid(jwtService.getExpiryFromAuthentication())) {

            throw new BadRequestException(jwtService.returnIfTokenIsNoLongerValid());
        }

        String username = jwtService.getSubjectFromAuthentication();
        return new ResponseEntity<>(documentService.updateDocument(document, documentRequest, username),
                HttpStatus.OK);
    }

    @DeleteMapping(path = "previousVersion")
    @PreAuthorize("hasAnyAuthority('SCOPE_CLIENT')")
    public ResponseEntity<Void> usePreviousVersion(@Valid @RequestParam String documentName) {

        if(jwtService.tokenIsStillValid(jwtService.getExpiryFromAuthentication())) {

            throw new BadRequestException(jwtService.returnIfTokenIsNoLongerValid());
        }

        String username = jwtService.getSubjectFromAuthentication();
        documentService.usePreviousVersion(documentName, username);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_CLIENT')")
    public ResponseEntity<Void> deleteDocumentByName(@Valid @RequestParam String documentName) {

        if(jwtService.tokenIsStillValid(jwtService.getExpiryFromAuthentication())) {

            throw new BadRequestException(jwtService.returnIfTokenIsNoLongerValid());
        }

        String username = jwtService.getSubjectFromAuthentication();
        documentService.deleteAllDocumentWithName(documentName, username);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(path = "download/{documentName:.+}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable String documentName,
                                                 HttpServletRequest httpServletRequest) {

        if(jwtService.tokenIsStillValid(jwtService.getExpiryFromAuthentication())) {

            throw new BadRequestException(jwtService.returnIfTokenIsNoLongerValid());
        }

        String username = jwtService.getSubjectFromAuthentication();
        User user = userRepository.findByUsername(username);

        if(Boolean.TRUE.equals(user.getExcluded())) {

            throw new BadRequestException("This user has been deleted");
        }

        try {

            int substringBegin = documentName.indexOf(".");
            String guideName = documentName.substring(0, substringBegin) + "-" + username;

            Resource resource = documentService.downloadDocument(guideName + "." +
                    documentName.substring(substringBegin + 1));

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