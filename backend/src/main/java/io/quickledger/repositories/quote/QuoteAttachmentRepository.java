package io.quickledger.repositories.quote;

import io.quickledger.entities.quote.QuoteAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuoteAttachmentRepository extends JpaRepository<QuoteAttachment, Long> {
    List<QuoteAttachment> findAllByQuoteId(Long quoteId);

    Optional<QuoteAttachment> findByIdAndCompanyId(Long id, Long companyId);
}
