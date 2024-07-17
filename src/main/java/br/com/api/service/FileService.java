package br.com.api.service;

import br.com.api.config.FileStorageProperties;
import br.com.api.dto.FileDto;
import br.com.api.entities.File_;
import br.com.api.entities.Role;
import br.com.api.entities.User;
import br.com.api.entities.enums.RoleName;
import br.com.api.exception.BadRequestException;
import br.com.api.repository.EmployeeRepository;
import br.com.api.repository.FileRepository;

import br.com.api.repository.UserRepository;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final FileStorageProperties fileStorageProperties;
    private final Path fileStorageLocation;
    private final UserRepository userRepository;
    private final UserService userService;
    private final EmployeeRepository employeeRepository;

    public FileService() {

        this.fileRepository = null;
        this.fileStorageProperties = null;
        this.fileStorageLocation = null;
        this.userRepository = null;
        this.userService = null;
        this.employeeRepository = null;
    }

    @Autowired
    public FileService(FileRepository fileRepository, FileStorageProperties fileStorageProperties,
                       UserRepository userRepository, UserService userService,
                       EmployeeRepository employeeRepository) {

        this.fileRepository = fileRepository;
        this.fileStorageProperties = fileStorageProperties;
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDirectory())
                .toAbsolutePath()
                .normalize();
        this.userRepository = userRepository;
        this.userService = userService;
        this.employeeRepository = employeeRepository;
    }

    public List<File_> listAllFilesFromUsername(String username) {

        User user = userService.findUserByUsername(username);
        Role role = user.getRoleList().get(0);

        if(role.getRoleName() == RoleName.CLIENT) {

            return user.getFileList();
        } else {

            return employeeRepository.findByUsername(username).getClient().getFileList();
        }
    }

    @Transactional
    public File_ addNewFile(MultipartFile multipartFile, FileDto fileDto) throws IOException {

        User user = userService.findUserByUsername(fileDto.username());

        String originalFileName = getOriginalFileName(multipartFile);
        String baseName = FilenameUtils.getBaseName(originalFileName);
        String extension = FilenameUtils.getExtension(originalFileName);

        boolean nameAlreadyExisting = nameAlreadyExisting(baseName);

        if (!nameAlreadyExisting) {

            multipartFile.transferTo(takeTheDestinationPath(baseName, extension));
            File_ fileToSave = persistingTheFile(baseName, extension, fileDto.validity());

            List<File_> userFileList = user.getFileList();
            userFileList.add(fileToSave);

            user.setFileList(userFileList);
            userRepository.save(user);

            return fileToSave;
        } else {

            throw new BadRequestException("This file name already exists!");
        }
    }

    public Path takeTheDestinationPath(String baseName, String extension) throws IOException {

        Path filePathStorage = fileStorageProperties.getFileStorageLocation();
        Files.createDirectories(filePathStorage);

        return filePathStorage.resolve
                        (Paths.get(baseName + "." + extension))
                .normalize()
                .toAbsolutePath();
    }

    public File_ persistingTheFile(String baseName, String extension, LocalDate validity) {

        File_ fileToSave = File_.builder()
                .name(baseName)
                .extension(extension)
                .version(1)
                .validity(validity)
                .build();

        return fileRepository.save(fileToSave);
    }

    public Boolean nameAlreadyExisting(String name) {

        List<File_> fileListWithThisName = fileRepository.findByName(name);
        return !fileListWithThisName.isEmpty();
    }

    @Transactional
    public File_ updateFile(MultipartFile multipartFile, FileDto fileDto) throws IOException {

        String originalFileName = getOriginalFileName(multipartFile);
        String baseName = FilenameUtils.getBaseName(originalFileName);

        User user = userService.findUserByUsername(fileDto.username());
        List<File_> fileListUser = user.getFileList();

        List<File_> listFiles = listFilesByName(baseName, fileDto.username());

        if (listFiles.isEmpty()) {

            throw new BadRequestException("No files found with the specified name: " + baseName);
        }

        File_ fileToUpdate = listFiles.get(listFiles.size() - 1);

        if(fileToUpdate.getVersion() == 10) {

            throw new BadRequestException("This file has reached the limit of 10 previous versions");
        }

        renameFile(fileToUpdate, constructionOfFileToUpdateRenamed(fileToUpdate), fileDto.username());

        Files.createDirectories(fileStorageLocation);
        Path destinationFile = fileStorageLocation.resolve(originalFileName).normalize().toAbsolutePath();
        multipartFile.transferTo(destinationFile);

        File_ updatedFile = persistenceOfTheUpdatedFile(baseName, fileToUpdate, fileDto.validity());

        fileListUser.add(updatedFile);
        user.setFileList(fileListUser);
        userRepository.save(user);

        return updatedFile;
    }

    public File_ constructionOfFileToUpdateRenamed(File_ fileToUpdate) {

        return File_.builder()
                .name(fileToUpdate.getName() + "_v" + fileToUpdate.getVersion())
                .extension(fileToUpdate.getExtension())
                .version(fileToUpdate.getVersion())
                .validity(fileToUpdate.getValidity())
                .build();
    }

    public File_ persistenceOfTheUpdatedFile(String baseName, File_ fileToUpdate,
                                             LocalDate validity) {

         File_ fileUpdated = File_.builder()
                .name(baseName)
                .extension(fileToUpdate.getExtension())
                .version(fileToUpdate.getVersion() + 1)
                .validity(validity != null ? validity : fileToUpdate.getValidity())
                .build();

        return fileRepository.save(fileUpdated);
    }

    public String getOriginalFileName(MultipartFile multipartFile) {

        return StringUtils.cleanPath(
                Objects.requireNonNull(multipartFile.getOriginalFilename())
        );
    }

    @Transactional
    public void usePreviousVersion(String filename, String username) {

        List<File_> fileList = listFilesByName(filename, username);

        if(fileList.size() == 1) {

            throw new BadRequestException("This is the first version of the file");
        }

        File_ file = fileList.get(fileList.size() - 1);

        String originalFileName = file.getName() + "." + file.getExtension();
        Path filePath = fileStorageLocation.resolve(originalFileName).normalize().toAbsolutePath();

        try {

            Files.deleteIfExists(filePath);
        } catch(IOException ioexception) {

            throw new BadRequestException("Error deleting file from file system: " + file.getName());
        }

        fileRepository.delete(file);

        List<File_> newFileList = listFilesByName(filename, username);
        File_ fileToUpdate;

        if (!newFileList.isEmpty()) {

            fileToUpdate = fileList.get(newFileList.size() - 1);

            renameFile(file, fileToUpdate,
                    constructionOfPreviousFileRenamed(
                            filename,
                            fileToUpdate.getExtension(),
                            fileToUpdate.getVersion(),
                            fileToUpdate.getValidity()
                    ),
                    username);
        }
    }

    public File_ constructionOfPreviousFileRenamed(String filename, String extension,
                                                   Integer version, LocalDate validity) {

        return File_.builder()
                .name(filename)
                .extension(extension)
                .version(version)
                .validity(validity)
                .build();
    }

    @Transactional
    public void deleteAllFilesWithName(String name, String username) {

        List<File_> fileList = listFilesByName(name, username);

        if (fileList.isEmpty()) {

            throw new BadRequestException("No files found with the specified name: " + name);
        }

        for (File_ file : fileList) {

            String originalFileName = file.getName() + "." + file.getExtension();
            Path filePath = fileStorageLocation.resolve(originalFileName).normalize().toAbsolutePath();

            try {

                Files.deleteIfExists(filePath);
            } catch(IOException ioexception) {

                throw new BadRequestException("Error deleting file from file system: " + originalFileName);
            }

            fileRepository.delete(file);
        }
    }

    public List<File_> listFilesByName(String name, String username) {

        User user = userService.findUserByUsername(username);
        List<File_> filesFromUser = user.getFileList();
        List<File_> filenameWithVersion = new ArrayList<>();

        for(int i = 1; i <= 10; i++) {

            filenameWithVersion.addAll(fileRepository.findByName(name + "_V" + i));
        }

        List<File_> allFilesWithThisNameFromUser = new ArrayList<>(
                listFilesWithVersionInName(filenameWithVersion, filesFromUser)
        );

        List<File_> filenameWithoutVersion = fileRepository.findByName(name);

        allFilesWithThisNameFromUser.addAll(listFilesWithoutVersionInName(filenameWithoutVersion, filesFromUser));

        return allFilesWithThisNameFromUser;
    }

    public List<File_> listFilesWithVersionInName(List<File_> filenameWithVersion,
                                                  List<File_> filesFromUser) {

        List<File_> filesTheUserOwns = new ArrayList<>();

        if(!filenameWithVersion.isEmpty()) {

            for (File_ fileVersion : filenameWithVersion) {

                for (File_ file : filesFromUser) {

                    if (fileVersion == file) {

                        filesTheUserOwns.add(fileVersion);
                    }
                }
            }
        }

        return filesTheUserOwns;
    }

    public List<File_> listFilesWithoutVersionInName(List<File_> filenameWithoutVersion,
                                                     List<File_> filesFromUser) {

        List<File_> filesTheUserOwns = new ArrayList<>();

        if(!filenameWithoutVersion.isEmpty()) {

            for(File_ fileUser : filesFromUser) {

                for(File_ fileName : filenameWithoutVersion) {

                    if(fileUser == fileName) {

                        filesTheUserOwns.add(fileName);
                    }
                }
            }
        }

        return filesTheUserOwns;
    }

    @Transactional
    public void renameFile(File_ fileToUpdate, File_ fileToUpdateRenamed, String username) {

        User user = userService.findUserByUsername(username);
        List<File_> userFileList = user.getFileList();

        renamePhysicalFile(fileToUpdate, fileToUpdateRenamed);

        userFileList.remove(fileToUpdate);

        BeanUtils.copyProperties(fileToUpdateRenamed, fileToUpdate, "uuid");

        fileRepository.save(fileToUpdate);
        userFileList.add(fileToUpdate);
        user.setFileList(userFileList);
        userRepository.save(user);
    }

    @Transactional
    public void renameFile(File_ file, File_ fileToUpdate, File_ fileToUpdateRenamed,
                           String username) {

        User user = userService.findUserByUsername(username);
        List<File_> userFileList = user.getFileList();

        renamePhysicalFile(fileToUpdate, fileToUpdateRenamed);

        userFileList.remove(fileToUpdate);
        userFileList.remove(file);

        BeanUtils.copyProperties(fileToUpdateRenamed, fileToUpdate, "uuid");

        fileRepository.save(fileToUpdate);
        userFileList.add(fileToUpdate);
        user.setFileList(userFileList);
        userRepository.save(user);
    }

    public void renamePhysicalFile(File_ fileToUpdate, File_ fileToUpdateRenamed) {

        Path filePathStorage = fileStorageProperties.getFileStorageLocation();

        Path fileToRename = Paths.get(filePathStorage + "/" + fileToUpdate.getName() + "." +
                fileToUpdate.getExtension());

        Path modifiedFile = Paths.get(filePathStorage + "/" + fileToUpdateRenamed.getName() + "." +
                fileToUpdateRenamed.getExtension());

        try {

            Files.move(fileToRename, modifiedFile);
        } catch(IOException exception) {

            throw new BadRequestException
                    ("Unable to rename the file before this one for version differentiation");
        }
    }

    @Transactional
    public Resource downloadFile(String fileName) throws MalformedURLException {

        Path filePath = fileStorageLocation.resolve(fileName).normalize();
        return new UrlResource(filePath.toUri());
    }
}
