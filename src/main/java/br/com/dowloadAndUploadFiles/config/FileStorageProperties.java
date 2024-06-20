package br.com.dowloadAndUploadFiles.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@ConfigurationProperties(prefix = "file")
@Getter @Setter
public class FileStorageProperties {

    private String uploadDirectory;

    public Path getFileStorageLocation() {
        return Paths.get(uploadDirectory)
                .toAbsolutePath()
                .normalize();
    }
}
