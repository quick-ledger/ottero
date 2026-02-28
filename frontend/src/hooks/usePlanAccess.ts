import { useState, useEffect, useMemo } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useApi } from './useApi';
import {
    Plan,
    Feature,
    normalizePlan,
    hasFeature,
    hasMinimumPlan,
    getMinimumPlanForFeature,
    isComingSoon,
    getUpgradeMessage,
} from '@/lib/plans';

interface UserProfile {
    subscriptionPlan: string;
    subscriptionStatus: string;
}

interface PlanAccessResult {
    // Current plan info
    plan: Plan;
    planName: string;
    isLoading: boolean;
    isTrialing: boolean;

    // Feature checks
    hasFeature: (feature: Feature) => boolean;
    hasMinimumPlan: (minimumPlan: Plan) => boolean;
    getMinimumPlanForFeature: (feature: Feature) => Plan;
    isComingSoon: (feature: Feature) => boolean;
    getUpgradeMessage: (feature: Feature) => string;

    // Convenience checks for common features
    canUseRecurringInvoices: boolean;
    canUseExpenses: boolean;
    hasUnlimitedDocuments: boolean;

    // Plan tier checks
    isFree: boolean;
    isBasic: boolean;
    isAdvanced: boolean;
    isBasicOrHigher: boolean;
    isAdvancedOrHigher: boolean;
}

/**
 * Hook for checking user's plan and feature access.
 * Fetches user profile and provides convenient methods for plan-based feature gating.
 */
export function usePlanAccess(): PlanAccessResult {
    const { user } = useAuth0();
    const api = useApi();
    const [profile, setProfile] = useState<UserProfile | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const fetchProfile = async () => {
            if (!user?.sub) {
                setIsLoading(false);
                return;
            }

            try {
                const response = await api.get('/api/users/profile', {
                    headers: { 'X-User-Id': user.sub }
                });
                setProfile(response.data);
            } catch (error) {
                console.error('Failed to fetch profile for plan access', error);
            } finally {
                setIsLoading(false);
            }
        };

        fetchProfile();
    }, [user?.sub]);

    const plan = useMemo(() => normalizePlan(profile?.subscriptionPlan), [profile?.subscriptionPlan]);

    return useMemo(() => ({
        // Current plan info
        plan,
        planName: plan,
        isLoading,
        isTrialing: profile?.subscriptionStatus?.toLowerCase() === 'trialing',

        // Feature checks
        hasFeature: (feature: Feature) => hasFeature(plan, feature),
        hasMinimumPlan: (minimumPlan: Plan) => hasMinimumPlan(plan, minimumPlan),
        getMinimumPlanForFeature,
        isComingSoon,
        getUpgradeMessage,

        // Convenience checks for common features
        canUseRecurringInvoices: hasFeature(plan, 'RECURRING_INVOICES'),
        canUseExpenses: hasFeature(plan, 'EXPENSE_MANAGEMENT'),
        hasUnlimitedDocuments: hasFeature(plan, 'QUOTES_UNLIMITED'),

        // Plan tier checks
        isFree: plan === 'Free',
        isBasic: plan === 'Basic',
        isAdvanced: plan === 'Advanced',
        isBasicOrHigher: hasMinimumPlan(plan, 'Basic'),
        isAdvancedOrHigher: hasMinimumPlan(plan, 'Advanced'),
    }), [plan, isLoading, profile?.subscriptionStatus]);
}
