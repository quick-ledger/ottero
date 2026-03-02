package io.quickledger.repositories.job;

import io.quickledger.entities.job.JobAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobAttachmentRepository extends JpaRepository<JobAttachment, Long> {

    List<JobAttachment> findAllByJobId(Long jobId);

    Optional<JobAttachment> findByIdAndCompanyId(Long id, Long companyId);
}
