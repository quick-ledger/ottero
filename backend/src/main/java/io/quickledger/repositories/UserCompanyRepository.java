package io.quickledger.repositories;

import io.quickledger.entities.Company;
import io.quickledger.entities.User;
import io.quickledger.entities.UserCompany;
import io.quickledger.entities.UserCompanyId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCompanyRepository extends CrudRepository<UserCompany, UserCompanyId> {
    List<UserCompany> findByUserId(Long userId);

    void deleteByCompanyId(Long companyId);

    Optional<UserCompany> findByUserAndCompany(User user, Company company);

    Optional<UserCompany> findByUserIdAndCompanyId(Long userId, Long companyId);
}