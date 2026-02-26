package io.quickledger.controllers;

import io.quickledger.dto.ReferralDto;
import io.quickledger.services.ReferralService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/referrals")
public class ReferralController {

    private static final Logger logger = LoggerFactory.getLogger(ReferralController.class);

    private final ReferralService referralService;

    public ReferralController(ReferralService referralService) {
        this.referralService = referralService;
    }

    @PostMapping
    public ResponseEntity<?> createReferral(
            @RequestHeader("X-User-Id") String userExternalId,
            @RequestBody Map<String, String> payload) {

        logger.debug("Creating referral for user: {}", userExternalId);

        String refereeEmail = payload.get("refereeEmail");
        String refereeName = payload.get("refereeName");

        if (refereeEmail == null || refereeEmail.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Referee email is required"));
        }

        try {
            ReferralDto referral = referralService.createReferral(userExternalId, refereeEmail.trim(), refereeName);
            return ResponseEntity.status(HttpStatus.CREATED).body(referral);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to create referral: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<ReferralDto>> getReferrals(
            @RequestHeader("X-User-Id") String userExternalId) {

        logger.debug("Getting referrals for user: {}", userExternalId);

        try {
            List<ReferralDto> referrals = referralService.getReferralsByUser(userExternalId);
            return ResponseEntity.ok(referrals);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to get referrals: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
