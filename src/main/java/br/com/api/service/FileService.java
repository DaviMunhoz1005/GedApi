package br.com.api.service;

import br.com.api.config.FileStorageProperties;
import br.com.api.dto.FileDto;
import br.com.api.entities.File_;
import br.com.api.exception.BadRequestException;
import br.com.api.repository.FileRepository;

import jakarta.transaction.Transactional;

import org.apache.commons.io.FilenameUtils;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.Objects;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final FileStorageProperties fileStorageProperties;
    private final Path fileStorageLocation;

    /*

    TODO -

    */

    public FileService() {

        this.fileRepository = null;
        this.fileStorageProperties = null;
        this.fileStorageLocation = null;
    }

    @Autowired
    public FileService(FileRepository fileRepository, FileStorageProperties fileStorageProperties) {

        this.fileRepository = fileRepository;
        this.fileStorageProperties = fileStorageProperties;
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDirectory())
                .toAbsolutePath()
                .normalize();
    }

    public List<File_> listAllFiles() {

        return fileRepository.findAll();
    }

    public List<File_> listFilesByName(String name) {

        return fileRepository.findByName(name);
    }

    @Transactional
    public String addNewFile(MultipartFile multipartFile, FileDto fileDto) throws IOException {

        String originalFileName = getOriginalFileName(multipartFile);

        String baseName = FilenameUtils.getBaseName(originalFileName);
        String extension = FilenameUtils.getExtension(originalFileName);

        boolean nameAlreadyExisting = nameAlreadyExisting(baseName);

        if (nameAlreadyExisting) {

            File_ fileToSave = File_.builder()
                    .name(baseName)
                    .extension(extension)
                    .version(1)
                    .validity(fileDto.validity())
                    .build();

            Path filePathStorage = fileStorageProperties.getFileStorageLocation();
            Files.createDirectories(filePathStorage);
            Path destinationFile = filePathStorage.resolve
                            (Paths.get(fileToSave.getName() + "." + fileToSave.getExtension()))
                    .normalize()
                    .toAbsolutePath();

            multipartFile.transferTo(destinationFile);
            fileRepository.save(fileToSave);

            return fileToSave.getName();
        } else {

            throw new BadRequestException("This file name already exists!");
        }
    }

    @Transactional
    public File_ updateFile(MultipartFile multipartFile, FileDto fileDto) throws IOException {

        String originalFileName = getOriginalFileName(multipartFile);
        String baseName = FilenameUtils.getBaseName(originalFileName);

        Path filePathStorage = fileStorageProperties.getFileStorageLocation();

        List<File_> listFiles = listFilesByName(baseName);

        if (listFiles.isEmpty()) {

            throw new BadRequestException("No files found with the specified name: " + baseName);
        }

        File_ previousFile = listFiles.get(listFiles.size() - 1);

        File_ previousFileRenamed = File_.builder()
                .name(previousFile.getName() + "_v" + previousFile.getVersion())
                .extension(previousFile.getExtension())
                .version(previousFile.getVersion())
                .validity(previousFile.getValidity())
                .build();

        Path fileToRename = Paths.get(filePathStorage + "/" + previousFile.getName() + "." +
                previousFile.getExtension());
        Path modifiedFile = Paths.get(filePathStorage + "/" + previousFileRenamed.getName() + "." +
                previousFileRenamed.getExtension());

        BeanUtils.copyProperties(previousFileRenamed, previousFile, "id");
        fileRepository.save(previousFile);

        try {

            Files.move(fileToRename, modifiedFile);
        } catch(BadRequestException exception) {

            throw new BadRequestException
                    ("Unable to rename the file before this one for version differentiation");
        }

        Files.createDirectories(fileStorageLocation);
        Path destinationFile = fileStorageLocation.resolve(originalFileName).normalize().toAbsolutePath();

        multipartFile.transferTo(destinationFile);

        File_ fileUpdated = File_.builder()
                .name(baseName)
                .extension(previousFile.getExtension())
                .version(previousFile.getVersion() + 1)
                .validity(fileDto.validity() != null ? fileDto.validity() : previousFile.getValidity())
                .build();

        return fileRepository.save(fileUpdated);
    }

    public Boolean nameAlreadyExisting(String name) {

        List<File_> fileListWithThisName = fileRepository.findByName(name);
        return fileListWithThisName.isEmpty();
    }

    public String getOriginalFileName(MultipartFile multipartFile) {

        return StringUtils.cleanPath(
                Objects.requireNonNull(multipartFile.getOriginalFilename())
        );
    }

    @Transactional
    public Resource downloadFile(String fileName) throws MalformedURLException {

        Path filePath = fileStorageLocation.resolve(fileName).normalize();
        return new UrlResource(filePath.toUri());
    }

    @Transactional
    public void deleteFileById(Long id) {

        File_ file = fileRepository.findById(id)
                .orElseThrow(() -> new BadRequestException(
                        ("File not found with the specified ID:" + id)));

        String originalFileName = file.getName() + "." + file.getExtension();

        Path filePath = fileStorageLocation.resolve(originalFileName).normalize().toAbsolutePath();
        try {

            Files.deleteIfExists(filePath);
        } catch(IOException ioexception) {

            throw new BadRequestException("Error deleting file from file system: " + file.getName());
        }

        fileRepository.deleteById(id);
    }

    @Transactional
    public void deleteFileByName(String name) {

        List<File_> listFiles = listFilesByName(name);

        if (listFiles.isEmpty()) {

            throw new BadRequestException("No files found with the specified name: " + name);
        }

        for (File_ file : listFiles) {

            String originalFileName = file.getName() + "." + file.getExtension();
            Path filePath = fileStorageLocation.resolve(originalFileName).normalize().toAbsolutePath();

            try {

                Files.deleteIfExists(filePath);
            } catch(IOException ioexception) {

                throw new BadRequestException("Error deleting file from file system: " + originalFileName);
            }

            fileRepository.deleteById(file.getId());
        }
    }
}
