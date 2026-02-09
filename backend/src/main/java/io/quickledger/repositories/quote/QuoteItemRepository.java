package io.quickledger.repositories.quote;

import io.quickledger.entities.quote.Quote;
import io.quickledger.entities.quote.QuoteItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteItemRepository extends JpaRepository<QuoteItem, Long> {
    List<QuoteItem> findAllByQuote(Quote quote);
}