import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { useExpenses } from '@/hooks/useExpenses';
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
import { Plus, Search, Receipt, Paperclip, Loader2, Lock, Sparkles } from 'lucide-react';
import { EXPENSE_CATEGORY_LABELS, type ExpenseCategory } from '@/types';

const getStatusVariant = (status: string) => {
    switch (status) {
        case 'APPROVED':
            return 'success';
        case 'REJECTED':
            return 'destructive';
        case 'ARCHIVED':
            return 'secondary';
        default:
            return 'default';
    }
};

interface UserProfile {
    subscriptionPlan: string;
}

export default function ExpenseListPage() {
    const navigate = useNavigate();
    const { user } = useAuth0();
    const api = useApi();
    const { selectedCompanyId } = useAppStore();
    const [page, setPage] = useState(0);
    const [searchTerm, setSearchTerm] = useState('');
    const [profile, setProfile] = useState<UserProfile | null>(null);
    const debouncedSearchTerm = useDebounce(searchTerm, 500);

    const isAdvancedPlan = profile?.subscriptionPlan?.toLowerCase() === 'advanced';

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const response = await api.get('/api/users/profile', {
                    headers: { 'X-User-Id': user?.sub }
                });
                setProfile(response.data);
            } catch (error) {
                console.error('Failed to fetch profile', error);
            }
        };
        if (user?.sub) fetchProfile();
    }, [user?.sub]);

    const { data, isLoading } = useExpenses({
        page,
        size: 10,
        searchTerm: debouncedSearchTerm,
        companyId: selectedCompanyId,
    });

    const formatCurrency = (amount: number | undefined) => {
        if (amount === undefined || amount === null) return '$0.00';
        return `$${amount.toFixed(2)}`;
    };

    return (
        <div className="container mx-auto py-10">
            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Expenses</h1>
                    <p className="text-muted-foreground mt-1">
                        Track business expenses for tax preparation.
                    </p>
                </div>
                {isAdvancedPlan && (
                    <Button onClick={() => navigate('/expenses/new')}>
                        <Plus className="mr-2 h-4 w-4" />
                        New Expense
                    </Button>
                )}
            </div>

            {!isAdvancedPlan && profile && (
                <Card className="mb-6 border-amber-200 bg-amber-50 dark:bg-amber-950/20 dark:border-amber-800">
                    <CardContent className="flex items-center justify-between py-4">
                        <div className="flex items-center gap-3">
                            <Sparkles className="h-5 w-5 text-amber-600" />
                            <div>
                                <p className="font-medium text-amber-900 dark:text-amber-100">
                                    Expense Management is an Advanced plan feature
                                </p>
                                <p className="text-sm text-amber-700 dark:text-amber-300">
                                    Upgrade to track expenses, upload receipts, and prepare for tax time.
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
                    placeholder="Search by vendor or description..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="pl-9"
                />
            </div>

            <div className="border rounded-md">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>Date</TableHead>
                            <TableHead>Vendor</TableHead>
                            <TableHead>Category</TableHead>
                            <TableHead>Status</TableHead>
                            <TableHead className="text-right">Amount</TableHead>
                            <TableHead className="text-right">GST</TableHead>
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
                                    No expenses found.
                                </TableCell>
                            </TableRow>
                        ) : (
                            data.content.map((expense) => (
                                <TableRow key={expense.id}>
                                    <TableCell>{expense.expenseDate}</TableCell>
                                    <TableCell className="font-medium">
                                        <Link
                                            to={`/expenses/${expense.id}`}
                                            className="hover:underline flex items-center gap-2"
                                        >
                                            <Receipt className="h-4 w-4 text-muted-foreground" />
                                            {expense.vendor}
                                            {expense.attachments && expense.attachments.length > 0 && (
                                                <Paperclip className="h-3 w-3 text-blue-500" />
                                            )}
                                        </Link>
                                    </TableCell>
                                    <TableCell>
                                        {EXPENSE_CATEGORY_LABELS[expense.category as ExpenseCategory] || expense.category}
                                    </TableCell>
                                    <TableCell>
                                        <Badge variant={getStatusVariant(expense.status) as 'default' | 'secondary' | 'destructive' | 'outline'}>
                                            {expense.status}
                                        </Badge>
                                    </TableCell>
                                    <TableCell className="text-right">
                                        {formatCurrency(expense.amount)}
                                    </TableCell>
                                    <TableCell className="text-right">
                                        {formatCurrency(expense.gstAmount)}
                                    </TableCell>
                                    <TableCell className="text-right">
                                        <Button variant="ghost" size="sm" asChild>
                                            <Link to={`/expenses/${expense.id}`}>Edit</Link>
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
