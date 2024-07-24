package br.com.api.repository;

import br.com.api.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    Client findByUuid(UUID uuid);
    Client findByCnpjCpf(String cnpjCpf);
}
