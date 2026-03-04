package io.quickledger.repositories.job;

import io.quickledger.entities.job.JobTimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobTimeEntryRepository extends JpaRepository<JobTimeEntry, Long> {

    List<JobTimeEntry> findByJobIdOrderByEntryDateDesc(Long jobId);

    Optional<JobTimeEntry> findByIdAndCompanyId(Long id, Long companyId);
}
