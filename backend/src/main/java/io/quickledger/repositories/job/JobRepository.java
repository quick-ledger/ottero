package io.quickledger.repositories.job;

import io.quickledger.entities.job.Job;
import io.quickledger.entities.job.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    Page<Job> findByCompanyIdOrderByScheduledDateDesc(Long companyId, Pageable pageable);

    Optional<Job> findByIdAndCompanyId(Long id, Long companyId);

    Page<Job> findByCompanyIdAndStatus(Long companyId, JobStatus status, Pageable pageable);

    List<Job> findByCompanyIdAndClientId(Long companyId, Long clientId);

    @Query("SELECT j FROM Job j WHERE j.company.id = :companyId AND " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(j.jobNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(j.location) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Job> searchJobs(
            @Param("companyId") Long companyId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    @Query("SELECT COUNT(j) FROM Job j WHERE j.company.id = :companyId AND j.status = :status")
    long countByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") JobStatus status);
}
