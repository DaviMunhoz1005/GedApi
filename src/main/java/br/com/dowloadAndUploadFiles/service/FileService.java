package br.com.dowloadAndUploadFiles.service;

import br.com.dowloadAndUploadFiles.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
}
