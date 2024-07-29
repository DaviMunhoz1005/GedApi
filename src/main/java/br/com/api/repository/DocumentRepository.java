package br.com.api.repository;

import br.com.api.domain.entities.Documents;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Documents, UUID> {

    List<Documents> findByGuideName(String guideName);
}
