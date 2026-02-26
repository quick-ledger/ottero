package io.quickledger.entities;

public enum ReferralStatus {
    PENDING,        // Referral submitted, waiting for friend to sign up
    SIGNED_UP,      // Friend signed up, discount being applied
    DISCOUNT_APPLIED // Discount successfully applied to referrer
}
