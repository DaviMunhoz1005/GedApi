package br.com.api.service;

import br.com.api.config.DocumentStorageProperties;

import br.com.api.dto.DocumentRequest;
import br.com.api.dto.DocumentResponse;
import br.com.api.dto.UserResponse;

import br.com.api.entities.Client;
import br.com.api.entities.Document;
import br.com.api.entities.User;

import br.com.api.exception.BadRequestException;

import br.com.api.repository.ClientRepository;
import br.com.api.repository.DocumentRepository;
import br.com.api.repository.UserClientRepository;
import br.com.api.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.apache.commons.io.FilenameUtils;

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
import java.util.*;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentStorageProperties documentStorageProperties;
    private final Path documentStorageLocation;
    private final UserService userService;
    private final UserRepository userRepository;
    private final UserClientRepository userClientRepository;
    private final ClientRepository clientRepository;

    public DocumentService() {

        this.documentRepository = null;
        this.documentStorageProperties = null;
        this.documentStorageLocation = null;
        this.userService = null;
        this.userRepository = null;
        this.userClientRepository = null;
        this.clientRepository = null;
    }

    @Autowired
    public DocumentService(DocumentRepository documentRepository,
                           DocumentStorageProperties documentStorageProperties, UserService userService,
                           UserRepository userRepository, UserClientRepository userClientRepository,
                           ClientRepository clientRepository) {

        this.documentRepository = documentRepository;
        this.documentStorageProperties = documentStorageProperties;
        this.documentStorageLocation = Paths.get(documentStorageProperties.getUploadDirectory())
                .toAbsolutePath()
                .normalize();
        this.userService = userService;
        this.userRepository = userRepository;
        this.userClientRepository = userClientRepository;
        this.clientRepository = clientRepository;
    }

    public List<Document> listAllDocumentsFromUsername(String username) {

        UserResponse response = userService.findUserByUsername(username);
        return clientRepository.findByUuid(response.clientId()).getDocumentList();
    }

    @Transactional
    public DocumentResponse addNewDocument(MultipartFile multipartFile, DocumentRequest request,
                                           String username)
            throws IOException {

        User user = userRepository.findByUsername(username);

        if(Boolean.TRUE.equals(user.getExcluded())) {

            throw new BadRequestException("This user has been deleted");
        }

        Client client = userClientRepository.findByUser(user).getClient();

        String originalDocumentName = getOriginalDocumentName(multipartFile);
        String baseName = FilenameUtils.getBaseName(originalDocumentName);
        String extension = FilenameUtils.getExtension(originalDocumentName);

        String documentRenamed = renameDocumentNameToAddUser(baseName, username);

        boolean nameAlreadyExisting = documentNameAlreadyExists(documentRenamed);

        if (!nameAlreadyExisting) {

            multipartFile.transferTo(takeTheDestinationPath(baseName, extension));

            Document documentToSave = Document.builder()
                    .name(baseName)
                    .guideName(documentRenamed)
                    .extension(extension)
                    .version(1)
                    .validity(request.validity())
                    .originalDocument(null)
                    .creation(LocalDate.now())
                    .updated(null)
                    .exclusion(null)
                    .build();

            documentRepository.save(documentToSave);

            List<Document> documentListUser = user.getListDocumentsCreation();
            documentListUser.add(documentToSave);
            user.setListDocumentsCreation(documentListUser);

            List<Document> documentListClient = client.getDocumentList();
            documentListClient.add(documentToSave);
            client.setDocumentList(documentListClient);

            userRepository.save(user);
            clientRepository.save(client);

            renamePhysicalDocument(documentToSave.getName(), documentToSave.getGuideName(),
                    documentToSave.getExtension());

            return DocumentResponse.builder()
                    .name(documentToSave.getName())
                    .originalDocument(documentToSave.getOriginalDocument())
                    .extension(documentToSave.getExtension())
                    .version(documentToSave.getVersion())
                    .validity(documentToSave.getValidity())
                    .creation(documentToSave.getCreation())
                    .updated(documentToSave.getUpdated())
                    .exclusion(documentToSave.getExclusion())
                    .build();
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

    public Boolean documentNameAlreadyExists(String guideName) {

        List<Document> documentListWithThisName = documentRepository.findByGuideName(guideName);
        return !documentListWithThisName.isEmpty();
    }

    @Transactional
    public DocumentResponse updateDocument(MultipartFile multipartFile, DocumentRequest request, String username)
            throws IOException {

        User user = userRepository.findByUsername(username);

        if(Boolean.TRUE.equals(user.getExcluded())) {

            throw new BadRequestException("This user has been deleted");
        }

        Client client = userClientRepository.findByUser(user).getClient();

        String originalDocumentName = getOriginalDocumentName(multipartFile);
        String baseName = FilenameUtils.getBaseName(originalDocumentName);
        String guideName = renameDocumentNameToAddUser(baseName, username);

        List<Document> listDocumentsByName = listDocumentsByName(guideName, username);

        if (listDocumentsByName.isEmpty()) {

            throw new BadRequestException(exceptionReturnForEmptyList(baseName, username));
        }

        Document documentToUpdate = listDocumentsByName.get(0);

        documentToUpdate.setGuideName(documentToUpdate.getGuideName() + "_V" + documentToUpdate.getVersion());
        documentToUpdate.setUpdated(LocalDate.now());

        documentRepository.save(documentToUpdate);

        Document documentToSave = Document.builder()
                .name(baseName)
                .guideName(guideName)
                .extension(documentToUpdate.getExtension())
                .version(documentToUpdate.getVersion() + 1)
                .validity(request.validity())
                .creation(LocalDate.now())
                .updated(null)
                .exclusion(null)
                .originalDocument(documentToUpdate)
                .build();

        documentRepository.save(documentToSave);

        List<Document> documentListFromUser = user.getListDocumentsCreation();
        List<Document> documentListFromClient = client.getDocumentList();

        documentListFromUser.add(documentToSave);
        documentListFromClient.add(documentToSave);

        userRepository.save(user);
        clientRepository.save(client);

        Files.createDirectories(documentStorageLocation);
        Path destinationFile = documentStorageLocation.resolve(originalDocumentName).normalize().toAbsolutePath();
        multipartFile.transferTo(destinationFile);

        renamePhysicalDocument(documentToSave.getGuideName(), documentToUpdate.getGuideName(),
                documentToSave.getExtension());

        renamePhysicalDocument(documentToSave.getName(), documentToSave.getGuideName(),
                documentToSave.getExtension());

        return DocumentResponse.builder()
                .name(documentToSave.getName())
                .originalDocument(documentToSave.getOriginalDocument())
                .extension(documentToSave.getExtension())
                .version(documentToSave.getVersion())
                .validity(documentToSave.getValidity())
                .creation(documentToSave.getCreation())
                .updated(documentToSave.getUpdated())
                .exclusion(documentToSave.getExclusion())
                .build();
    }

    public String getOriginalDocumentName(MultipartFile multipartFile) {

        return StringUtils.cleanPath(
                Objects.requireNonNull(multipartFile.getOriginalFilename())
        );
    }

    @Transactional
    public void usePreviousVersion(String documentName, String username) {

        User user = userRepository.findByUsername(username);

        if(Boolean.TRUE.equals(user.getExcluded())) {

            throw new BadRequestException("This user has been deleted");
        }

        String guideName = renameDocumentNameToAddUser(documentName, username);
        List<Document> documentListFromUserByName = listDocumentsByName(guideName, username);

        if (documentListFromUserByName.isEmpty()) {

            throw new BadRequestException(exceptionReturnForEmptyList(documentName, username));
        }

        if (documentListFromUserByName.size() == 1) {

            throw new BadRequestException("This is the first version of the document");
        }

        Document documentToExcludeLogically = documentListFromUserByName.get(0);
        Document previousVersionDocument = documentToExcludeLogically.getOriginalDocument();

        String originalDocumentName = documentToExcludeLogically.getGuideName() + "." +
                documentToExcludeLogically.getExtension();
        Path documentPath = documentStorageLocation.resolve(originalDocumentName).normalize().toAbsolutePath();

        documentToExcludeLogically.setExclusion(LocalDate.now());
        documentToExcludeLogically.setGuideName("EXCLUDED_DOCUMENT");

        documentRepository.save(documentToExcludeLogically);

        List<Document> documentListFromUserToExclude = user.getListDocumentsExclusion();
        documentListFromUserToExclude.add(documentToExcludeLogically);
        user.setListDocumentsExclusion(documentListFromUserToExclude);

        userRepository.save(user);

        try {

            Files.deleteIfExists(documentPath);
        } catch (IOException ioexception) {

            throw new BadRequestException("Error deleting document from document system: " + documentToExcludeLogically.getName());
        }

        renamePhysicalDocument(previousVersionDocument.getGuideName(), guideName, documentToExcludeLogically.getExtension());

        previousVersionDocument.setUpdated(null);
        previousVersionDocument.setGuideName(guideName);
        documentRepository.save(previousVersionDocument);
    }

    @Transactional
    public void deleteAllDocumentWithName(String documentName, String username) {

        User user = userRepository.findByUsername(username);

        if(Boolean.TRUE.equals(user.getExcluded())) {

            throw new BadRequestException("This user has been deleted");
        }

        String guideName = renameDocumentNameToAddUser(documentName, username);
        List<Document> documentListByName = listDocumentsByName(guideName, username);

        if (documentListByName.isEmpty()) {

            throw new BadRequestException(exceptionReturnForEmptyList(documentName, username));
        }

        for (Document document : documentListByName) {

            String originalDocumentName = document.getGuideName() + "." + document.getExtension();
            Path documentPath = documentStorageLocation.resolve(originalDocumentName)
                    .normalize()
                    .toAbsolutePath();

            try {

                Files.deleteIfExists(documentPath);
            } catch (IOException ioexception) {

                throw new BadRequestException("Error deleting document from document system: "
                        + originalDocumentName);
            }

            document.setExclusion(LocalDate.now());
            document.setGuideName("EXCLUDED_DOCUMENT");

            documentRepository.save(document);

            List<Document> documentListFromUserToExclude = user.getListDocumentsExclusion();
            documentListFromUserToExclude.add(document);
            user.setListDocumentsExclusion(documentListFromUserToExclude);

            userRepository.save(user);
        }
    }

    public String renameDocumentNameToAddUser(String documentName, String username) {

        return documentName + "-" + username;
    }

    public List<Document> listDocumentsByName(String guideName, String username) {

        UserResponse userResponse = userService.findUserByUsername(username);
        User user = userRepository.findByUsername(userResponse.username());

        if(Boolean.TRUE.equals(user.getExcluded())) {

            throw new BadRequestException("This user has been deleted");
        }

        Client client = userClientRepository.findByUser(user).getClient();
        List<Document> documentListClient = client.getDocumentList();
        List<Document> documentListToReturn = new ArrayList<>();

        for (Document document : documentListClient) {

            if (document.getGuideName().equals(guideName)) {

                documentListToReturn.add(document);
            }
        }

        for (Document document : documentListClient) {

            if (document.getGuideName().contains(guideName + "_V")) {

                documentListToReturn.add(document);
            }
        }

        return documentListToReturn;
    }

    public String exceptionReturnForEmptyList(String documentName, String username) {

        return "No documents were found with the name " + documentName + " linked to the user " + username + ".";
    }

    @Transactional
    public Resource downloadDocument(String documentName) throws MalformedURLException {

        Path documentPath = documentStorageLocation.resolve(documentName).normalize();
        return new UrlResource(documentPath.toUri());
    }

    @Transactional
    public void renamePhysicalDocument(String originalName, String documentRenamed, String extension) {

        Path documentPathStorage = documentStorageProperties.getDocumentStorageLocation();

        Path documentToRename = Paths.get(documentPathStorage + "/" + originalName + "." +
                extension);

        Path modifiedDocument = Paths.get(documentPathStorage + "/" + documentRenamed + "."
                + extension);

        try {

            Files.move(documentToRename, modifiedDocument);
        } catch (IOException exception) {

            throw new BadRequestException
                    ("Unable to rename the document before this one for version differentiation");
        }
    }
}
