package br.com.api.config;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@ConfigurationProperties(prefix = "document")
@Getter
@Setter
public class DocumentStorageProperties {

    private String uploadDirectory;

    public Path getDocumentStorageLocation() {
        return Paths.get(uploadDirectory)
                .toAbsolutePath()
                .normalize();
    }
}
