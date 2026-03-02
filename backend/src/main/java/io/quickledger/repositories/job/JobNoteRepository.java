package io.quickledger.repositories.job;

import io.quickledger.entities.job.JobNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobNoteRepository extends JpaRepository<JobNote, Long> {

    List<JobNote> findByJobIdOrderByNoteDateDesc(Long jobId);

    Optional<JobNote> findByIdAndCompanyId(Long id, Long companyId);
}
