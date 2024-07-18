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
    public File_ addNewFile(MultipartFile multipartFile, FileDto fileDto, String username) throws IOException {

        String originalFileName = getOriginalFileName(multipartFile);
        String baseName = FilenameUtils.getBaseName(originalFileName);
        String extension = FilenameUtils.getExtension(originalFileName);
        String fileNameRenamed = renameFilenameToAddUserToName(baseName, username);

        boolean nameAlreadyExisting = fileNameAlreadyExists(fileNameRenamed);
        boolean userExists = userExists(username);

        if(!nameAlreadyExisting && userExists) {

            multipartFile.transferTo(takeTheDestinationPath(baseName, extension));

            File_ fileToUpdate = persistingTheFile(baseName, extension, fileDto.validity());
            File_ fileToUpdateRenamed = constructionOfFileToUpdateRenamed(
                    fileNameRenamed, extension, fileToUpdate.getVersion(), fileToUpdate.getValidity()
            );

            renameFile(fileToUpdate, fileToUpdateRenamed, username);
            renamePhysicalFile(fileToUpdate, fileToUpdateRenamed);

            return fileToUpdate;
        } else {

            throw new BadRequestException("This name is already used for another file, choose another name");
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

    public Boolean fileNameAlreadyExists(String fileName) {

        List<File_> fileListWithThisName = fileRepository.findByName(fileName);
        return !fileListWithThisName.isEmpty();
    }

    public Boolean userExists(String username) {

        User user = userService.findUserByUsername(username);
        return user != null;
    }

    @Transactional
    public File_ updateFile(MultipartFile multipartFile, FileDto fileDto, String username) throws IOException {

        String originalFileName = getOriginalFileName(multipartFile);
        String baseName = FilenameUtils.getBaseName(originalFileName);

        String fileNameRenamed = renameFilenameToAddUserToName(baseName, username);

        User user = userService.findUserByUsername(username);
        List<File_> fileListUser = user.getFileList();

        List<File_> listFiles = listFilesByName(fileNameRenamed, username);

        if (listFiles.isEmpty()) {

            throw new BadRequestException(exceptionReturnForEmptyList(baseName, username));
        }

        File_ fileToUpdate = listFiles.get(listFiles.size() - 1);

        if(fileToUpdate.getVersion() == 10) {

            throw new BadRequestException("This file has reached the limit of 10 previous versions");
        }

        renameFile(fileToUpdate, constructionOfFileToUpdateRenamed(fileToUpdate), username);

        Files.createDirectories(fileStorageLocation);
        Path destinationFile = fileStorageLocation.resolve(originalFileName).normalize().toAbsolutePath();
        multipartFile.transferTo(destinationFile);

        File_ updatedFile = persistenceOfTheUpdatedFile(fileNameRenamed, fileToUpdate, fileDto.validity());

        renamePhysicalFile(fileToUpdate, updatedFile, baseName);

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
    public void usePreviousVersion(String fileName, String username) {

        String fileNameRenamed = renameFilenameToAddUserToName(fileName, username);
        List<File_> fileList = listFilesByName(fileNameRenamed, username);

        if(fileList.isEmpty()) {

            throw new BadRequestException(exceptionReturnForEmptyList(fileName, username));
        }

        if(fileList.size() == 1) {

            throw new BadRequestException("This is the first version of the file");
        }

        File_ file = fileList.get(fileList.size() - 1);

        String originalFileName = file.getName() + "." + file.getExtension();
        Path filePath = fileStorageLocation.resolve(originalFileName).normalize().toAbsolutePath();

        try {

            fileRepository.deleteByName(file.getName());
            if (fileRepository.findByName(file.getName()).contains(file)) {

                throw new BadRequestException("Failed to delete database file: " + file.getName());
            }

            Files.deleteIfExists(filePath);
        } catch(IOException ioexception) {

            throw new BadRequestException("Error deleting file from file system: " + file.getName());
        }

        List<File_> newFileList = listFilesByName(fileNameRenamed, username);
        File_ fileToUpdate;

        if (!newFileList.isEmpty()) {

            fileToUpdate = newFileList.get(newFileList.size() - 1);

            renameFile(file, fileToUpdate,
                    constructionOfFileToUpdateRenamed(
                            fileNameRenamed,
                            fileToUpdate.getExtension(),
                            fileToUpdate.getVersion(),
                            fileToUpdate.getValidity()
                    ), username);
        }
    }

    public File_ constructionOfFileToUpdateRenamed(String fileName, String extension,
                                                   Integer version, LocalDate validity) {

        return File_.builder()
                .name(fileName)
                .extension(extension)
                .version(version)
                .validity(validity)
                .build();
    }

    @Transactional
    public void deleteAllFilesWithName(String fileName, String username) {

        String fileNameRenamed = renameFilenameToAddUserToName(fileName, username);
        List<File_> fileList = listFilesByName(fileNameRenamed, username);

        if (fileList.isEmpty()) {

            throw new BadRequestException(exceptionReturnForEmptyList(fileName, username));
        }

        for (File_ file : fileList) {

            String originalFileName = file.getName() + "." + file.getExtension();
            Path filePath = fileStorageLocation.resolve(originalFileName).normalize().toAbsolutePath();

            try {

                Files.deleteIfExists(filePath);
            } catch(IOException ioexception) {

                throw new BadRequestException("Error deleting file from file system: " + originalFileName);
            }

            fileRepository.deleteByName(file.getName());
        }
    }

    public String renameFilenameToAddUserToName(String fileName, String username) {

        return fileName + "-" + username;
    }

    public List<File_> listFilesByName(String fileName, String username) {

        User user = userService.findUserByUsername(username);
        Role role = user.getRoleList().get(0);

        if(role.getRoleName() == RoleName.EMPLOYEE) {

            user = userService.findUserByUsername(employeeRepository
                    .findByUsername(user.getUsername())
                    .getClient()
                    .getUsername());
        }

        List<File_> filesFromUser = user.getFileList();
        List<File_> fileNameWithVersion = new ArrayList<>();

        for(int i = 1; i <= 10; i++) {

            fileNameWithVersion.addAll(fileRepository.findByName(fileName + "_V" + i));
        }

        List<File_> allFilesWithThisNameFromUser = new ArrayList<>(
                listFilesWithVersionInName(fileNameWithVersion, filesFromUser)
        );

        List<File_> fileNameWithoutVersion = fileRepository.findByName(fileName);

        allFilesWithThisNameFromUser.addAll(listFilesWithoutVersionInName(fileNameWithoutVersion, filesFromUser));

        return allFilesWithThisNameFromUser;
    }

    public List<File_> listFilesWithVersionInName(List<File_> fileNameWithVersion,
                                                  List<File_> filesFromUser) {

        List<File_> filesTheUserOwns = new ArrayList<>();

        if(!fileNameWithVersion.isEmpty()) {

            for (File_ fileVersion : fileNameWithVersion) {

                for (File_ file : filesFromUser) {

                    if (fileVersion == file) {

                        filesTheUserOwns.add(fileVersion);
                    }
                }
            }
        }

        return filesTheUserOwns;
    }

    public List<File_> listFilesWithoutVersionInName(List<File_> fileNameWithoutVersion,
                                                     List<File_> filesFromUser) {

        List<File_> filesTheUserOwns = new ArrayList<>();

        if(!fileNameWithoutVersion.isEmpty()) {

            for(File_ fileUser : filesFromUser) {

                for(File_ fileName : fileNameWithoutVersion) {

                    if(fileUser == fileName) {

                        filesTheUserOwns.add(fileName);
                    }
                }
            }
        }

        return filesTheUserOwns;
    }

    public String exceptionReturnForEmptyList(String fileName, String username) {

        return "No files were found with the name " + fileName + " linked to the user " + username + ".";
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

    @Transactional
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
    public void renamePhysicalFile(File_ fileToUpdate, File_ fileToUpdateRenamed, String baseName) {

        Path filePathStorage = fileStorageProperties.getFileStorageLocation();

        Path fileToRename = Paths.get(filePathStorage + "/" + baseName + "." +
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
