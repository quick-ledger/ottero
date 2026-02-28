/**
 * Centralized plan configuration for feature gating.
 * Keep in sync with backend PlanService.java
 */

export type Plan = 'Free' | 'Basic' | 'Advanced';

export type Feature =
    // Free plan features
    | 'QUOTES_LIMITED'
    | 'INVOICES_LIMITED'
    | 'EMAIL_SUPPORT'
    | 'CUSTOM_TEMPLATES'
    | 'CUSTOMER_CRM'
    | 'EMPLOYEE_CRM'
    // Basic plan features
    | 'QUOTES_UNLIMITED'
    | 'INVOICES_UNLIMITED'
    | 'PRIORITY_SUPPORT'
    // Advanced plan features
    | 'RECURRING_INVOICES'
    | 'EXPENSE_MANAGEMENT'
    | 'JOB_MANAGEMENT'
    | 'ASSET_MANAGEMENT'
    | 'ADVANCED_ANALYTICS';

// Plan tier ordering (higher = more features)
const PLAN_TIERS: Record<Plan, number> = {
    Free: 0,
    Basic: 1,
    Advanced: 2,
};

// Features available for each plan
const PLAN_FEATURES: Record<Plan, Set<Feature>> = {
    Free: new Set([
        'QUOTES_LIMITED',
        'INVOICES_LIMITED',
        'EMAIL_SUPPORT',
        'CUSTOM_TEMPLATES',
        'CUSTOMER_CRM',
        'EMPLOYEE_CRM',
    ]),
    Basic: new Set([
        'QUOTES_UNLIMITED',
        'INVOICES_UNLIMITED',
        'PRIORITY_SUPPORT',
        'EMAIL_SUPPORT',
        'CUSTOM_TEMPLATES',
        'CUSTOMER_CRM',
        'EMPLOYEE_CRM',
    ]),
    Advanced: new Set([
        'QUOTES_UNLIMITED',
        'INVOICES_UNLIMITED',
        'PRIORITY_SUPPORT',
        'EMAIL_SUPPORT',
        'CUSTOM_TEMPLATES',
        'CUSTOMER_CRM',
        'EMPLOYEE_CRM',
        'RECURRING_INVOICES',
        'EXPENSE_MANAGEMENT',
        // Coming Soon: JOB_MANAGEMENT, ASSET_MANAGEMENT, ADVANCED_ANALYTICS
    ]),
};

// Minimum plan required for each feature
const FEATURE_MINIMUM_PLAN: Record<Feature, Plan> = {
    QUOTES_LIMITED: 'Free',
    INVOICES_LIMITED: 'Free',
    EMAIL_SUPPORT: 'Free',
    CUSTOM_TEMPLATES: 'Free',
    CUSTOMER_CRM: 'Free',
    EMPLOYEE_CRM: 'Free',
    QUOTES_UNLIMITED: 'Basic',
    INVOICES_UNLIMITED: 'Basic',
    PRIORITY_SUPPORT: 'Basic',
    RECURRING_INVOICES: 'Advanced',
    EXPENSE_MANAGEMENT: 'Advanced',
    JOB_MANAGEMENT: 'Advanced',
    ASSET_MANAGEMENT: 'Advanced',
    ADVANCED_ANALYTICS: 'Advanced',
};

// Human-readable feature names
export const FEATURE_LABELS: Record<Feature, string> = {
    QUOTES_LIMITED: 'Up to 5 Quotes/month',
    INVOICES_LIMITED: 'Up to 5 Invoices/month',
    EMAIL_SUPPORT: 'Email Support',
    CUSTOM_TEMPLATES: 'Custom Templates',
    CUSTOMER_CRM: 'Customer CRM',
    EMPLOYEE_CRM: 'Employee CRM',
    QUOTES_UNLIMITED: 'Unlimited Quotes',
    INVOICES_UNLIMITED: 'Unlimited Invoices',
    PRIORITY_SUPPORT: 'Priority Support',
    RECURRING_INVOICES: 'Recurring Invoices',
    EXPENSE_MANAGEMENT: 'Expense Management',
    JOB_MANAGEMENT: 'Job Management',
    ASSET_MANAGEMENT: 'Asset Management',
    ADVANCED_ANALYTICS: 'Advanced Analytics',
};

// Features marked as "Coming Soon"
export const COMING_SOON_FEATURES: Set<Feature> = new Set([
    'JOB_MANAGEMENT',
    'ASSET_MANAGEMENT',
    'ADVANCED_ANALYTICS',
]);

/**
 * Normalize plan name from API response.
 */
export function normalizePlan(planName: string | null | undefined): Plan {
    if (!planName) return 'Free';
    const normalized = planName.charAt(0).toUpperCase() + planName.slice(1).toLowerCase();
    if (normalized === 'Basic' || normalized === 'Advanced') return normalized as Plan;
    return 'Free';
}

/**
 * Check if a plan has access to a feature.
 */
export function hasFeature(plan: Plan, feature: Feature): boolean {
    return PLAN_FEATURES[plan].has(feature);
}

/**
 * Check if user plan meets minimum plan requirement.
 */
export function hasMinimumPlan(userPlan: Plan, minimumPlan: Plan): boolean {
    return PLAN_TIERS[userPlan] >= PLAN_TIERS[minimumPlan];
}

/**
 * Get the minimum plan required for a feature.
 */
export function getMinimumPlanForFeature(feature: Feature): Plan {
    return FEATURE_MINIMUM_PLAN[feature];
}

/**
 * Check if a feature is coming soon.
 */
export function isComingSoon(feature: Feature): boolean {
    return COMING_SOON_FEATURES.has(feature);
}

/**
 * Get upgrade message for a feature.
 */
export function getUpgradeMessage(feature: Feature): string {
    const minPlan = getMinimumPlanForFeature(feature);
    const label = FEATURE_LABELS[feature];
    return `${label} requires the ${minPlan} plan. Please upgrade to access this feature.`;
}
