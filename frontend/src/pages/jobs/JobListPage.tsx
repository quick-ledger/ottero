import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { useJobs } from '@/hooks/useJobs';
import { useAppStore } from '@/store/useAppStore';
import { useDebounce } from '@/hooks/useDebounce';
import { useApi } from '@/hooks/useApi';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table';
import { Pagination } from '@/components/ui/pagination-controls';
import { Plus, Search, Briefcase, MapPin, Loader2, Sparkles } from 'lucide-react';
import { JOB_STATUS_LABELS, type JobStatus } from '@/types';

const getStatusVariant = (status: JobStatus): 'default' | 'secondary' | 'destructive' | 'outline' => {
    switch (status) {
        case 'COMPLETED':
            return 'default';
        case 'IN_PROGRESS':
            return 'secondary';
        case 'ON_HOLD':
            return 'outline';
        case 'CANCELLED':
            return 'destructive';
        default:
            return 'outline';
    }
};

interface UserProfile {
    subscriptionPlan: string;
}

export default function JobListPage() {
    const navigate = useNavigate();
    const { user } = useAuth0();
    const api = useApi();
    const { selectedCompanyId } = useAppStore();
    const [page, setPage] = useState(0);
    const [searchTerm, setSearchTerm] = useState('');
    const [profile, setProfile] = useState<UserProfile | null>(null);
    const [profileLoading, setProfileLoading] = useState(true);
    const debouncedSearchTerm = useDebounce(searchTerm, 500);

    const isAdvancedPlan = profile?.subscriptionPlan?.toLowerCase() === 'advanced';

    useEffect(() => {
        const fetchProfile = async () => {
            setProfileLoading(true);
            try {
                const response = await api.get('/api/users/profile', {
                    headers: { 'X-User-Id': user?.sub }
                });
                setProfile(response.data);
            } catch (error) {
                console.error('Failed to fetch profile', error);
            } finally {
                setProfileLoading(false);
            }
        };
        if (user?.sub) {
            fetchProfile();
        } else {
            setProfileLoading(false);
        }
    }, [user?.sub]);

    const { data, isLoading } = useJobs({
        page,
        size: 10,
        searchTerm: debouncedSearchTerm,
        companyId: selectedCompanyId,
    });

    return (
        <div className="container mx-auto py-10">
            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Jobs</h1>
                    <p className="text-muted-foreground mt-1">
                        Manage and track your jobs and work orders.
                    </p>
                </div>
                {isAdvancedPlan && (
                    <Button onClick={() => navigate('/jobs/new')}>
                        <Plus className="mr-2 h-4 w-4" />
                        New Job
                    </Button>
                )}
            </div>

            {!isAdvancedPlan && !profileLoading && (
                <Card className="mb-6 border-amber-200 bg-amber-50 dark:bg-amber-950/20 dark:border-amber-800">
                    <CardContent className="flex items-center justify-between py-4">
                        <div className="flex items-center gap-3">
                            <Sparkles className="h-5 w-5 text-amber-600" />
                            <div>
                                <p className="font-medium text-amber-900 dark:text-amber-100">
                                    Job Management is an Advanced plan feature
                                </p>
                                <p className="text-sm text-amber-700 dark:text-amber-300">
                                    Upgrade to manage jobs, track progress, and link to quotes and invoices.
                                </p>
                            </div>
                        </div>
                        <Button onClick={() => navigate('/settings/pricing')} className="bg-amber-600 hover:bg-amber-700">
                            View Plans
                        </Button>
                    </CardContent>
                </Card>
            )}

            <div className="flex items-center py-4 relative max-w-sm">
                <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                    placeholder="Search by title, job number, or location..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="pl-9"
                />
            </div>

            <div className="border rounded-md">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>Job #</TableHead>
                            <TableHead>Title</TableHead>
                            <TableHead>Customer</TableHead>
                            <TableHead>Location</TableHead>
                            <TableHead>Status</TableHead>
                            <TableHead>Scheduled</TableHead>
                            <TableHead className="text-right">Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {isLoading ? (
                            <TableRow>
                                <TableCell colSpan={7} className="h-24 text-center">
                                    <Loader2 className="h-6 w-6 animate-spin mx-auto" />
                                </TableCell>
                            </TableRow>
                        ) : !data || data.content.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={7} className="h-24 text-center">
                                    No jobs found.
                                </TableCell>
                            </TableRow>
                        ) : (
                            data.content.map((job) => (
                                <TableRow key={job.id}>
                                    <TableCell className="font-mono text-sm">
                                        {job.jobNumber}
                                    </TableCell>
                                    <TableCell className="font-medium">
                                        <Link
                                            to={`/jobs/${job.id}`}
                                            className="hover:underline flex items-center gap-2"
                                        >
                                            <Briefcase className="h-4 w-4 text-muted-foreground" />
                                            {job.title}
                                        </Link>
                                    </TableCell>
                                    <TableCell>
                                        {job.clientName || '-'}
                                    </TableCell>
                                    <TableCell>
                                        {job.location ? (
                                            <span className="flex items-center gap-1">
                                                <MapPin className="h-3 w-3 text-muted-foreground" />
                                                {job.location.length > 30
                                                    ? job.location.substring(0, 30) + '...'
                                                    : job.location}
                                            </span>
                                        ) : (
                                            '-'
                                        )}
                                    </TableCell>
                                    <TableCell>
                                        <Badge variant={getStatusVariant(job.status)}>
                                            {JOB_STATUS_LABELS[job.status]}
                                        </Badge>
                                    </TableCell>
                                    <TableCell>
                                        {job.scheduledDate || '-'}
                                    </TableCell>
                                    <TableCell className="text-right">
                                        <Button variant="ghost" size="sm" asChild>
                                            <Link to={`/jobs/${job.id}`}>Edit</Link>
                                        </Button>
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
            </div>

            {data && data.totalPages > 1 && (
                <div className="mt-4">
                    <Pagination
                        currentPage={data.number}
                        totalPages={data.totalPages}
                        onPageChange={setPage}
                    />
                </div>
            )}
        </div>
    );
}
