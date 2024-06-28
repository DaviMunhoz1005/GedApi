package br.com.dowloadAndUploadFiles.repository;

import br.com.dowloadAndUploadFiles.entities.File_;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File_, Long> {

    List<File_> findByName(String name);
}
