package io.quickledger.repositories;

import io.quickledger.entities.Client;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
        // public Page<Client> findAllBy(Pageable pageable);

        // TODO consider some indexing on these fields later
        @Query("SELECT c FROM Client c WHERE c.company.id = ?1 AND " +
                        "(LOWER(c.phone) LIKE LOWER(CONCAT('%', ?2, '%')) OR " +
                        "LOWER(c.email) LIKE LOWER(CONCAT('%', ?3, '%')) OR " +
                        "LOWER(c.entityName) LIKE LOWER(CONCAT('%', ?4, '%')) OR " +
                        "LOWER(c.contactName) LIKE LOWER(CONCAT('%', ?5, '%')) OR " +
                        "LOWER(c.contactSurname) LIKE LOWER(CONCAT('%', ?6, '%')))")
        List<Client> search(Long companyId, String phone, String email, String entityName, String contactName,
                        String contactSurname, Pageable pageable);

        // single search term for all fields
        @Query("SELECT c FROM Client c WHERE c.company.id = ?1 AND " +
                        "(LOWER(c.phone) LIKE LOWER(CONCAT('%', ?2, '%')) OR " +
                        "LOWER(c.email) LIKE LOWER(CONCAT('%', ?2, '%')) OR " +
                        "LOWER(c.entityName) LIKE LOWER(CONCAT('%', ?2, '%')) OR " +
                        "LOWER(c.contactName) LIKE LOWER(CONCAT('%', ?2, '%')) OR " +
                        "LOWER(c.contactSurname) LIKE LOWER(CONCAT('%', ?2, '%')))")
        List<Client> search(Long companyId, String searchTerm, Pageable pageable);

        Optional<Client> findByEmailAndCompanyId(String email, Long companyId);

        Optional<Client> findByPhoneAndCompanyId(String phone, Long companyId);
}