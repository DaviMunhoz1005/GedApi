package br.com.dowloadAndUploadFiles.service;

import br.com.dowloadAndUploadFiles.config.FileStorageProperties;
import br.com.dowloadAndUploadFiles.dto.FileDto;
import br.com.dowloadAndUploadFiles.entities.File;
import br.com.dowloadAndUploadFiles.repository.FileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final Path fileStorageLocation;

    public FileService(FileRepository fileRepository, FileStorageProperties fileStorageProperties) {
        this.fileRepository = fileRepository;
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDirectory())
                .toAbsolutePath()
                .normalize();
    }

    public List<File> listAllFiles() {

        return fileRepository.findAll();
    }

    public List<File> listFilesByName(String name) {

        return fileRepository.findByName(name);
    }

    @Transactional
    public void addNewFile(MultipartFile multipartFile, FileDto fileDto) throws IOException {

        String fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));

        File fileToSave = File.builder()
                .name(fileName)
                .version(1)
                .validity(fileDto.validity())
                .build();

        List<File> fileListWithThisName = listFilesByName(fileToSave.getName());

        if(!fileListWithThisName.isEmpty()) {

            int indexToAddToTheNameToDifferentiateIt = 1;

            do {

                String numberToAdd = String.valueOf(indexToAddToTheNameToDifferentiateIt);
                String newNameWithIndexedNumber =  fileToSave.getName() + "_" + numberToAdd;

                fileToSave.setName(newNameWithIndexedNumber);
                fileListWithThisName = listFilesByName(fileToSave.getName());

                indexToAddToTheNameToDifferentiateIt++;
            } while(!fileListWithThisName.isEmpty());
        }

        Files.createDirectories(fileStorageLocation);
        Path destinationFile = fileStorageLocation.resolve(Paths.get(fileToSave.getName()))
                .normalize()
                .toAbsolutePath();

        multipartFile.transferTo(destinationFile);

        fileRepository.save(fileToSave);
    }

    @Transactional
    public File updateFile(MultipartFile multipartFile, FileDto fileDto) throws IOException {

        String fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));

        if (fileName.isEmpty()) {
            throw new IllegalArgumentException("The 'name' field cannot be empty");
        }

        List<File> listFiles = listFilesByName(fileName);

        if (listFiles.isEmpty()) {
            throw new IllegalArgumentException("No files found with the specified name: " + fileName);
        }

        File previousFile = listFiles.get(listFiles.size() - 1);

        previousFile.setVersion(previousFile.getVersion() + 1);
        if (fileDto.validity() != null) {
            previousFile.setValidity(fileDto.validity());
        }

        Files.createDirectories(fileStorageLocation);
        Path destinationFile = fileStorageLocation.resolve(fileName).normalize().toAbsolutePath();

        multipartFile.transferTo(destinationFile);

        previousFile.setName(fileName);

        return fileRepository.save(previousFile);
    }

    @Transactional
    public void deleteFileById(Long id) {

        File file = fileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException
                        (("File not found with the specified ID:" + id)));

        Path filePath = fileStorageLocation.resolve(file.getName()).normalize().toAbsolutePath();
        try {

            Files.deleteIfExists(filePath);
        }catch(IOException ioexception) {

            throw new RuntimeException
                    ("Error deleting file from file system: " + file.getName(), ioexception);
        }

        fileRepository.deleteById(id);
    }

    @Transactional
    public void deleteFileByName(String name) {

        List<File> listFiles = listFilesByName(name);

        if (listFiles.isEmpty()) {
            throw new IllegalArgumentException("No files found with the specified name: " + name);
        }

        for (File file : listFiles) {

            Path filePath = fileStorageLocation.resolve(file.getName()).normalize().toAbsolutePath();

            try {

                Files.deleteIfExists(filePath);
            }catch(IOException ioexception) {

                throw new RuntimeException
                        ("Error deleting file from file system: " + file.getName(), ioexception);
            }

            fileRepository.deleteById(file.getId());
        }
    }
}
