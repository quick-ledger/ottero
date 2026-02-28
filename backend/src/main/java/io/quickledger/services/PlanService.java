package io.quickledger.services;

import io.quickledger.entities.User;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;

/**
 * Centralized service for plan-based feature access control.
 * Defines which features are available for each subscription plan.
 */
@Service
public class PlanService {

    /**
     * All features that can be gated by plan.
     */
    public enum Feature {
        // Free plan features
        QUOTES_LIMITED,           // Up to 5/month
        INVOICES_LIMITED,         // Up to 5/month
        EMAIL_SUPPORT,
        CUSTOM_TEMPLATES,
        CUSTOMER_CRM,
        EMPLOYEE_CRM,

        // Basic plan features (includes Free features)
        QUOTES_UNLIMITED,
        INVOICES_UNLIMITED,
        PRIORITY_SUPPORT,

        // Advanced plan features (includes Basic features)
        RECURRING_INVOICES,
        EXPENSE_MANAGEMENT,
        JOB_MANAGEMENT,           // Coming Soon
        ASSET_MANAGEMENT,         // Coming Soon
        ADVANCED_ANALYTICS        // Coming Soon
    }

    /**
     * Subscription plans in order of tier (higher index = higher tier).
     */
    public enum Plan {
        FREE(0),
        BASIC(1),
        ADVANCED(2);

        private final int tier;

        Plan(int tier) {
            this.tier = tier;
        }

        public int getTier() {
            return tier;
        }

        public static Plan fromString(String planName) {
            if (planName == null) return FREE;
            return switch (planName.toUpperCase()) {
                case "BASIC" -> BASIC;
                case "ADVANCED" -> ADVANCED;
                default -> FREE;
            };
        }
    }

    // Feature sets for each plan
    private static final Set<Feature> FREE_FEATURES = EnumSet.of(
            Feature.QUOTES_LIMITED,
            Feature.INVOICES_LIMITED,
            Feature.EMAIL_SUPPORT,
            Feature.CUSTOM_TEMPLATES,
            Feature.CUSTOMER_CRM,
            Feature.EMPLOYEE_CRM
    );

    private static final Set<Feature> BASIC_FEATURES = EnumSet.of(
            // Includes all free features conceptually, plus:
            Feature.QUOTES_UNLIMITED,
            Feature.INVOICES_UNLIMITED,
            Feature.PRIORITY_SUPPORT,
            Feature.EMAIL_SUPPORT,
            Feature.CUSTOM_TEMPLATES,
            Feature.CUSTOMER_CRM,
            Feature.EMPLOYEE_CRM
    );

    private static final Set<Feature> ADVANCED_FEATURES = EnumSet.of(
            // Includes all basic features, plus:
            Feature.QUOTES_UNLIMITED,
            Feature.INVOICES_UNLIMITED,
            Feature.PRIORITY_SUPPORT,
            Feature.EMAIL_SUPPORT,
            Feature.CUSTOM_TEMPLATES,
            Feature.CUSTOMER_CRM,
            Feature.EMPLOYEE_CRM,
            Feature.RECURRING_INVOICES,
            Feature.EXPENSE_MANAGEMENT
            // JOB_MANAGEMENT, ASSET_MANAGEMENT, ADVANCED_ANALYTICS - Coming Soon
    );

    /**
     * Check if a user has access to a specific feature.
     */
    public boolean hasFeature(User user, Feature feature) {
        Plan plan = Plan.fromString(user.getSubscriptionPlan());
        return hasFeature(plan, feature);
    }

    /**
     * Check if a plan has access to a specific feature.
     */
    public boolean hasFeature(Plan plan, Feature feature) {
        return switch (plan) {
            case FREE -> FREE_FEATURES.contains(feature);
            case BASIC -> BASIC_FEATURES.contains(feature);
            case ADVANCED -> ADVANCED_FEATURES.contains(feature);
        };
    }

    /**
     * Get the user's current plan.
     */
    public Plan getUserPlan(User user) {
        return Plan.fromString(user.getSubscriptionPlan());
    }

    /**
     * Check if user has at least the specified plan tier.
     */
    public boolean hasMinimumPlan(User user, Plan minimumPlan) {
        Plan userPlan = getUserPlan(user);
        return userPlan.getTier() >= minimumPlan.getTier();
    }

    /**
     * Validate that user has access to a feature, throwing an exception if not.
     */
    public void requireFeature(User user, Feature feature) {
        if (!hasFeature(user, feature)) {
            Plan requiredPlan = getMinimumPlanForFeature(feature);
            throw new IllegalStateException(
                    String.format("%s requires %s plan or higher. Please upgrade to access this feature.",
                            formatFeatureName(feature), requiredPlan.name()));
        }
    }

    /**
     * Get the minimum plan required for a feature.
     */
    public Plan getMinimumPlanForFeature(Feature feature) {
        if (FREE_FEATURES.contains(feature)) return Plan.FREE;
        if (BASIC_FEATURES.contains(feature)) return Plan.BASIC;
        return Plan.ADVANCED;
    }

    /**
     * Check if user can create more quotes/invoices this month (for limited plans).
     */
    public boolean canCreateMoreDocuments(User user, long currentMonthCount) {
        Plan plan = getUserPlan(user);
        if (plan == Plan.FREE) {
            return currentMonthCount < 5;
        }
        // Basic and Advanced have unlimited
        return true;
    }

    /**
     * Get monthly document limit for a plan (quotes + invoices).
     */
    public int getMonthlyDocumentLimit(Plan plan) {
        return switch (plan) {
            case FREE -> 5;
            case BASIC, ADVANCED -> Integer.MAX_VALUE; // Unlimited
        };
    }

    private String formatFeatureName(Feature feature) {
        return feature.name()
                .replace("_", " ")
                .toLowerCase()
                .substring(0, 1).toUpperCase() +
                feature.name().replace("_", " ").toLowerCase().substring(1);
    }
}
