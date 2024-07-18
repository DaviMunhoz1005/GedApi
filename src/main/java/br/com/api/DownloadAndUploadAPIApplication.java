package br.com.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
		info = @Info(
				title = "Download and Upload Files API",
				version = "1.0",
				description = "This API was created with the aim of storing user files and these users can be " +
						"linked to others so that they have access to these documents, it was implemented in my " +
						"TCC at ETEC Professor Camargo Aranha"
		)
)

@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")

@SpringBootApplication
public class DownloadAndUploadAPIApplication {

	public static void main(String[] args) {
		SpringApplication.run(DownloadAndUploadAPIApplication.class, args);
	}

}
