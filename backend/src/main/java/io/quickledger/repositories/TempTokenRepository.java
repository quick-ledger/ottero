package io.quickledger.repositories;

import com.ibm.asyncutil.iteration.AsyncIterator;
import io.quickledger.entities.TempToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TempTokenRepository extends JpaRepository<TempToken, Long> {
    Optional<TempToken> findByToken(String token);
    void deleteByExpiryTimeBefore(LocalDateTime expiryTime);

//    Optional<TempToken> findByQuoteIdAndClientId(Long quoteId, Long clientId);

    Optional<TempToken> findByClientIdAndCompanyIdAndTokenType(Long clientId, Long companyId, TempToken.TokenType tokenType);
}