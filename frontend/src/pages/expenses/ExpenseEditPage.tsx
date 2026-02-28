import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { useAppStore } from '@/store/useAppStore';
import { useExpense, useDeleteExpense } from '@/hooks/useExpenses';
import { useExpenseForm } from '@/hooks/useExpenseForm';
import { useApi } from '@/hooks/useApi';
import { toast } from 'sonner';
import { Loader2, ArrowLeft, Save, Trash, Paperclip, Download, X, Lock, Sparkles } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Switch } from '@/components/ui/switch';
import {
    Form,
    FormControl,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from '@/components/ui/form';
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { FileUpload } from '@/components/FileUpload';
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from '@/components/ui/alert-dialog';

import { EXPENSE_CATEGORY_LABELS, type ExpenseCategory } from '@/types';

interface UserProfile {
    subscriptionPlan: string;
}

export default function ExpenseEditPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { user } = useAuth0();
    const { selectedCompanyId } = useAppStore();
    const api = useApi();
    const isNew = id === 'new' || !id;

    const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
    const [isSaving, setIsSaving] = useState(false);
    const [profile, setProfile] = useState<UserProfile | null>(null);
    const [profileLoading, setProfileLoading] = useState(true);
    const { data: expense, isLoading, refetch } = useExpense(id || '', selectedCompanyId);
    const { form } = useExpenseForm();
    const deleteExpense = useDeleteExpense();

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
            } finally {
                setProfileLoading(false);
            }
        };
        if (user?.sub) fetchProfile();
    }, [user?.sub]);

    useEffect(() => {
        if (expense) {
            form.reset({
                ...expense,
                expenseDate: expense.expenseDate?.substring(0, 10),
                gstAmount: expense.gstAmount || 0,
            });
        }
    }, [expense, form]);

    const onSubmit = async (data: Record<string, unknown>) => {
        if (!selectedCompanyId) return;

        try {
            setIsSaving(true);
            const payload = { ...data, companyId: selectedCompanyId };

            if (isNew) {
                const response = await api.post(`/api/companies/${selectedCompanyId}/expenses`, payload);
                toast.success('Expense created successfully');
                navigate(`/expenses/${response.data.id}`);
            } else {
                await api.put(`/api/companies/${selectedCompanyId}/expenses/${id}`, payload);
                toast.success('Expense updated successfully');
                refetch();
            }
        } catch (error: unknown) {
            console.error(error);
            const errorMessage = (error as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Failed to save expense';
            toast.error(errorMessage);
        } finally {
            setIsSaving(false);
        }
    };

    const handleDelete = async () => {
        if (!selectedCompanyId || !id) return;

        try {
            await deleteExpense.mutateAsync({ id, companyId: selectedCompanyId });
            toast.success('Expense deleted');
            navigate('/expenses');
        } catch {
            toast.error('Failed to delete expense');
        }
    };

    const handleDeleteAttachment = async (attachmentId: string) => {
        if (!selectedCompanyId || !id) return;

        try {
            await api.delete(`/api/companies/${selectedCompanyId}/expenses/${id}/attachments/${attachmentId}`);
            toast.success('Attachment deleted');
            refetch();
        } catch {
            toast.error('Failed to delete attachment');
        }
    };

    if (!selectedCompanyId) {
        return (
            <div className="container mx-auto py-10 text-center">
                <p>Please select a company first.</p>
            </div>
        );
    }

    if (profileLoading) {
        return (
            <div className="container mx-auto py-10 flex justify-center">
                <Loader2 className="h-8 w-8 animate-spin" />
            </div>
        );
    }

    // Require Advanced plan for creating/editing expenses
    if (!isAdvancedPlan) {
        return (
            <div className="container mx-auto py-10 max-w-2xl">
                <Card className="border-amber-200 bg-amber-50 dark:bg-amber-950/20 dark:border-amber-800">
                    <CardHeader className="text-center">
                        <div className="mx-auto mb-4 h-12 w-12 rounded-full bg-amber-100 dark:bg-amber-900 flex items-center justify-center">
                            <Lock className="h-6 w-6 text-amber-600" />
                        </div>
                        <CardTitle className="text-2xl text-amber-900 dark:text-amber-100">
                            Advanced Plan Required
                        </CardTitle>
                    </CardHeader>
                    <CardContent className="text-center space-y-4">
                        <p className="text-amber-700 dark:text-amber-300">
                            Expense Management is available exclusively on the Advanced plan.
                            Upgrade to track business expenses, upload receipt photos, and prepare for tax time.
                        </p>
                        <div className="flex justify-center gap-4 pt-4">
                            <Button variant="outline" onClick={() => navigate('/expenses')}>
                                <ArrowLeft className="mr-2 h-4 w-4" />
                                Back to Expenses
                            </Button>
                            <Button onClick={() => navigate('/settings/pricing')} className="bg-amber-600 hover:bg-amber-700">
                                <Sparkles className="mr-2 h-4 w-4" />
                                View Plans & Pricing
                            </Button>
                        </div>
                    </CardContent>
                </Card>
            </div>
        );
    }

    if (isLoading && !isNew) {
        return (
            <div className="container mx-auto py-10 flex justify-center">
                <Loader2 className="h-8 w-8 animate-spin" />
            </div>
        );
    }

    return (
        <div className="container mx-auto py-6 max-w-4xl space-y-8">
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                    <Button variant="ghost" size="icon" onClick={() => navigate('/expenses')}>
                        <ArrowLeft className="h-4 w-4" />
                    </Button>
                    <h1 className="text-3xl font-bold">{isNew ? 'New Expense' : 'Edit Expense'}</h1>
                </div>
                <Button type="button" onClick={form.handleSubmit(onSubmit)} disabled={isSaving}>
                    {isSaving ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <Save className="mr-2 h-4 w-4" />}
                    Save
                </Button>
            </div>

            <Form {...form}>
                <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-8">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        {/* Expense Details */}
                        <Card>
                            <CardHeader>
                                <CardTitle>Expense Details</CardTitle>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                <FormField
                                    control={form.control}
                                    name="expenseDate"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>
                                                Date <span className="text-destructive">*</span>
                                            </FormLabel>
                                            <FormControl>
                                                <Input type="date" {...field} />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name="vendor"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>
                                                Vendor <span className="text-destructive">*</span>
                                            </FormLabel>
                                            <FormControl>
                                                <Input placeholder="e.g., Officeworks" {...field} />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name="category"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>
                                                Category <span className="text-destructive">*</span>
                                            </FormLabel>
                                            <Select onValueChange={field.onChange} value={field.value}>
                                                <FormControl>
                                                    <SelectTrigger>
                                                        <SelectValue placeholder="Select category" />
                                                    </SelectTrigger>
                                                </FormControl>
                                                <SelectContent>
                                                    {Object.entries(EXPENSE_CATEGORY_LABELS).map(([key, label]) => (
                                                        <SelectItem key={key} value={key}>
                                                            {label}
                                                        </SelectItem>
                                                    ))}
                                                </SelectContent>
                                            </Select>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name="referenceNumber"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Reference/Receipt Number</FormLabel>
                                            <FormControl>
                                                <Input placeholder="Optional" {...field} />
                                            </FormControl>
                                        </FormItem>
                                    )}
                                />
                            </CardContent>
                        </Card>

                        {/* Amount & Tax */}
                        <Card>
                            <CardHeader>
                                <CardTitle>Amount & Tax</CardTitle>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                <FormField
                                    control={form.control}
                                    name="amount"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>
                                                Total Amount (Inc GST) <span className="text-destructive">*</span>
                                            </FormLabel>
                                            <FormControl>
                                                <Input type="number" step="0.01" min="0" {...field} />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name="gstClaimable"
                                    render={({ field }) => (
                                        <FormItem className="flex flex-row items-center justify-between rounded-lg border p-4">
                                            <div className="space-y-0.5">
                                                <FormLabel className="text-base">GST Claimable</FormLabel>
                                                <p className="text-sm text-muted-foreground">
                                                    Can you claim GST on this expense?
                                                </p>
                                            </div>
                                            <FormControl>
                                                <Switch checked={field.value} onCheckedChange={field.onChange} />
                                            </FormControl>
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name="taxDeductible"
                                    render={({ field }) => (
                                        <FormItem className="flex flex-row items-center justify-between rounded-lg border p-4">
                                            <div className="space-y-0.5">
                                                <FormLabel className="text-base">Tax Deductible</FormLabel>
                                                <p className="text-sm text-muted-foreground">
                                                    Is this a tax-deductible expense?
                                                </p>
                                            </div>
                                            <FormControl>
                                                <Switch checked={field.value} onCheckedChange={field.onChange} />
                                            </FormControl>
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name="paymentMethod"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Payment Method</FormLabel>
                                            <Select onValueChange={field.onChange} value={field.value || ''}>
                                                <FormControl>
                                                    <SelectTrigger>
                                                        <SelectValue placeholder="Select method" />
                                                    </SelectTrigger>
                                                </FormControl>
                                                <SelectContent>
                                                    <SelectItem value="CARD">Credit/Debit Card</SelectItem>
                                                    <SelectItem value="CASH">Cash</SelectItem>
                                                    <SelectItem value="BANK_TRANSFER">Bank Transfer</SelectItem>
                                                    <SelectItem value="PAYPAL">PayPal</SelectItem>
                                                    <SelectItem value="OTHER">Other</SelectItem>
                                                </SelectContent>
                                            </Select>
                                        </FormItem>
                                    )}
                                />
                            </CardContent>
                        </Card>
                    </div>

                    {/* Description & Notes */}
                    <Card>
                        <CardHeader>
                            <CardTitle>Description & Notes</CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            <FormField
                                control={form.control}
                                name="expenseDescription"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Description</FormLabel>
                                        <FormControl>
                                            <Input placeholder="Brief description of the expense" {...field} />
                                        </FormControl>
                                    </FormItem>
                                )}
                            />
                            <FormField
                                control={form.control}
                                name="notes"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Notes</FormLabel>
                                        <FormControl>
                                            <Textarea
                                                placeholder="Additional notes for tax purposes..."
                                                {...field}
                                            />
                                        </FormControl>
                                    </FormItem>
                                )}
                            />
                        </CardContent>
                    </Card>

                    {/* Receipt Upload - Only show for existing expenses */}
                    {!isNew && (
                        <Card>
                            <CardHeader>
                                <CardTitle className="flex items-center gap-2">
                                    <Paperclip className="h-5 w-5" />
                                    Receipt Attachments
                                </CardTitle>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                <FileUpload
                                    uploadUrl={`/api/companies/${selectedCompanyId}/expenses/${id}/attachments`}
                                    accept="image/*,application/pdf"
                                    maxSizeMB={10}
                                    uploadLabel="Upload Receipt"
                                    onUploadSuccess={() => {
                                        refetch();
                                        toast.success('Receipt uploaded');
                                    }}
                                />
                                {expense?.attachments && expense.attachments.length > 0 && (
                                    <div className="space-y-2 mt-4">
                                        <p className="text-sm font-medium">Uploaded Receipts:</p>
                                        {expense.attachments.map((att) => (
                                            <div
                                                key={att.id}
                                                className="flex items-center justify-between p-3 border rounded-lg"
                                            >
                                                <span className="text-sm truncate flex-1">{att.fileName}</span>
                                                <div className="flex gap-2 ml-2">
                                                    <Button variant="ghost" size="sm" asChild>
                                                        <a
                                                            href={`/api/companies/${selectedCompanyId}/expenses/${id}/attachments/${att.id}`}
                                                            target="_blank"
                                                            rel="noopener noreferrer"
                                                        >
                                                            <Download className="h-4 w-4" />
                                                        </a>
                                                    </Button>
                                                    <Button
                                                        variant="ghost"
                                                        size="sm"
                                                        onClick={() => handleDeleteAttachment(att.id)}
                                                    >
                                                        <X className="h-4 w-4 text-destructive" />
                                                    </Button>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </CardContent>
                        </Card>
                    )}
                </form>
            </Form>

            {/* Delete Action */}
            {!isNew && (
                <div className="pt-8 border-t">
                    <Button variant="destructive" onClick={() => setIsDeleteDialogOpen(true)}>
                        <Trash className="mr-2 h-4 w-4" /> Delete Expense
                    </Button>
                </div>
            )}

            <AlertDialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>Delete Expense?</AlertDialogTitle>
                        <AlertDialogDescription>
                            This action cannot be undone. The expense and all its attachments will be
                            permanently deleted.
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel>Cancel</AlertDialogCancel>
                        <AlertDialogAction onClick={handleDelete} className="bg-destructive hover:bg-destructive/90">
                            Delete
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>
        </div>
    );
}
