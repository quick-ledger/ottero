package io.quickledger.controllers;

import io.quickledger.services.StripeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import io.quickledger.entities.User;
import io.quickledger.security.UserIdAuth;

import java.util.Map;

@RestController
@RequestMapping("/stripe")
public class StripeConnectController {

    private final StripeService stripeService;

    @Value("${application.frontend.url:https://dashboard.ottero.com.au}")
    private String frontendUrl;

    public StripeConnectController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    // Endpoint to get the OAuth link
    // Example usage: GET /api/stripe/connect?companyId=123
    @GetMapping("/connect")
    public ResponseEntity<Map<String, String>> connect(@RequestParam Long companyId, @UserIdAuth User user) {
        // In real world, verify user has access to company (e.g. via @UserIdAuth or
        // security context)
        // For MVP, we pass companyId and userId
        String state = companyId + "_" + user.getId();
        String url = stripeService.createStripeConnectOAuthUrl(state);
        return ResponseEntity.ok(Map.of("url", url));
    }

    // Callback
    @GetMapping("/callback")
    public RedirectView callback(@RequestParam String code, @RequestParam String state) {
        try {
            // State format: companyId_userId
            String[] parts = state.split("_");
            Long companyId = Long.parseLong(parts[0]);

            stripeService.connectCompanyAccount(code, companyId);

            // Redirect to company page
            return new RedirectView(frontendUrl + "/companies/" + companyId + "?success=stripe_connected");
        } catch (Exception e) {
            e.printStackTrace();
            return new RedirectView(frontendUrl + "/settings?error=stripe_connection_failed");
        }
    }
}
