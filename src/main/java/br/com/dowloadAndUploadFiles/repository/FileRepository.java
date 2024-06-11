package br.com.dowloadAndUploadFiles.repository;

import br.com.dowloadAndUploadFiles.entities.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
}
