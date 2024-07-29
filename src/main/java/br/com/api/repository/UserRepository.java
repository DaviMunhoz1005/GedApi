package br.com.api.repository;

import br.com.api.domain.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<Users, UUID> {

    Users findByUsername(String username);
}
