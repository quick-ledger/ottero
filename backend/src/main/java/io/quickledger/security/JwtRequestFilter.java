package io.quickledger.security;

import com.auth0.jwk.*;
import io.jsonwebtoken.*;
import io.quickledger.entities.User;
import io.quickledger.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.MalformedURLException;

import java.security.PublicKey;
import java.util.Base64;
import java.util.Optional;

@Service
// @Profile("prod")
public class JwtRequestFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    private final UserService userService;

    @Value("${auth0.domain}")
    private String auth0Domain;

    private JwkProvider jwkProvider;

    public JwtRequestFilter(UserService userService) {
        this.userService = userService;
    }

    @PostConstruct
    public void init() {
        try {
            logger.info("Initializing JWK Provider for domain: {}", auth0Domain);
            if (auth0Domain == null || auth0Domain.isEmpty()) {
                throw new IllegalArgumentException("Auth0 domain is not configured");
            }
            String jwksUrl = "https://" + auth0Domain + "/.well-known/jwks.json";
            this.jwkProvider = new JwkProviderBuilder(URI.create(jwksUrl).toURL())
                    .cached(10, 24, java.util.concurrent.TimeUnit.HOURS) // Cache keys for 24 hours
                    .build();
            logger.info("JWK Provider initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize JWK Provider", e);
            throw new RuntimeException("Failed to initialize JWK Provider", e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        boolean skip = isExcludedPath(path);

        if (skip) {
            logger.debug("Skipping JWT filter for excluded path: {}", path);
        }

        return skip;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        logger.debug("Path: " + request.getRequestURI());
        if (isExcludedPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            String jwtToken = requestTokenHeader.substring(7);
            try {
                // Extract kid from the token header (unverified step just to find the key)
                String[] parts = jwtToken.split("\\.");
                if (parts.length < 2) {
                    throw new IllegalArgumentException("Invalid JWT token format");
                }
                String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
                String kid = new ObjectMapper().readTree(headerJson).get("kid").asText();

                // Fetch Public Key using JwkProvider (cached)
                Jwk jwk = jwkProvider.get(kid);
                PublicKey publicKey = jwk.getPublicKey();

                // Verify and Parse Token
                Jws<Claims> claims = Jwts.parser()
                        .verifyWith(publicKey)
                        .build()
                        .parseSignedClaims(jwtToken);

                // Extract User Details
                String sub = claims.getPayload().getSubject();
                String email = claims.getPayload().get("https://quickledger.io/email", String.class);
                if (email == null) {
                    // Fallback to standard email claim
                    email = claims.getPayload().get("email", String.class);
                }

                logger.debug("Token claims - sub: {}, email: {}", sub, email);

                Optional<User> user = userService.findByExternalId(sub);

                if (!user.isPresent() && email != null) {
                    // Fallback: Try to find by email and link account
                    user = userService.findByEmail(email);
                    if (user.isPresent()) {
                        User existingUser = user.get();
                        if (existingUser.getExternalId() == null) {
                            logger.info("Linking user {} with externalId {}", email, sub);
                            existingUser.setExternalId(sub);
                            userService.saveUser(existingUser);
                        }
                    }
                }

                if (user.isPresent()) {
                    UserDetails userDetails = new UserDetails(user.get());
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    logger.warn("User with sub {} and email {} not found in database", sub, email);
                }

            } catch (JwkException e) {
                logger.error("JWK Error: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Key ID");
                return;
            } catch (ExpiredJwtException e) {
                logger.warn("JWT Expired");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Expired JWT token");
                return;
            } catch (Exception e) {
                logger.error("JWT Verification Error", e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isExcludedPath(String path) {
        boolean excluded = path.equalsIgnoreCase("/api/stripe/webhook") ||
                path.equalsIgnoreCase("/api/stripe/callback") ||
                path.equalsIgnoreCase("/api/users/auth0-webhook-create") ||
                path.equalsIgnoreCase("/api/plans");

        if (path.contains("stripe") || path.contains("webhook")) {
            logger.debug("Path exclusion check - path: '{}', excluded: {}", path, excluded);
        }

        return excluded;
    }

}
