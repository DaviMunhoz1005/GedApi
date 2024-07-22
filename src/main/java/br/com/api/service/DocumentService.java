package br.com.api.service;

import br.com.api.config.DocumentStorageProperties;
import br.com.api.dto.FileDto;
import br.com.api.dto.UserResponse;
import br.com.api.entities.Document;
import br.com.api.entities.Role;
import br.com.api.entities.enums.RoleName;
import br.com.api.exception.BadRequestException;
import br.com.api.repository.DocumentRepository;

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
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentStorageProperties documentStorageProperties;
    private final Path documentStorageLocation;
    private final UserService userService;

    public DocumentService() {

        this.documentRepository = null;
        this.documentStorageProperties = null;
        this.documentStorageLocation = null;
        this.userService = null;
    }

    @Autowired
    public DocumentService(DocumentRepository documentRepository,
                           DocumentStorageProperties documentStorageProperties, UserService userService) {

        this.documentRepository = documentRepository;
        this.documentStorageProperties = documentStorageProperties;
        this.documentStorageLocation = Paths.get(documentStorageProperties.getUploadDirectory())
                .toAbsolutePath()
                .normalize();
        this.userService = userService;
    }

    public List<Document> listAllDocumentsFromUsername(String username) {

        UserResponse user = userService.findUserByUsername(username);
        Role role = user.role();

        if(role.getRoleName() == RoleName.CLIENT) {

            return null;
        } else {

            return null;
        }
    }

    @Transactional
    public Document addNewDocument(MultipartFile multipartFile, FileDto fileDto, String username)
            throws IOException {

        String originalDocumentName = getOriginalDocumentName(multipartFile);
        String baseName = FilenameUtils.getBaseName(originalDocumentName);
        String extension = FilenameUtils.getExtension(originalDocumentName);
        String documentNameRenamed = renameDocumentNameToAddUserToName(baseName, username);

        boolean nameAlreadyExisting = documentNameAlreadyExists(documentNameRenamed);
        boolean userExists = userExists(username);

        if(!nameAlreadyExisting && userExists) {

            multipartFile.transferTo(takeTheDestinationPath(baseName, extension));

            Document documentToUpdate = persistingTheDocument(baseName, extension, fileDto.validity());
            Document documentToUpdateRenamed = constructionOfDocumentToUpdateRenamed(
                    documentNameRenamed, extension, documentToUpdate.getVersion(), documentToUpdate.getValidity()
            );

            renameDocument(documentToUpdate, documentToUpdateRenamed, username);
            renamePhysicalDocument(documentToUpdate, documentToUpdateRenamed);

            return documentToUpdate;
        } else {

            throw new BadRequestException("This name is already used for another document, choose another name");
        }
    }

    public Path takeTheDestinationPath(String baseName, String extension) throws IOException {

        Path documentPathStorage = documentStorageProperties.getDocumentStorageLocation();
        Files.createDirectories(documentPathStorage);

        return documentPathStorage.resolve
                        (Paths.get(baseName + "." + extension))
                .normalize()
                .toAbsolutePath();
    }

    public Document persistingTheDocument(String baseName, String extension, LocalDate validity) {

        Document documentToSave = Document.builder()
                .name(baseName)
                .extension(extension)
                .version(1)
                .validity(validity)
                .build();

        return documentRepository.save(documentToSave);
    }

    public Boolean documentNameAlreadyExists(String documentName) {

        List<Document> documentListWithThisName = documentRepository.findByName(documentName);
        return !documentListWithThisName.isEmpty();
    }

    public Boolean userExists(String username) {

        UserResponse user = userService.findUserByUsername(username);
        return user != null;
    }

    @Transactional
    public Document updateDocument(MultipartFile multipartFile, FileDto fileDto, String username)
            throws IOException {

        String originalDocumentName = getOriginalDocumentName(multipartFile);
        String baseName = FilenameUtils.getBaseName(originalDocumentName);

        String documentNameRenamed = renameDocumentNameToAddUserToName(baseName, username);

        UserResponse user = userService.findUserByUsername(username);
        List<Document> documentListUser = null;

        List<Document> listDocuments = listDocumentsByName(documentNameRenamed, username);

        if (listDocuments.isEmpty()) {

            throw new BadRequestException(exceptionReturnForEmptyList(baseName, username));
        }

        Document documentToUpdate = listDocuments.get(listDocuments.size() - 1);

        if(documentToUpdate.getVersion() == 10) {

            throw new BadRequestException("This document has reached the limit of 10 previous versions");
        }

        renameDocument(documentToUpdate, constructionOfDocumentToUpdateRenamed(documentToUpdate), username);

        Files.createDirectories(documentStorageLocation);
        Path destinationFile = documentStorageLocation.resolve(originalDocumentName).normalize().toAbsolutePath();
        multipartFile.transferTo(destinationFile);

        Document updatedDocument = persistenceOfTheUpdatedDocument(documentNameRenamed, documentToUpdate, fileDto.validity());

        renamePhysicalDocument(documentToUpdate, updatedDocument, baseName);

        documentListUser.add(updatedDocument);
//        user.setFileList(fileListUser);
//        userRepository.save(user);

        return updatedDocument;
    }

    public Document constructionOfDocumentToUpdateRenamed(Document documentToUpdate) {

        return Document.builder()
                .name(documentToUpdate.getName() + "_v" + documentToUpdate.getVersion())
                .extension(documentToUpdate.getExtension())
                .version(documentToUpdate.getVersion())
                .validity(documentToUpdate.getValidity())
                .build();
    }

    public Document persistenceOfTheUpdatedDocument(String baseName, Document documentToUpdate,
                                                    LocalDate validity) {

         Document documentUpdated = Document.builder()
                .name(baseName)
                .extension(documentToUpdate.getExtension())
                .version(documentToUpdate.getVersion() + 1)
                .validity(validity != null ? validity : documentToUpdate.getValidity())
                .build();

        return documentRepository.save(documentUpdated);
    }

    public String getOriginalDocumentName(MultipartFile multipartFile) {

        return StringUtils.cleanPath(
                Objects.requireNonNull(multipartFile.getOriginalFilename())
        );
    }

    @Transactional
    public void usePreviousVersion(String documentName, String username) {

        String documentNameRenamed = renameDocumentNameToAddUserToName(documentName, username);
        List<Document> documentList = listDocumentsByName(documentNameRenamed, username);

        if(documentList.isEmpty()) {

            throw new BadRequestException(exceptionReturnForEmptyList(documentName, username));
        }

        if(documentList.size() == 1) {

            throw new BadRequestException("This is the first version of the document");
        }

        Document document = documentList.get(documentList.size() - 1);

        String originalDocumentName = document.getName() + "." + document.getExtension();
        Path documentPath = documentStorageLocation.resolve(originalDocumentName).normalize().toAbsolutePath();

        try {

            documentRepository.deleteByName(document.getName());
            if (documentRepository.findByName(document.getName()).contains(document)) {

                throw new BadRequestException("Failed to delete database document: " + document.getName());
            }

            Files.deleteIfExists(documentPath);
        } catch(IOException ioexception) {

            throw new BadRequestException("Error deleting document from document system: " + document.getName());
        }

        List<Document> newDocumentList = listDocumentsByName(documentNameRenamed, username);
        Document documentToUpdate;

        if (!newDocumentList.isEmpty()) {

            documentToUpdate = newDocumentList.get(newDocumentList.size() - 1);

            renameDocument(document, documentToUpdate,
                    constructionOfDocumentToUpdateRenamed(
                            documentNameRenamed,
                            documentToUpdate.getExtension(),
                            documentToUpdate.getVersion(),
                            documentToUpdate.getValidity()
                    ), username);
        }
    }

    public Document constructionOfDocumentToUpdateRenamed(String documentName, String extension,
                                                          Integer version, LocalDate validity) {

        return Document.builder()
                .name(documentName)
                .extension(extension)
                .version(version)
                .validity(validity)
                .build();
    }

    @Transactional
    public void deleteAllDocumentWithName(String documentName, String username) {

        String documentNameRenamed = renameDocumentNameToAddUserToName(documentName, username);
        List<Document> documentList = listDocumentsByName(documentNameRenamed, username);

        if (documentList.isEmpty()) {

            throw new BadRequestException(exceptionReturnForEmptyList(documentName, username));
        }

        for (Document document : documentList) {

            String originalDocumentName = document.getName() + "." + document.getExtension();
            Path documentPath = documentStorageLocation.resolve(originalDocumentName)
                    .normalize()
                    .toAbsolutePath();

            try {

                Files.deleteIfExists(documentPath);
            } catch(IOException ioexception) {

                throw new BadRequestException("Error deleting document from document system: "
                        + originalDocumentName);
            }

            documentRepository.deleteByName(document.getName());
        }
    }

    public String renameDocumentNameToAddUserToName(String documentName, String username) {

        return documentName + "-" + username;
    }

    public List<Document> listDocumentsByName(String documentName, String username) {

        UserResponse user = userService.findUserByUsername(username);
        Role role = user.role();

        if(role.getRoleName() == RoleName.EMPLOYEE) {

            user = null;
        }

        List<Document> documentFromUser = null;
        List<Document> documentNameWithVersion = new ArrayList<>();

        for(int i = 1; i <= 10; i++) {

            documentNameWithVersion.addAll(documentRepository.findByName(documentName + "_V" + i));
        }

        List<Document> allDocumentWithThisNameFromUser = new ArrayList<>(
                listDocumentsWithVersionInName(documentNameWithVersion, documentFromUser)
        );

        List<Document> documentNameWithoutVersion = documentRepository.findByName(documentName);

        allDocumentWithThisNameFromUser.addAll(listDocumentsWithoutVersionInName(documentNameWithoutVersion,
                documentFromUser));

        return allDocumentWithThisNameFromUser;
    }

    public List<Document> listDocumentsWithVersionInName(List<Document> documentNameWithVersion,
                                                         List<Document> documentsFromUser) {

        List<Document> documentsTheUserOwns = new ArrayList<>();

        if(!documentNameWithVersion.isEmpty()) {

            for (Document documentVersion : documentNameWithVersion) {

                for (Document document : documentsFromUser) {

                    if (documentVersion == document) {

                        documentsTheUserOwns.add(documentVersion);
                    }
                }
            }
        }

        return documentsTheUserOwns;
    }

    public List<Document> listDocumentsWithoutVersionInName(List<Document> documentNameWithoutVersion,
                                                            List<Document> documentsFromUser) {

        List<Document> documentsTheUserOwns = new ArrayList<>();

        if(!documentNameWithoutVersion.isEmpty()) {

            for(Document documentUser : documentsFromUser) {

                for(Document document : documentNameWithoutVersion) {

                    if(documentUser == document) {

                        documentsTheUserOwns.add(document);
                    }
                }
            }
        }

        return documentsTheUserOwns;
    }

    public String exceptionReturnForEmptyList(String documentName, String username) {

        return "No documents were found with the name " + documentName + " linked to the user " + username + ".";
    }

    @Transactional
    public void renameDocument(Document documentToUpdate, Document documentToUpdateRenamed, String username) {

        UserResponse user = userService.findUserByUsername(username);
        List<Document> userDocumentList = null;

        renamePhysicalDocument(documentToUpdate, documentToUpdateRenamed);

        userDocumentList.remove(documentToUpdate);

        BeanUtils.copyProperties(documentToUpdateRenamed, documentToUpdate, "uuid");

        documentRepository.save(documentToUpdate);
        userDocumentList.add(documentToUpdate);
//        user.setFileList(userDocumentList);
//        userRepository.save(user);
    }

    @Transactional
    public void renameDocument(Document document, Document documentToUpdate, Document documentToUpdateRenamed,
                               String username) {

        UserResponse user = userService.findUserByUsername(username);
        List<Document> userDocumentList = null;

        renamePhysicalDocument(documentToUpdate, documentToUpdateRenamed);

        userDocumentList.remove(documentToUpdate);
        userDocumentList.remove(document);

        BeanUtils.copyProperties(documentToUpdateRenamed, documentToUpdate, "uuid");

        documentRepository.save(documentToUpdate);
        userDocumentList.add(documentToUpdate);
//        user.setFileList(userDocumentList);
//        userRepository.save(user);
    }

    @Transactional
    public void renamePhysicalDocument(Document documentToUpdate, Document documentToUpdateRenamed) {

        Path documentPathStorage = documentStorageProperties.getDocumentStorageLocation();

        Path documentToRename = Paths.get(documentPathStorage + "/" + documentToUpdate.getName() + "." +
                documentToUpdate.getExtension());

        Path modifiedDocument = Paths.get(documentPathStorage + "/" + documentToUpdateRenamed.getName() + "."
                + documentToUpdateRenamed.getExtension());

        try {

            Files.move(documentToRename, modifiedDocument);
        } catch(IOException exception) {

            throw new BadRequestException
                    ("Unable to rename the document before this one for version differentiation");
        }
    }

    @Transactional
    public void renamePhysicalDocument(Document documentToUpdate, Document documentToUpdateRenamed,
                                       String baseName) {

        Path documentPathStorage = documentStorageProperties.getDocumentStorageLocation();

        Path documentToRename = Paths.get(documentPathStorage + "/" + baseName + "." +
                documentToUpdate.getExtension());

        Path modifiedDocument = Paths.get(documentPathStorage + "/" + documentToUpdateRenamed.getName() + "."
                + documentToUpdateRenamed.getExtension());

        try {

            Files.move(documentToRename, modifiedDocument);
        } catch(IOException exception) {

            throw new BadRequestException
                    ("Unable to rename the document before this one for version differentiation");
        }
    }

    @Transactional
    public Resource downloadDocument(String documentName) throws MalformedURLException {

        Path documentPath = documentStorageLocation.resolve(documentName).normalize();
        return new UrlResource(documentPath.toUri());
    }
}
