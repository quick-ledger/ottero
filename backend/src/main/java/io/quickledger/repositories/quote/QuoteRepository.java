package io.quickledger.repositories.quote;

import io.quickledger.entities.quote.Quote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuoteRepository extends JpaRepository<Quote, Long> {
        List<Quote> findByCompanyId(Long companyId);

        Page<Quote> findByCompanyId(Long companyId, Pageable pageable);

        Optional<Quote> findByQuoteNumberAndQuoteRevisionAndCompanyId(String quoteNumber, Integer quoteRevision,
                        Long companyId);

        List<Quote> findByQuoteNumberAndCompanyId(String quoteNumber, Long companyId);

        @Query(value = "SELECT * FROM quotes q WHERE q.quote_number = :quoteNumber AND q.company_id = :companyId ORDER BY q.quote_revision DESC LIMIT 1", nativeQuery = true)
        Optional<Quote> findLatestRevisionByQuoteNumberAndCompanyId(@Param("quoteNumber") String quoteNumber,
                        @Param("companyId") Long companyId);

        // return only unique quote numbers and latest revisions
        // @Query(value = "SELECT q.* FROM quotes q INNER JOIN ( SELECT quote_number,
        // MAX(quote_revision) AS max_revision FROM quotes GROUP BY quote_number ) AS
        // latest_revision ON q.quote_number = latest_revision.quote_number AND
        // q.quote_revision = latest_revision.max_revision WHERE q.company_id =
        // :companyId ORDER BY q.modified_date DESC", nativeQuery = true)
        @Query(value = "SELECT q.* FROM quotes q INNER JOIN ( SELECT quote_number, MAX(quote_revision) AS max_revision FROM quotes WHERE company_id = :companyId GROUP BY quote_number ) AS latest_revision ON q.quote_number = latest_revision.quote_number AND q.quote_revision = latest_revision.max_revision WHERE q.company_id = :companyId ORDER BY q.modified_date DESC", countQuery = "SELECT count(*) FROM quotes q INNER JOIN ( SELECT quote_number, MAX(quote_revision) AS max_revision FROM quotes WHERE company_id = :companyId GROUP BY quote_number ) AS latest_revision ON q.quote_number = latest_revision.quote_number AND q.quote_revision = latest_revision.max_revision WHERE q.company_id = :companyId", nativeQuery = true)
        Page<Quote> getAllQuotesLatestRevision(@Param("companyId") Long companyId, Pageable pageable);

        @Query("SELECT q FROM Quote q JOIN Client c ON q.client.id = c.id WHERE (LOWER(q.quoteNumber) LIKE LOWER(CONCAT('%', :searchTerms, '%')) OR LOWER(c.contactName) LIKE LOWER(CONCAT('%', :searchTerms, '%')) OR LOWER(c.contactSurname) LIKE LOWER(CONCAT('%', :searchTerms, '%')) OR LOWER(c.entityName) LIKE LOWER(CONCAT('%', :searchTerms, '%'))) AND q.company.id = :companyId")
        List<Quote> searchQuote(Long companyId, String searchTerms);

        /**
         * Count unique quotes (by quote_number) created this month for a company.
         * Used for free plan limit enforcement (5 quotes/invoices per month).
         */
        @Query(value = "SELECT COUNT(DISTINCT quote_number) FROM quotes WHERE company_id = :companyId AND YEAR(created_date) = YEAR(CURRENT_DATE) AND MONTH(created_date) = MONTH(CURRENT_DATE)", nativeQuery = true)
        long countMonthlyQuotesByCompanyId(@Param("companyId") Long companyId);
}