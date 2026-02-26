import { useAuth0 } from '@auth0/auth0-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { Users, Gift, Mail, Clock, CheckCircle, AlertCircle } from 'lucide-react';
import { useState, useEffect } from 'react';
import { useApi } from '@/hooks/useApi';
import { toast } from 'sonner';

interface Referral {
    refereeEmail: string;
    refereeName: string | null;
    status: 'PENDING' | 'SIGNED_UP' | 'DISCOUNT_APPLIED';
    referralCode: string;
    createdDate: string;
}

export default function ReferralPage() {
    const { user } = useAuth0();
    const api = useApi();
    const [referrals, setReferrals] = useState<Referral[]>([]);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [refereeEmail, setRefereeEmail] = useState('');
    const [refereeName, setRefereeName] = useState('');

    useEffect(() => {
        fetchReferrals();
    }, []);

    const fetchReferrals = async () => {
        try {
            const response = await api.get('/api/referrals', {
                headers: { 'X-User-Id': user?.sub }
            });
            setReferrals(response.data);
        } catch (error) {
            console.error('Failed to fetch referrals', error);
        } finally {
            setLoading(false);
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!refereeEmail.trim()) {
            toast.error('Please enter your friend\'s email address');
            return;
        }

        setSubmitting(true);
        try {
            await api.post('/api/referrals', {
                refereeEmail: refereeEmail.trim(),
                refereeName: refereeName.trim() || null
            }, {
                headers: { 'X-User-Id': user?.sub }
            });

            toast.success('Referral submitted! You\'ll get a free month when your friend signs up.');
            setRefereeEmail('');
            setRefereeName('');
            fetchReferrals();
        } catch (error: any) {
            const message = error.response?.data?.error || 'Failed to submit referral';
            toast.error(message);
        } finally {
            setSubmitting(false);
        }
    };

    const getStatusBadge = (status: Referral['status']) => {
        switch (status) {
            case 'PENDING':
                return (
                    <Badge variant="outline" className="flex items-center gap-1">
                        <Clock className="h-3 w-3" />
                        Pending
                    </Badge>
                );
            case 'SIGNED_UP':
                return (
                    <Badge variant="secondary" className="flex items-center gap-1">
                        <AlertCircle className="h-3 w-3" />
                        Signed Up
                    </Badge>
                );
            case 'DISCOUNT_APPLIED':
                return (
                    <Badge variant="default" className="flex items-center gap-1 bg-green-600">
                        <CheckCircle className="h-3 w-3" />
                        Discount Applied
                    </Badge>
                );
        }
    };

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString('en-AU', {
            day: 'numeric',
            month: 'short',
            year: 'numeric'
        });
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
                <h1 className="text-3xl font-bold tracking-tight">Refer a Friend</h1>
                <p className="text-muted-foreground mt-2">
                    Share Ottero with friends and get a free month when they sign up!
                </p>
            </div>

            <div className="space-y-6">
                {/* How it Works */}
                <Card>
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                            <Gift className="h-5 w-5" />
                            How It Works
                        </CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                            <div className="text-center">
                                <div className="bg-primary/10 rounded-full w-12 h-12 flex items-center justify-center mx-auto mb-3">
                                    <span className="text-lg font-bold text-primary">1</span>
                                </div>
                                <h3 className="font-medium mb-1">Enter Friend's Email</h3>
                                <p className="text-sm text-muted-foreground">
                                    Submit your friend's email address below
                                </p>
                            </div>
                            <div className="text-center">
                                <div className="bg-primary/10 rounded-full w-12 h-12 flex items-center justify-center mx-auto mb-3">
                                    <span className="text-lg font-bold text-primary">2</span>
                                </div>
                                <h3 className="font-medium mb-1">Friend Signs Up</h3>
                                <p className="text-sm text-muted-foreground">
                                    When your friend creates an Ottero account
                                </p>
                            </div>
                            <div className="text-center">
                                <div className="bg-primary/10 rounded-full w-12 h-12 flex items-center justify-center mx-auto mb-3">
                                    <span className="text-lg font-bold text-primary">3</span>
                                </div>
                                <h3 className="font-medium mb-1">You Get Free Month</h3>
                                <p className="text-sm text-muted-foreground">
                                    100% off your next billing cycle!
                                </p>
                            </div>
                        </div>
                    </CardContent>
                </Card>

                {/* Referral Form */}
                <Card>
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                            <Users className="h-5 w-5" />
                            Refer a Friend
                        </CardTitle>
                        <CardDescription>
                            Enter your friend's details to submit a referral
                        </CardDescription>
                    </CardHeader>
                    <CardContent>
                        <form onSubmit={handleSubmit} className="space-y-4">
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <Label htmlFor="refereeEmail">Friend's Email *</Label>
                                    <div className="relative">
                                        <Mail className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                                        <Input
                                            id="refereeEmail"
                                            type="email"
                                            placeholder="friend@example.com"
                                            value={refereeEmail}
                                            onChange={(e) => setRefereeEmail(e.target.value)}
                                            className="pl-10"
                                            required
                                        />
                                    </div>
                                </div>
                                <div className="space-y-2">
                                    <Label htmlFor="refereeName">Friend's Name (Optional)</Label>
                                    <Input
                                        id="refereeName"
                                        type="text"
                                        placeholder="John Smith"
                                        value={refereeName}
                                        onChange={(e) => setRefereeName(e.target.value)}
                                    />
                                </div>
                            </div>
                            <Button type="submit" disabled={submitting} className="w-full md:w-auto">
                                {submitting ? 'Submitting...' : 'Submit Referral'}
                            </Button>
                        </form>
                    </CardContent>
                </Card>

                {/* Referral History */}
                <Card>
                    <CardHeader>
                        <CardTitle>Your Referrals</CardTitle>
                        <CardDescription>
                            Track the status of your referrals
                        </CardDescription>
                    </CardHeader>
                    <CardContent>
                        {referrals.length === 0 ? (
                            <div className="text-center py-8 text-muted-foreground">
                                <Users className="h-12 w-12 mx-auto mb-3 opacity-50" />
                                <p>No referrals yet. Start referring friends to earn free months!</p>
                            </div>
                        ) : (
                            <div className="space-y-4">
                                {referrals.map((referral, index) => (
                                    <div key={referral.referralCode}>
                                        {index > 0 && <Separator className="my-4" />}
                                        <div className="flex flex-col md:flex-row md:items-center justify-between gap-3">
                                            <div>
                                                <p className="font-medium">
                                                    {referral.refereeName || referral.refereeEmail}
                                                </p>
                                                {referral.refereeName && (
                                                    <p className="text-sm text-muted-foreground">{referral.refereeEmail}</p>
                                                )}
                                                <p className="text-xs text-muted-foreground mt-1">
                                                    Referred on {formatDate(referral.createdDate)}
                                                </p>
                                            </div>
                                            <div className="flex items-center gap-3">
                                                {getStatusBadge(referral.status)}
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
