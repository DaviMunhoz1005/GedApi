package br.com.api.repository;

import br.com.api.domain.entities.Clients;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Clients, UUID> {

    Clients findByUuid(UUID uuid);
    Clients findByCnpjCpf(String cnpjCpf);
}
