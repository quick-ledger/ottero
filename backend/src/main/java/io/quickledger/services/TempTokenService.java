package io.quickledger.services;

import io.quickledger.entities.TempToken;
import io.quickledger.exception.TokenValidationException;
import io.quickledger.mappers.TempTokenMapper;
import io.quickledger.repositories.TempTokenRepository;
import io.quickledger.security.JwtUtil;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class TempTokenService {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TempTokenService.class);
    private final TempTokenRepository tempTokenRepository;
    private final TempTokenMapper tempTokenMapper;
    private final JwtUtil jwtUtil;

    public TempTokenService(TempTokenRepository tempTokenRepository, TempTokenMapper tempTokenMapper, JwtUtil jwtUtil) {
        this.tempTokenRepository = tempTokenRepository;
        this.tempTokenMapper = tempTokenMapper;
        this.jwtUtil = jwtUtil;
    }

    // MBH: I need to check if token is already generated for this quoteId and
    // companyId I return it.
    // first I can try to fetch it from the database then generate or the otherway
    // around
    // RG: let me know what you think?

    public String generateToken(Map<String, Object> claims, long expirationTimeMillis, String tokenSubject,
            TempToken.TokenType tokenType) {
        Long clientId = Long.valueOf(claims.get("clientId").toString());
        Long companyId = Long.valueOf(claims.get("companyId").toString());

        // Generate the token first
        Instant expiryInstant = Instant.now().plusMillis(expirationTimeMillis);
        Date expiryDate = Date.from(expiryInstant);
        LocalDateTime expiryLocalDateTime = LocalDateTime.ofInstant(expiryInstant, ZoneOffset.UTC);
        String token = jwtUtil.generateToken(claims, tokenSubject, expiryDate);

        // Check if a token already exists for this combination
        Optional<TempToken> existingTokenOpt = tempTokenRepository.findByClientIdAndCompanyIdAndTokenType(clientId,
                companyId, tokenType);

        if (existingTokenOpt.isPresent()) {
            TempToken existingToken = existingTokenOpt.get();
            existingToken.setToken(token);
            existingToken.setExpiryTime(expiryLocalDateTime);
            tempTokenRepository.save(existingToken);
        } else {
            TempToken newTempToken = new TempToken(token, clientId, companyId, expiryLocalDateTime, tokenType);
            tempTokenRepository.save(newTempToken);
        }

        return token;
    }

    public Map<String, Object> validateToken(String token, TempToken.TokenType tokenType) {
        Map<String, Object> claims = jwtUtil.getAllClaimsFromToken(token);
        Object clientIdClaim = claims.get(QuoteTokenClaims.CLIENT_ID.getClaimKey());
        Object companyIdClaim = claims.get(QuoteTokenClaims.COMPANY_ID.getClaimKey());

        Long clientId = (clientIdClaim instanceof Number) ? ((Number) clientIdClaim).longValue()
                : Long.valueOf(clientIdClaim.toString());
        Long companyId = (companyIdClaim instanceof Number) ? ((Number) companyIdClaim).longValue()
                : Long.valueOf(companyIdClaim.toString());

        TempToken tempToken = tempTokenRepository.findByClientIdAndCompanyIdAndTokenType(clientId, companyId, tokenType)
                .filter(t -> t.getToken().equals(token) && t.getExpiryTime().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new TokenValidationException("Token is invalid or expired.", HttpStatus.FORBIDDEN));

        if (!tempToken.getClientId().equals(clientId) || !tempToken.getCompanyId().equals(companyId)) {
            throw new AccessDeniedException("Access denied for the provided token.");
        }
        return claims;
    }

    /*
     * MBH: somehow now that I implemented the token generation I realized that we
     * don't care about content of jwt token.
     * For now I string match token as well as content of it but it's really not
     * needed as in database we know the owner of token.
     * For better performance always use findByQuoteIdAndClientId as I have indexed
     * it while token itself is not indexed.
     */
    /*
     * public long validateQuoteToken(String token, long quoteId, long clientId) {
     * TempToken tempToken = tempTokenRepository.findByQuoteIdAndClientId(quoteId,
     * clientId)
     * .filter(t -> t.getToken().equals(token) &&
     * t.getExpiryTime().isAfter(LocalDateTime.now()))
     * .orElseThrow(() -> new
     * TokenValidationException("Token is invalid or expired.",
     * HttpStatus.FORBIDDEN));
     * 
     * if (tempToken.getClientId() != clientId || tempToken.getQuoteId() != quoteId)
     * {
     * throw new AccessDeniedException("Access denied for the provided token.");
     * } else
     * return tempToken.getQuoteId();
     * }
     */

    public Long getClientIdFromToken(String token) {
        return tempTokenRepository.findByToken(token)
                .map(TempToken::getClientId)
                .orElse(null);
    }

    public Long getCompanyIdFromToken(String token) {
        return tempTokenRepository.findByToken(token)
                .map(TempToken::getCompanyId)
                .orElse(null);
    }

    public void invalidateToken(String token) {
        tempTokenRepository.findByToken(token).ifPresent(tempTokenRepository::delete);
    }

    public void invalidateTokenByClientIdAndCompanyIdAndTokenType(Long clientId, Long companyId,
            TempToken.TokenType tokenType) {
        tempTokenRepository.findByClientIdAndCompanyIdAndTokenType(clientId, companyId, tokenType)
                .ifPresent(tempTokenRepository::delete);
    }

    public enum QuoteTokenClaims {
        CLIENT_ID("clientId"),
        COMPANY_ID("companyId"),
        USER_ID("userId"),
        QUOTE_ID("quoteId");

        private final String claimKey;

        QuoteTokenClaims(String claimKey) {
            this.claimKey = claimKey;
        }

        public String getClaimKey() {
            return claimKey;
        }
    }

    public enum InvoiceTokenClaims {
        CLIENT_ID("clientId"),
        COMPANY_ID("companyId"),
        USER_ID("userId"),
        INVOICE_ID("invoiceId");

        private final String claimKey;

        InvoiceTokenClaims(String claimKey) {
            this.claimKey = claimKey;
        }

        public String getClaimKey() {
            return claimKey;
        }
    }

    public String getOrCreateInvoiceToken(Long clientId, Long companyId, Long invoiceId, long expirationTimeMillis) {
        // Check existing token
        Optional<TempToken> existing = tempTokenRepository.findByClientIdAndCompanyIdAndTokenType(clientId, companyId,
                TempToken.TokenType.INVOICE_TOKEN);

        if (existing.isPresent()) {
            try {
                // Parse to check if it matches the invoiceId
                Map<String, Object> claims = jwtUtil.getAllClaimsFromToken(existing.get().getToken());
                Object invoiceIdClaim = claims.get(InvoiceTokenClaims.INVOICE_ID.getClaimKey());
                Long claimInvoiceId = (invoiceIdClaim instanceof Number) ? ((Number) invoiceIdClaim).longValue()
                        : Long.valueOf(invoiceIdClaim.toString());

                // If it matches and hasn't expired (with buffer), return it
                if (claimInvoiceId.equals(invoiceId) && existing.get().getExpiryTime().isAfter(LocalDateTime.now())) {
                    return existing.get().getToken();
                }
            } catch (Exception e) {
                logger.warn("Failed to parse existing token, regenerating: " + e.getMessage());
            }
        }

        // Generate new token (this will delete the old one due to constraints)
        Map<String, Object> claims = new HashMap<>();
        claims.put(InvoiceTokenClaims.INVOICE_ID.getClaimKey(), invoiceId);
        claims.put(InvoiceTokenClaims.COMPANY_ID.getClaimKey(), companyId);
        claims.put(InvoiceTokenClaims.CLIENT_ID.getClaimKey(), clientId);

        return generateToken(claims, expirationTimeMillis, "invoice-view", TempToken.TokenType.INVOICE_TOKEN);
    }
}
