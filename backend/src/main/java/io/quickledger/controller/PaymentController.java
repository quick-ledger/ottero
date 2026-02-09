package io.quickledger.controller;

import io.quickledger.services.StripeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final StripeService stripeService;

    public PaymentController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(
            @RequestHeader("X-User-Id") String userExternalId,
            @RequestBody Map<String, String> request) {
        try {
            String priceId = request.get("priceId");
            String planName = request.get("planName"); // e.g., "Basic", "Advanced"

            String sessionUrl = stripeService.createCheckoutSession(userExternalId, priceId, planName);
            return ResponseEntity.ok(Map.of("url", sessionUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/create-customer-portal-session")
    public ResponseEntity<Map<String, String>> createCustomerPortalSession(
            @RequestHeader("X-User-Id") String userExternalId) {
        try {
            String portalUrl = stripeService.createCustomerPortalSession(userExternalId);
            return ResponseEntity.ok(Map.of("url", portalUrl));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
