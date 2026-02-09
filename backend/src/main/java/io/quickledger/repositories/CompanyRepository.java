package io.quickledger.repositories;

import io.quickledger.entities.Company;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends CrudRepository<Company, Long> {
    // TODO RG shall we use this optional everywhere? seems to be much better approach than usual null check!
    // we don't need to add it CRUD repo actually has it! so we can remove it
    Optional<Company> findById(Long id);
}