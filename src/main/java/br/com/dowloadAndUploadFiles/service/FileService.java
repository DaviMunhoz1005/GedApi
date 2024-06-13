package br.com.dowloadAndUploadFiles.service;

import br.com.dowloadAndUploadFiles.dto.FileDto;
import br.com.dowloadAndUploadFiles.entities.File;
import br.com.dowloadAndUploadFiles.repository.FileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;

    public List<File> listAllFiles() {

        return fileRepository.findAll();
    }

    public List<File> listFilesByName(String name) {

        return fileRepository.listByName(name);
    }

    @Transactional
    public File addNewFile(FileDto fileDto) {

        File fileToSave = File.builder()
                .name(fileDto.name())
                .version(1)
                .validity(fileDto.validity())
                .build();

        String nameNewFile = fileToSave.getName();

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

        return fileRepository.save(fileToSave);
    }

    @Transactional
    public File updateFile(FileDto fileDto) {

        List<File> listFiles = listFilesByName(fileDto.name());
        int indexNumberPreviousFile = listFiles.size();
        File previusFile = listFiles.get(indexNumberPreviousFile - 1);

        File currentDocument = File.builder()
                .name(fileDto.name())
                .version(previusFile.getVersion() + 1)
                .validity(fileDto.validity() != null ? fileDto.validity() : previusFile.getValidity())
                .build();

        return fileRepository.save(currentDocument);
    }

    @Transactional
    public void deleteFileById(Long id) {

        fileRepository.deleteById(id);
    }

    @Transactional
    public void deleteFileByName(String name) {

        fileRepository.deleteByName(name);
    }
}
