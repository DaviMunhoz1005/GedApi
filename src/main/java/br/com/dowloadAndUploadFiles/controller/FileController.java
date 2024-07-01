package br.com.dowloadAndUploadFiles.controller;

import br.com.dowloadAndUploadFiles.service.FileService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import br.com.dowloadAndUploadFiles.dto.FileDto;
import br.com.dowloadAndUploadFiles.entities.File_;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.net.MalformedURLException;
import java.util.List;

@Controller
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /*

    TODO - fazer com que retorne o novo nome do arquivo caso ele tenha sido modificado no POST;

    */

    @GetMapping
    public ResponseEntity<List<File_>> listFiles() {

        return new ResponseEntity<>(fileService.listAllFiles(), HttpStatus.OK);
    }

    @GetMapping(path = "param")
    public ResponseEntity<List<File_>> listFilesByName(@Valid @RequestParam String name) {

        return new ResponseEntity<>(fileService.listFilesByName(name), HttpStatus.OK);
    }

    @PostMapping(path = "upload", consumes = "multipart/form-data")
    public ResponseEntity<String> addNewFile(@RequestPart("file") MultipartFile file,
                                             @RequestPart("fileDto") FileDto fileDto) throws IOException {

        fileService.addNewFile(file, fileDto);

        return ResponseEntity.status(HttpStatus.CREATED).body("Arquivo e JSON recebidos com sucesso!");
    }

    @PutMapping(path = "upload", consumes = {"multipart/form-data"})
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

    @DeleteMapping(path = "{id}")
    public ResponseEntity<Void> deleteFileById(@Valid @PathVariable Long id) {

        fileService.deleteFileById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteFileByName(@Valid @RequestParam String name) {

        fileService.deleteFileByName(name);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
