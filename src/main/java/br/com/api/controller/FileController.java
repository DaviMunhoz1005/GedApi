package br.com.api.controller;

import br.com.api.service.FileService;

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

    /*

    TODO - Atualizar métodos para pedir o nome do usuário que está fazend a req:
                - downloadFile();

    */

    private final FileService fileService;

    @GetMapping(path = "find")
    public ResponseEntity<List<File_>> listFiles(@RequestParam String username) {

        return new ResponseEntity<>(fileService.listAllFilesFromUsername(username), HttpStatus.OK);
    }

    @GetMapping(path = "findName")
    public ResponseEntity<List<File_>> listFilesByName(@Valid @RequestParam String name,
                                                       @RequestParam String username) {

        return new ResponseEntity<>(fileService.listFilesByName(name, username), HttpStatus.OK);
    }

    @PostMapping(path = "upload", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyAuthority('SCOPE_COMMON_USER', 'SCOPE_ADMIN_COMPANY')")
    public ResponseEntity<String> addNewFile(@RequestPart("file") MultipartFile file,
                                             @RequestPart("fileDto") FileDto fileDto) throws IOException {

        String fileName = fileService.addNewFile(file, fileDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Arquivo e JSON recebidos com sucesso, nome do arquivo foi salvo como: " + fileName);
    }

    @PutMapping(path = "upload", consumes = {"multipart/form-data"})
    @PreAuthorize("hasAnyAuthority('SCOPE_COMMON_USER', 'SCOPE_ADMIN_COMPANY')")
    public ResponseEntity<File_> updateFile(@Valid @RequestPart("file") MultipartFile file,
                                            @RequestPart("json") FileDto fileDto) throws IOException {

        return new ResponseEntity<>(fileService.updateFile(file, fileDto), HttpStatus.OK);
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
    @PreAuthorize("hasAnyAuthority('SCOPE_COMMON_USER', 'SCOPE_ADMIN_COMPANY')")
    public ResponseEntity<Void> usePreviousVersion(@Valid @RequestParam String filename,
                                                   @RequestParam String username) {

        fileService.usePreviousVersion(filename, username);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_COMMON_USER', 'SCOPE_ADMIN_COMPANY')")
    public ResponseEntity<Void> deleteFileByName(@Valid @RequestParam String name, @RequestParam String username) {

        fileService.deleteAllFilesWithName(name, username);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
