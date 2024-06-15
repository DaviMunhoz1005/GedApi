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

        return fileRepository.findByName(name);
    }

    @Transactional
    public File addNewFile(FileDto fileDto) {

        File fileToSave = File.builder()
                .name(fileDto.name())
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

        return fileRepository.save(fileToSave);
    }

    @Transactional
    public File updateFile(FileDto fileDto) {

        if (fileDto.name() == null || fileDto.name().isEmpty()) {
            throw new IllegalArgumentException("The 'name' field cannot be empty");
        }

        List<File> listFiles = listFilesByName(fileDto.name());

        if (listFiles.isEmpty()) {
            throw new IllegalArgumentException("No files found with the specified name: " + fileDto.name());
        }

        File previousFile = listFiles.get(listFiles.size() - 1);

        previousFile.setVersion(previousFile.getVersion() + 1);
        if (fileDto.validity() != null) {
            previousFile.setValidity(fileDto.validity());
        }

        return fileRepository.save(previousFile);
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
