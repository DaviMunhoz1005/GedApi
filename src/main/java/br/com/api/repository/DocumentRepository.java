package br.com.api.repository;

import br.com.api.entities.Document;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    List<Document> findByGuideName(String guideName);

    @Modifying
    @Transactional
    @Query("DELETE FROM Document f WHERE f.name = :name")
    void deleteByName(@Param("name") String documentName);
}
