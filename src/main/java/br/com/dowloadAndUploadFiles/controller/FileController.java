package br.com.dowloadAndUploadFiles.controller;

import br.com.dowloadAndUploadFiles.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.dowloadAndUploadFiles.dto.FileDto;
import br.com.dowloadAndUploadFiles.entities.File;
import br.com.dowloadAndUploadFiles.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping
    public ResponseEntity<List<File>> listFiles() {

        return new ResponseEntity<>(fileService.listAllFiles(), HttpStatus.OK);
    }

    @GetMapping(path = "param")
    public ResponseEntity<List<File>> listFilesByName(@Valid @RequestParam String name) {

        return new ResponseEntity<>(fileService.listFilesByName(name), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<File> addNewFile(@Valid @RequestBody FileDto fileDto) {

        return new ResponseEntity<>(fileService.addNewFile(fileDto), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<File> updateFile(@Valid @RequestBody FileDto fileDto) {

        return new ResponseEntity<>(fileService.updateFile(fileDto), HttpStatus.OK);
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
