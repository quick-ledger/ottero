package io.quickledger.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.quickledger.exception.TokenValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtil {

    @Value("${aws.jwt.secret}")
    private String secret;
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        // return
        // Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
        return Jwts.parser().setSigningKey(secret).build().parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /*
     * public String generateToken(UserDetails userDetails) {
     * Map<String, Object> claims = new HashMap<>();
     * return createToken(claims, userDetails.getUsername());
     * }
     */

    public String generateToken(Map<String, Object> claims, String subject, Date expiryDate) {
        logger.info("Generating token for subject: {} and secret {}", subject, secret);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public Map<String, Object> getAllClaimsFromToken(String token) throws TokenValidationException {
        try {
            if (isTokenExpired(token)) {
                throw new TokenValidationException("Token is expired", HttpStatus.FORBIDDEN);
            }
            Claims claims = Jwts.parser().setSigningKey(secret).build().parseClaimsJws(token).getBody();
            logger.debug("Claims from token: {}", claims);
            return claims;
        } catch (Exception e) {
            logger.error("Error validating token: {}", e.getMessage());
            throw new TokenValidationException("Token validation failed", HttpStatus.FORBIDDEN);
        }
    }

    /*
     * private String createToken(Map<String, Object> claims, String subject) {
     * return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new
     * Date(System.currentTimeMillis()))
     * .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
     * .signWith(SignatureAlgorithm.HS256, SECRET_KEY).compact();
     * }
     */

    /*
     * public Boolean validateToken(String token, UserDetails userDetails) {
     * final String username = extractUsername(token);
     * return (username.equals(userDetails.getUsername()) &&
     * !isTokenExpired(token));
     * }
     */

}
