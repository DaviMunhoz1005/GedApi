package br.com.api.repository;

import br.com.api.entities.User;
import br.com.api.entities.UserClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserClientRepository extends JpaRepository<UserClient, UUID> {

    UserClient findByUser(User user);
}
