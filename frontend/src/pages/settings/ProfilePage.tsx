import { useAuth0 } from '@auth0/auth0-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { User, Mail, CreditCard, Calendar } from 'lucide-react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { useApi } from '@/hooks/useApi';
import { toast } from 'sonner';

interface UserProfile {
    email: string;
    name: string;
    subscriptionPlan: string;
    subscriptionStatus: string;
    stripeCustomerId: string;
    cancelAtPeriodEnd?: boolean;
}

export default function ProfilePage() {
    const { user, logout } = useAuth0();
    const navigate = useNavigate();
    const api = useApi();
    const [profile, setProfile] = useState<UserProfile | null>(null);
    const [loading, setLoading] = useState(true);
    const [searchParams, setSearchParams] = useSearchParams();

    useEffect(() => {
        const success = searchParams.get('success');
        const canceled = searchParams.get('canceled');

        if (success === 'true') {
            toast.success('Subscription updated successfully!');
            // clear params
            setSearchParams({});
            // Refresh profile to show new status
            fetchProfile();
        } else if (canceled === 'true') {
            toast.info('Subscription update canceled.');
            setSearchParams({});
        }
    }, [searchParams, setSearchParams]);

    useEffect(() => {
        fetchProfile();
    }, []);

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
            toast.error('Failed to load profile information');
        } finally {
            setLoading(false);
        }
    };

    const handleManageSubscription = async () => {
        if (!profile?.stripeCustomerId) {
            // User doesn't have a subscription yet, redirect to billing page
            navigate('/settings/pricing');
            return;
        }

        try {
            const response = await api.post('/api/payments/create-customer-portal-session', {}, {
                headers: {
                    'X-User-Id': user?.sub
                }
            });

            if (response.data.url) {
                window.location.href = response.data.url;
            } else {
                toast.error('Failed to open subscription management');
            }
        } catch (error) {
            console.error('Failed to create portal session', error);
            toast.error('Failed to open subscription management');
        }
    };

    const getPlanBadgeVariant = (plan: string) => {
        switch (plan?.toLowerCase()) {
            case 'basic':
                return 'default';
            case 'advanced':
                return 'secondary';
            default:
                return 'outline';
        }
    };

    const getStatusBadgeVariant = (status: string) => {
        switch (status?.toLowerCase()) {
            case 'active':
                return 'default';
            case 'trialing':
                return 'secondary';
            case 'past_due':
            case 'canceled':
                return 'destructive';
            default:
                return 'outline';
        }
    };

    if (loading) {
        return (
            <div className="container mx-auto py-10 max-w-4xl">
                <div className="animate-pulse space-y-4">
                    <div className="h-8 bg-gray-200 rounded w-1/4"></div>
                    <div className="h-64 bg-gray-200 rounded"></div>
                </div>
            </div>
        );
    }

    return (
        <div className="container mx-auto py-10 max-w-4xl">
            <div className="mb-8">
                <h1 className="text-3xl font-bold tracking-tight">Profile & Account</h1>
                <p className="text-muted-foreground mt-2">
                    Manage your account information and subscription
                </p>
            </div>

            <div className="space-y-6">
                {/* Personal Information */}
                <Card>
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                            <User className="h-5 w-5" />
                            Personal Information
                        </CardTitle>
                        <CardDescription>Your account details</CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div>
                                <label className="text-sm font-medium text-muted-foreground">Name</label>
                                <p className="text-lg font-medium">{profile?.name || user?.name || 'Not set'}</p>
                            </div>
                            <div>
                                <label className="text-sm font-medium text-muted-foreground flex items-center gap-2">
                                    <Mail className="h-4 w-4" />
                                    Email
                                </label>
                                <p className="text-lg font-medium">{profile?.email || user?.email || 'Not set'}</p>
                            </div>
                        </div>
                    </CardContent>
                </Card>

                {/* Subscription Information */}
                <Card>
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                            <CreditCard className="h-5 w-5" />
                            Subscription Details
                        </CardTitle>
                        <CardDescription>Your current plan and billing information</CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-6">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div>
                                <label className="text-sm font-medium text-muted-foreground">Current Plan</label>
                                <div className="flex items-center gap-2 mt-1">

                                    <Badge variant={getPlanBadgeVariant(profile?.subscriptionPlan || 'free')}>
                                        {profile?.subscriptionPlan || 'Free'}
                                    </Badge>
                                </div>
                            </div>
                            <div>
                                <label className="text-sm font-medium text-muted-foreground">Status</label>
                                <div className="flex items-center gap-2 mt-1">
                                    <Badge variant={getStatusBadgeVariant(profile?.subscriptionStatus || 'free')}>
                                        {profile?.subscriptionStatus || 'Free'}
                                    </Badge>
                                    {profile?.cancelAtPeriodEnd && (
                                        <Badge variant="outline" className="ml-2 text-amber-600 border-amber-600">
                                            Ends Soon
                                        </Badge>
                                    )}
                                </div>
                                {profile?.cancelAtPeriodEnd && (
                                    <p className="text-xs text-amber-600 mt-1">
                                        Access continues until end of billing period.
                                    </p>
                                )}
                            </div>
                        </div>

                        <Separator />

                        <div className="flex flex-col sm:flex-row gap-3">
                            <Button onClick={handleManageSubscription} className="flex-1">
                                <Calendar className="mr-2 h-4 w-4" />
                                {profile?.stripeCustomerId ? 'Manage Subscription in Stripe' : 'View Plans'}
                            </Button>
                        </div>

                        {profile?.subscriptionPlan === 'Basic' && (
                            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                                <p className="text-sm text-blue-900">
                                    <strong>Basic Plan Benefits:</strong> Unlimited quotes and invoices, priority support, and all essential features.
                                </p>
                            </div>
                        )}

                        {(!profile?.subscriptionPlan || profile?.subscriptionPlan === 'Free') && (
                            <div className="bg-amber-50 border border-amber-200 rounded-lg p-4">
                                <p className="text-sm text-amber-900">
                                    <strong>Free Plan:</strong> You're currently on the free plan with up to 5 quotes/invoices per month. Upgrade to unlock unlimited access!
                                </p>
                            </div>
                        )}
                    </CardContent>
                </Card>

                {/* Account Actions */}
                <Card>
                    <CardHeader>
                        <CardTitle>Account Actions</CardTitle>
                        <CardDescription>Manage your account settings</CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-3">
                        <Button
                            variant="outline"
                            className="w-full justify-start"
                            onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}
                        >
                            Sign Out
                        </Button>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
