import { useState, useEffect } from 'react';
import { Separator } from '@/components/ui/separator';
import InvoiceLineItems from './InvoiceLineItems';
import { Badge } from '@/components/ui/badge';
import { Loader2, ArrowLeft, Save, Trash, Download, Mail, Link, RefreshCw, Sparkles } from 'lucide-react';
import { useAuth0 } from '@auth0/auth0-react';
import { toast } from 'sonner';
import { useParams, useNavigate } from 'react-router-dom';
import { useAppStore } from '@/store/useAppStore';
import { useInvoice } from '@/hooks/useInvoices';
import { useInvoiceForm } from '@/hooks/useInvoiceForm';
import { useApi } from '@/hooks/useApi';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Switch } from '@/components/ui/switch';
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { CustomerSearch } from '@/components/quote/CustomerSearch';
import { INVOICE_STATUS_LABELS } from '@/lib/invoice-status';
interface UserProfile {
    subscriptionPlan: string;
}

export default function InvoiceEditPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { user } = useAuth0();
    const { selectedCompanyId } = useAppStore();
    const api = useApi();
    const isNew = id === 'new' || !id;

    const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
    const [isSendDialogOpen, setIsSendDialogOpen] = useState(false);
    const [isLinkConfirmOpen, setIsLinkConfirmOpen] = useState(false);
    const [profile, setProfile] = useState<UserProfile | null>(null);

    const isAdvancedPlan = profile?.subscriptionPlan?.toLowerCase() === 'advanced';

    const { data: invoice, isLoading: isInvoiceLoading } = useInvoice(id || '', selectedCompanyId);
    const { form } = useInvoiceForm();

    const [isVoidDialogOpen, setIsVoidDialogOpen] = useState(false);

    const status = form.watch("status");
    const isReadOnly = status === 'PAID' || status === 'CANCELLED' || status === 'SENT';

    // Fetch user profile for plan checking
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

    useEffect(() => {
        if (invoice) {
            form.reset({
                ...invoice,
                clientEntityName: invoice.clientEntityName || '',
                clientEmail: invoice.clientEmail || '',
                clientPhone: invoice.clientPhone || '',
                notes: invoice.notes || '',
                status: invoice.status,
                issueDate: invoice.issueDate?.substring(0, 10),
                dueDate: invoice.dueDate?.substring(0, 10),
                isRecurring: invoice.isRecurring || false,
                recurringFrequency: invoice.recurringFrequency || 'MONTHLY',
                recurringEndDate: invoice.recurringEndDate?.substring(0, 10) || '',
                recurringAutoSend: invoice.recurringAutoSend || false,
                nextRecurringDate: invoice.nextRecurringDate?.substring(0, 10) || '',
            });
        }
    }, [invoice, form]);

    const onSubmit = async (data: any) => {
        if (!selectedCompanyId) return;

        try {
            const payload = { ...data, companyId: selectedCompanyId };

            if (isNew) {
                await api.post(`/api/companies/${selectedCompanyId}/invoices`, payload);
                toast.success("Invoice created successfully");
            } else {
                await api.put(`/api/companies/${selectedCompanyId}/invoices/${id}`, payload);
                toast.success("Invoice updated successfully");
            }
            navigate('/invoices');
        } catch (error: any) {
            console.error(error);
            const errorMessage = typeof error?.response?.data === 'string'
                ? error.response.data
                : error?.response?.data?.message || "Failed to save invoice";

            if (errorMessage.toLowerCase().includes("limit")) {
                toast.error(errorMessage, {
                    action: {
                        label: 'Upgrade',
                        onClick: () => navigate('/settings/pricing'),
                    },
                    duration: 5000,
                });
            } else {
                toast.error(errorMessage);
            }
        }
    };

    const onError = (errors: any) => {
        console.error("Form validation errors:", errors);
        toast.error("Please fill in all required fields");
    };

    const handleDelete = async () => {
        if (!selectedCompanyId || !id) return;

        try {
            await api.delete(`/api/companies/${selectedCompanyId}/invoices/${id}`);
            toast.success("Invoice deleted");
            navigate('/invoices');
        } catch (error) {
            toast.error("Failed to delete invoice");
        }
    }

    const handleVoid = async () => {
        if (!selectedCompanyId || !id) return;
        try {
            await api.put(`/api/companies/${selectedCompanyId}/invoices/${id}`, { ...invoice, status: 'CANCELLED' });
            toast.success("Invoice voided");
            form.setValue('status', 'CANCELLED');
            setIsVoidDialogOpen(false);
        } catch (error) {
            console.error(error);
            toast.error("Failed to void invoice");
        }
    }

    const handleDownloadPdf = async () => {
        if (!selectedCompanyId || !id || isNew) return;
        try {
            const response = await api.get(`/api/companies/${selectedCompanyId}/invoices/${id}/pdf`, {
                responseType: 'blob',
                headers: {
                    'Accept': 'application/pdf'
                }
            });

            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', `invoice_${invoice?.invoiceNumber || id}.pdf`);
            document.body.appendChild(link);
            link.click();
            link.parentNode?.removeChild(link);
        } catch (error) {
            console.error(error);
            toast.error("Failed to download PDF");
        }
    }

    const handleSendInvoice = async () => {
        if (!selectedCompanyId || !id || isNew) return;

        try {
            await api.post(`/api/companies/${selectedCompanyId}/invoices/${id}/send`);
            toast.success("Invoice sent successfully to customer!");
            form.setValue('status', 'SENT');
            setIsSendDialogOpen(false);
        } catch (error: any) {
            console.error(error);
            const errorMessage = error?.response?.data?.message || "Failed to send invoice";
            toast.error(errorMessage);
        }
    }

    const handleCopyLink = async () => {
        if (!selectedCompanyId || !id || isNew) return;
        try {
            const response = await api.get(`/api/companies/${selectedCompanyId}/invoices/${id}/public-link`);
            const link = response.data;
            await navigator.clipboard.writeText(link);
            toast.success("Public link copied to clipboard");
            setIsLinkConfirmOpen(false);
        } catch (error) {
            console.error(error);
            toast.error("Failed to generate/copy public link");
        }
    }

    if (!selectedCompanyId) return <div>Please select a company first.</div>;
    if (isInvoiceLoading) return <div><Loader2 className="animate-spin" /> Loading...</div>;

    const watchedTotal = form.watch("totalPrice") || 0;
    const watchedGst = form.watch("gst") || 0;


    return (
        <div className="container mx-auto py-6 max-w-5xl space-y-8">
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                    <Button variant="ghost" size="icon" onClick={() => navigate('/invoices')}>
                        <ArrowLeft className="h-4 w-4" />
                    </Button>
                    <div className="flex items-center gap-3">
                        <h1 className="text-3xl font-bold">{isNew ? 'New Invoice' : `Invoice #${invoice?.invoiceNumber || ''}`}</h1>
                        {!isNew && status && (
                            <Badge variant={status === 'PAID' ? 'success' : status === 'CANCELLED' ? 'destructive' : status === 'SENT' ? 'info' : 'secondary'}>
                                {INVOICE_STATUS_LABELS[status as keyof typeof INVOICE_STATUS_LABELS] || status}
                            </Badge>
                        )}
                    </div>
                    {invoice?.quoteNumber && (
                        <Badge
                            variant="outline"
                            className="border-blue-500 text-blue-600 bg-blue-50 cursor-pointer hover:bg-blue-100"
                            onClick={() => invoice.quoteId && navigate(`/quotes/${invoice.quoteId}`)}
                        >
                            Source Quote: {invoice.quoteNumber}
                        </Badge>
                    )}
                </div>
                <div className="flex items-center gap-2">

                    {/* Delete removed from header */}

                    <Button type="button" onClick={form.handleSubmit(onSubmit, onError)} disabled={isReadOnly}>
                        <Save className="mr-2 h-4 w-4" />
                        Save
                    </Button>
                </div>
            </div>

            <Form {...form}>
                <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-8">
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                        <Card className="md:col-span-2">
                            <CardHeader>
                                <CardTitle>Customer Details</CardTitle>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                <div className="space-y-2">
                                    <label className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
                                        Search Existing Client
                                    </label>
                                    <CustomerSearch
                                        disabled={isReadOnly}
                                        onSelect={(client) => {
                                            form.setValue('clientId', String(client.id));
                                            form.setValue('clientFirstname', client.firstName);
                                            form.setValue('clientLastname', client.lastName);
                                            form.setValue('clientEntityName', client.entityName || '');
                                            form.setValue('clientEmail', client.email || '');
                                            form.setValue('clientPhone', client.phone || '');
                                        }} />
                                </div>
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-4">
                                    {/* Hidden Client ID field */}
                                    <FormField
                                        control={form.control}
                                        name="clientId"
                                        render={({ field }) => (
                                            <input type="hidden" {...field} value={field.value || ''} />
                                        )}
                                    />
                                    <FormField
                                        control={form.control}
                                        name="clientFirstname"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormLabel>First Name <span className="text-destructive">*</span></FormLabel>
                                                <FormControl>
                                                    <Input {...field} value={field.value || ''} required disabled={isReadOnly} />
                                                </FormControl>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />
                                    <FormField
                                        control={form.control}
                                        name="clientLastname"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormLabel>Last Name <span className="text-destructive">*</span></FormLabel>
                                                <FormControl>
                                                    <Input {...field} value={field.value || ''} required disabled={isReadOnly} />
                                                </FormControl>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />
                                    <FormField
                                        control={form.control}
                                        name="clientEntityName"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormLabel>Company <span className="text-muted-foreground text-xs">(Optional)</span></FormLabel>
                                                <FormControl>
                                                    <Input {...field} value={field.value || ''} disabled={isReadOnly} />
                                                </FormControl>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />
                                    <FormField
                                        control={form.control}
                                        name="clientEmail"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormLabel>Email <span className="text-destructive">*</span></FormLabel>
                                                <FormControl>
                                                    <Input {...field} value={field.value || ''} required disabled={isReadOnly} />
                                                </FormControl>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />
                                    <FormField
                                        control={form.control}
                                        name="clientPhone"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormLabel>Phone <span className="text-destructive">*</span></FormLabel>
                                                <FormControl>
                                                    <Input {...field} value={field.value || ''} required disabled={isReadOnly} />
                                                </FormControl>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />
                                </div>
                            </CardContent>
                        </Card>

                        <Card>
                            <CardHeader>
                                <CardTitle>Invoice Details</CardTitle>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                <FormField
                                    control={form.control}
                                    name="issueDate"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Issue Date</FormLabel>
                                            <FormControl>
                                                <Input type="date" {...field} value={field.value || ''} disabled={isReadOnly} />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name="dueDate"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Due Date</FormLabel>
                                            <FormControl>
                                                <Input type="date" {...field} value={field.value || ''} disabled={isReadOnly} />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                                {/* Status field removed */}
                            </CardContent>
                        </Card>
                    </div>

                    {/* Recurring Settings Card */}
                    <Card>
                        <CardHeader>
                            <CardTitle className="flex items-center gap-2">
                                <RefreshCw className="h-5 w-5" />
                                Recurring Settings
                                {!isAdvancedPlan && profile && (
                                    <Badge variant="secondary" className="ml-2 text-xs">Advanced Plan</Badge>
                                )}
                            </CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            {!isAdvancedPlan && profile && (
                                <div className="flex items-center justify-between rounded-lg border border-amber-200 bg-amber-50 dark:bg-amber-950/20 dark:border-amber-800 p-4">
                                    <div className="flex items-center gap-3">
                                        <Sparkles className="h-5 w-5 text-amber-600" />
                                        <div>
                                            <p className="font-medium text-amber-800 dark:text-amber-200">Upgrade to Advanced</p>
                                            <p className="text-sm text-amber-700 dark:text-amber-300">Recurring invoices require the Advanced plan.</p>
                                        </div>
                                    </div>
                                    <Button size="sm" onClick={() => navigate('/settings/pricing')}>
                                        Upgrade
                                    </Button>
                                </div>
                            )}
                            <FormField
                                control={form.control}
                                name="isRecurring"
                                render={({ field }) => (
                                    <FormItem className="flex flex-row items-center justify-between rounded-lg border p-4">
                                        <div className="space-y-0.5">
                                            <FormLabel className="text-base">Enable Recurring</FormLabel>
                                            <p className="text-sm text-muted-foreground">
                                                Automatically generate invoices on a schedule
                                            </p>
                                        </div>
                                        <FormControl>
                                            <Switch
                                                checked={field.value || false}
                                                onCheckedChange={field.onChange}
                                                disabled={isReadOnly || status === 'PAID' || status === 'CANCELLED' || !isAdvancedPlan}
                                            />
                                        </FormControl>
                                    </FormItem>
                                )}
                            />

                            {form.watch('isRecurring') && (
                                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 pt-4">
                                    <FormField
                                        control={form.control}
                                        name="recurringFrequency"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormLabel>Frequency</FormLabel>
                                                <Select onValueChange={field.onChange} value={field.value || 'MONTHLY'} disabled={isReadOnly}>
                                                    <FormControl>
                                                        <SelectTrigger>
                                                            <SelectValue placeholder="Select frequency" />
                                                        </SelectTrigger>
                                                    </FormControl>
                                                    <SelectContent>
                                                        <SelectItem value="WEEKLY">Weekly</SelectItem>
                                                        <SelectItem value="BIWEEKLY">Bi-weekly</SelectItem>
                                                        <SelectItem value="MONTHLY">Monthly</SelectItem>
                                                        <SelectItem value="QUARTERLY">Quarterly</SelectItem>
                                                        <SelectItem value="ANNUALLY">Annually</SelectItem>
                                                    </SelectContent>
                                                </Select>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />

                                    <FormField
                                        control={form.control}
                                        name="recurringEndDate"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormLabel>End Date <span className="text-muted-foreground text-xs">(Optional)</span></FormLabel>
                                                <FormControl>
                                                    <Input type="date" {...field} value={field.value || ''} disabled={isReadOnly} />
                                                </FormControl>
                                                <p className="text-xs text-muted-foreground">Leave empty for indefinite</p>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />

                                    <FormField
                                        control={form.control}
                                        name="recurringAutoSend"
                                        render={({ field }) => (
                                            <FormItem className="flex flex-row items-center justify-between rounded-lg border p-4">
                                                <div className="space-y-0.5">
                                                    <FormLabel>Auto-send</FormLabel>
                                                    <p className="text-xs text-muted-foreground">
                                                        Send to client automatically
                                                    </p>
                                                </div>
                                                <FormControl>
                                                    <Switch
                                                        checked={field.value || false}
                                                        onCheckedChange={field.onChange}
                                                        disabled={isReadOnly}
                                                    />
                                                </FormControl>
                                            </FormItem>
                                        )}
                                    />

                                    {form.watch('nextRecurringDate') && (
                                        <div className="flex flex-col justify-center rounded-lg border p-4 bg-muted/50">
                                            <span className="text-sm font-medium">Next Invoice</span>
                                            <span className="text-lg font-bold">
                                                {new Date(form.watch('nextRecurringDate')!).toLocaleDateString('en-AU', {
                                                    day: 'numeric',
                                                    month: 'short',
                                                    year: 'numeric'
                                                })}
                                            </span>
                                        </div>
                                    )}
                                </div>
                            )}
                        </CardContent>
                    </Card>

                    <InvoiceLineItems form={form} disabled={isReadOnly} />

                    <div className="grid grid-cols-1 md:grid-cols-12 gap-6">
                        <div className="md:col-span-8">
                            <Card>
                                <CardHeader>
                                    <CardTitle>Notes</CardTitle>
                                </CardHeader>
                                <CardContent>
                                    <FormField
                                        control={form.control}
                                        name="notes"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormControl>
                                                    <Textarea placeholder="Add notes here..." className="min-h-[120px]" {...field} value={field.value || ''} />
                                                </FormControl>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />
                                </CardContent>
                            </Card>
                        </div>

                        <div className="md:col-span-4">
                            <Card>
                                <CardContent className="pt-6 space-y-4">
                                    <div className="flex justify-between items-center">
                                        <span className="text-muted-foreground">Subtotal (Ex GST)</span>
                                        <span>${(watchedTotal - watchedGst).toFixed(2)}</span>
                                    </div>
                                    <div className="flex justify-between items-center">
                                        <span className="text-muted-foreground">GST</span>
                                        <span>${watchedGst.toFixed(2)}</span>
                                    </div>

                                    <Separator />

                                    <div className="flex justify-between items-center font-bold text-lg">
                                        <span>Total</span>
                                        <span>${watchedTotal.toFixed(2)}</span>
                                    </div>

                                    <div className="grid grid-cols-2 gap-2 pt-4">
                                        <FormField
                                            control={form.control}
                                            name="discountType"
                                            render={({ field }) => (
                                                <FormItem>
                                                    <Select onValueChange={field.onChange} value={field.value} disabled={isReadOnly}>
                                                        <FormControl>
                                                            <SelectTrigger>
                                                                <SelectValue />
                                                            </SelectTrigger>
                                                        </FormControl>
                                                        <SelectContent>
                                                            <SelectItem value="DOLLAR">Discount ($)</SelectItem>
                                                            <SelectItem value="PERCENT">Discount (%)</SelectItem>
                                                        </SelectContent>
                                                    </Select>
                                                </FormItem>
                                            )}
                                        />
                                        <FormField
                                            control={form.control}
                                            name="discountValue"
                                            render={({ field }) => (
                                                <FormItem>
                                                    <FormControl>
                                                        <Input type="number" {...field} value={field.value ?? ''} min="0" disabled={isReadOnly} />
                                                    </FormControl>
                                                    <FormMessage />
                                                </FormItem>
                                            )}
                                        />
                                    </div>
                                </CardContent>
                            </Card>
                        </div>
                    </div>
                </form>
            </Form>

            {!isNew && (
                <div className="space-y-4 pt-8 border-t mt-8">
                    {/* Primary Actions */}
                    <div className="flex flex-wrap gap-2">
                        <Button variant="outline" onClick={handleDownloadPdf} type="button">
                            <Download className="mr-2 h-4 w-4" />
                            Download PDF
                        </Button>
                        <Button variant="outline" onClick={() => setIsLinkConfirmOpen(true)} type="button">
                            <Link className="mr-2 h-4 w-4" />
                            Copy Public Link
                        </Button>
                        <Button
                            variant="outline"
                            type="button"
                            onClick={() => setIsSendDialogOpen(true)}
                            disabled={form.formState.isDirty}
                            title={form.formState.isDirty ? "Save changes before sending" : "Send to Customer"}
                        >
                            <Mail className="mr-2 h-4 w-4" />
                            Send to Customer
                        </Button>
                    </div>

                    {/* Secondary Actions */}
                    <div className="flex flex-wrap gap-2">
                        {status !== 'CANCELLED' && status !== 'PAID' && (
                            <Button variant="secondary" onClick={() => setIsVoidDialogOpen(true)} type="button">
                                Void Invoice
                            </Button>
                        )}
                        <Button variant="destructive" onClick={() => setIsDeleteDialogOpen(true)} type="button">
                            <Trash className="mr-2 h-4 w-4" />
                            Delete Invoice
                        </Button>
                    </div>
                </div>
            )}

            <AlertDialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>Are you absolutely sure?</AlertDialogTitle>
                        <AlertDialogDescription>
                            This action cannot be undone. This will permanently delete the invoice.
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel>Cancel</AlertDialogCancel>
                        <AlertDialogAction onClick={handleDelete} className="bg-destructive text-destructive-foreground hover:bg-destructive/90">
                            Delete
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>

            <AlertDialog open={isVoidDialogOpen} onOpenChange={setIsVoidDialogOpen}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>Void Invoice?</AlertDialogTitle>
                        <AlertDialogDescription>
                            Are you sure you want to void this invoice? This action cannot be undone and it will be marked as VOID.
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel>Cancel</AlertDialogCancel>
                        <AlertDialogAction onClick={handleVoid} className="bg-destructive text-destructive-foreground hover:bg-destructive/90">
                            Void Invoice
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>

            <AlertDialog open={isSendDialogOpen} onOpenChange={setIsSendDialogOpen}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>Send Invoice to Customer?</AlertDialogTitle>
                        <AlertDialogDescription>
                            This will send an email to <strong>{form.watch('clientEmail') || 'the customer'}</strong> with the invoice PDF attached and a payment link (if Stripe is connected).
                            <br /><br />
                            <strong>Invoice #{invoice?.invoiceNumber}</strong><br />
                            Total: ${invoice?.totalPrice?.toFixed(2) || '0.00'}
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel>Cancel</AlertDialogCancel>
                        <AlertDialogAction onClick={handleSendInvoice} className="bg-primary text-primary-foreground hover:bg-primary/90">
                            Send to Customer
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>

            <AlertDialog open={isLinkConfirmOpen} onOpenChange={setIsLinkConfirmOpen}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>Copy Public Link?</AlertDialogTitle>
                        <AlertDialogDescription>
                            <strong>Warning:</strong> This link grants view access to the invoice without a login. <br /><br />
                            You can send this link to your customer to view and pay the invoice online. Anyone with this link can view the invoice details.
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel>Cancel</AlertDialogCancel>
                        <AlertDialogAction onClick={handleCopyLink} className="bg-blue-600 hover:bg-blue-700 text-white">
                            I understand, Copy Link
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>
        </div>
    );
}
