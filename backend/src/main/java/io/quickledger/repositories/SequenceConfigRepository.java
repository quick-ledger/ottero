package io.quickledger.repositories;

import io.quickledger.entities.SequenceConfig;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SequenceConfigRepository extends JpaRepository<SequenceConfig, Long> {
    Optional<List<SequenceConfig>> findByCompanyId(Long companyId);

    // help implement findByIdAndCompanyId() and deleteByIdAndCompanyId()
    Optional<SequenceConfig> findByIdAndCompanyId(Long id, Long companyId);

    void deleteByIdAndCompanyId(Long id, Long companyId);

    Optional<SequenceConfig> findByEntityTypeAndCompanyId(SequenceConfig.EntityType type, Long companyId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SequenceConfig s WHERE s.entityType = :type AND s.companyId = :companyId")
    Optional<SequenceConfig> findByEntityTypeAndCompanyIdForUpdate(
            @Param("type") SequenceConfig.EntityType type,
            @Param("companyId") Long companyId);
}