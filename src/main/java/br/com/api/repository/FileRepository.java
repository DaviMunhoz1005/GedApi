package br.com.api.repository;

import br.com.api.entities.File_;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<File_, UUID> {

    List<File_> findByName(String filename);

    @Modifying
    @Transactional
    @Query("DELETE FROM File_ f WHERE f.name = :name")
    void deleteByName(@Param("name") String name);
}
