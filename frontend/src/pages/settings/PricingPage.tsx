import { useAuth0 } from '@auth0/auth0-react';
import { useState, useEffect } from 'react';
import { useAppStore } from '@/store/useAppStore';
import { useApi } from '@/hooks/useApi';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Check, Loader2 } from 'lucide-react';
import { toast } from 'sonner';
import { useSearchParams, useNavigate } from 'react-router-dom';

import { getEnv } from '@/utils/env';

const PLANS = [
    {
        id: 'price_free',
        name: 'Free',
        price: '$0',
        description: 'Perfect for getting started',
        features: ['Up to 5 Quotes and Invoices/month', 'Email Support', 'Custom Templates', 'Customer & Employee CRM'],
        variant: 'outline',
        disabled: false
    },
    {
        id: getEnv('STRIPE_PRICE_BASIC') || 'price_1SwXG9BN4BBTzyIzU1G5sPnW', // Basic plan $5/mo
        name: 'Basic',
        price: '$5/mo',
        description: 'For growing small businesses',
        features: ['1 Month Free Trial', 'Unlimited Quotes', 'Unlimited Invoices', 'Priority Support'],
        variant: 'default',
        isPopular: true,
        disabled: false
    },
    {
        id: getEnv('STRIPE_PRICE_ADVANCED') || 'price_1T5cMbBN4BBTzyIzA7uPHGLc', // Advanced plan $15/mo
        name: 'Advanced',
        price: '$15/mo',
        description: 'For established enterprises',
        features: ['Everything in Basic', 'Recurring Invoices', 'Expense Management', 'Job Management (Coming Soon)', 'Asset Management (Coming Soon)', 'Advanced Analytics (Coming Soon)'],
        variant: 'secondary',
        disabled: false
    }
];

interface UserProfile {
    email: string;
    name: string;
    subscriptionPlan: string;
    subscriptionStatus: string;
    stripeCustomerId: string;
    cancelAtPeriodEnd?: boolean;
}

export default function PricingPage() {
    const { selectedCompanyId } = useAppStore();
    const { user } = useAuth0();
    const api = useApi();
    const [loadingPlan, setLoadingPlan] = useState<string | null>(null);
    const [profile, setProfile] = useState<UserProfile | null>(null);
    const [loadingProfile, setLoadingProfile] = useState(true);
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    // Fetch user profile to determine current plan
    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const response = await api.get('/api/users/profile', {
                    headers: {
                        'X-User-Id': user?.sub
                    }
                });
                setProfile(response.data);
            } catch (error) {
                console.error('Failed to fetch profile', error);
            } finally {
                setLoadingProfile(false);
            }
        };

        if (user?.sub) {
            fetchProfile();
        }
    }, [user?.sub]);

    // Handle Stripe redirect
    useEffect(() => {
        const success = searchParams.get('success');
        const canceled = searchParams.get('canceled');

        if (success === 'true') {
            toast.success('Subscription activated! Welcome to the Basic plan with 30-day free trial.');
            // Clean up URL
            navigate('/settings/billing', { replace: true });
        } else if (canceled === 'true') {
            toast.info('Checkout canceled. You can upgrade anytime!');
            // Clean up URL
            navigate('/settings/billing', { replace: true });
        }
    }, [searchParams, navigate]);

    const handleSubscribe = async (plan: typeof PLANS[0]) => {
        if (!selectedCompanyId) {
            toast.error("Please select a company first");
            return;
        }

        if (plan.name === 'Free') {
            toast.success("You are already on the Free plan!");
            return;
        }

        try {
            setLoadingPlan(plan.name);
            // Call backend to create session
            const response = await api.post('/api/payments/create-checkout-session', {
                priceId: plan.id,
                planName: plan.name
            }, {
                headers: {
                    'X-User-Id': user?.sub
                }
            });

            if (response.data.url) {
                window.location.href = response.data.url;
            } else {
                toast.error("Failed to start checkout");
            }

        } catch (error) {
            console.error(error);
            toast.error("Failed to initiate payment");
        } finally {
            setLoadingPlan(null);
        }
    };

    // Helper function to determine if a plan is the user's current plan
    const isCurrentPlan = (plan: typeof PLANS[0]) => {
        if (!profile) {
            // If profile hasn't loaded yet, default to Free being current
            return plan.name === 'Free';
        }

        const currentPlan = profile.subscriptionPlan || 'Free';
        return plan.name === currentPlan;
    };

    if (loadingProfile) {
        return (
            <div className="container mx-auto py-10 max-w-6xl">
                <div className="text-center mb-10 space-y-4">
                    <h1 className="text-4xl font-bold tracking-tight">Simple, Transparent Pricing</h1>
                    <p className="text-xl text-muted-foreground">Choose the plan that's right for your business</p>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                    {[1, 2, 3].map((i) => (
                        <div key={i} className="animate-pulse">
                            <div className="h-96 bg-gray-200 rounded-lg"></div>
                        </div>
                    ))}
                </div>
            </div>
        );
    }

    return (
        <div className="container mx-auto py-10 max-w-6xl">
            <div className="text-center mb-10 space-y-4">
                <h1 className="text-4xl font-bold tracking-tight">Simple, Transparent Pricing</h1>
                <p className="text-xl text-muted-foreground">Choose the plan that's right for your business</p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                {PLANS.map((plan) => (
                    <Card key={plan.name} className={`flex flex-col relative ${plan.isPopular ? 'border-primary shadow-lg scale-105' : ''} ${isCurrentPlan(plan) ? 'border-green-500 border-2' : ''}`}>
                        {plan.isPopular && !isCurrentPlan(plan) && (
                            <div className="absolute top-0 right-0 -mr-2 -mt-2">
                                <Badge className="px-3 py-1">Most Popular</Badge>
                            </div>
                        )}
                        {isCurrentPlan(plan) && (
                            <div className="absolute top-0 right-0 -mr-2 -mt-2">
                                <Badge className="px-3 py-1 bg-green-600">
                                    {profile?.subscriptionStatus === 'trialing' ? 'Current Plan (Trial)' : 'Current Plan'}
                                </Badge>
                            </div>
                        )}
                        <CardHeader>
                            <CardTitle className="text-2xl">{plan.name}</CardTitle>
                            <CardDescription>{plan.description}</CardDescription>
                        </CardHeader>
                        <CardContent className="flex-1">
                            <div className="mb-6">
                                <span className="text-4xl font-bold">{plan.price}</span>
                                {plan.price !== '$0' && !plan.price.includes('/mo') && <span className="text-muted-foreground">/month</span>}
                            </div>
                            <ul className="space-y-3">
                                {plan.features.map((feature) => (
                                    <li key={feature} className="flex items-center gap-2">
                                        <Check className="h-4 w-4 text-green-500" />
                                        <span className="text-sm">{feature}</span>
                                    </li>
                                ))}
                            </ul>
                        </CardContent>
                        <CardFooter>
                            <Button
                                className="w-full"
                                variant={plan.variant as any}
                                onClick={() => handleSubscribe(plan)}
                                disabled={!!loadingPlan || !!plan.disabled || isCurrentPlan(plan)}
                            >
                                {loadingPlan === plan.name ? (
                                    <><Loader2 className="mr-2 h-4 w-4 animate-spin" /> Processing...</>
                                ) : (
                                    plan.disabled ? 'Not Available Yet' :
                                        isCurrentPlan(plan) ? (
                                            profile?.subscriptionStatus === 'trialing' ? 'Current Plan (Free Trial)' : 'Current Plan'
                                        ) :
                                            plan.name === 'Basic' ? 'Start 1 Month Free Trial' : `Subscribe to ${plan.name}`
                                )}
                            </Button>
                        </CardFooter>
                    </Card>
                ))}
            </div>
        </div>
    );
}
