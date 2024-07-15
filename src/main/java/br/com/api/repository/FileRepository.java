package br.com.api.repository;

import br.com.api.entities.File_;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<File_, Long> {

    List<File_> findByName(String filename);
}
