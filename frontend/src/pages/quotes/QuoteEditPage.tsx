import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAppStore } from '@/store/useAppStore';
import { useQuote } from '@/hooks/useQuotes';
import { useQuoteForm } from '@/hooks/useQuoteForm';
import { useApi } from '@/hooks/useApi'; // For manual mutations (save/delete)
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
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
import { Separator } from '@/components/ui/separator';
import QuoteLineItems from './QuoteLineItems';
import { Badge } from '@/components/ui/badge';
import { Loader2, ArrowLeft, Save, Trash, Download, Link } from 'lucide-react';
import { toast } from 'sonner';
import { CustomerSearch } from '@/components/quote/CustomerSearch';
import { QuoteAttachments } from '@/components/quote/QuoteAttachments';
import { QUOTE_STATUS_LABELS, getQuoteStatusColor } from '@/lib/quote-status';

export default function QuoteEditPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { selectedCompanyId } = useAppStore();
    const api = useApi();
    const isNew = id === 'new' || !id;

    // Fetch existing quote data
    const { data: quote, isLoading: isQuoteLoading } = useQuote(id || '', selectedCompanyId);

    // Initialize form
    const { form } = useQuoteForm();

    const [isCancelDialogOpen, setIsCancelDialogOpen] = useState(false);

    const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
    const [isLinkConfirmOpen, setIsLinkConfirmOpen] = useState(false);

    // Read only logic: Any state that is "Finalized"
    const status = form.watch("status");
    const isReadOnly = status === 'CANCELLED' || status === 'ACCEPTED' || status === 'REJECTED' || status === 'SENT';

    // Populate form when data loads
    useEffect(() => {
        if (quote) {
            form.reset({
                ...quote,
                // Ensure dates are strings YYYY-MM-DD
                quoteDate: quote.quoteDate?.substring(0, 10),
                expiryDate: quote.expiryDate?.substring(0, 10),
            });
        }
    }, [quote, form]);

    // Ensure companyId is in the form for validation
    useEffect(() => {
        if (selectedCompanyId) {
            form.setValue('companyId', String(selectedCompanyId));
        }
    }, [selectedCompanyId, form]);

    // Recalculate totals when items change
    const quoteItems = form.watch('quoteItems');
    const discountType = form.watch('discountType');
    const discountValue = form.watch('discountValue');

    useEffect(() => {
        let subTotal = 0;
        let totalGst = 0;

        quoteItems?.forEach(item => {
            const qty = Number(item.quantity) || 0;
            const price = Number(item.price) || 0;
            const gstRate = Number(item.gst) === 10 ? 0.1 : 0;

            let lineTotal = qty * price;
            // No GST calculation on line item total for subtotal ex GST
            subTotal += lineTotal;

            if (gstRate > 0) {
                totalGst += lineTotal * 0.1;
            }
        });

        let grandTotal = subTotal + totalGst;

        // Apply discount
        const discountVal = Number(discountValue) || 0;
        if (discountType === 'DOLLAR') {
            grandTotal -= discountVal;
        } else if (discountType === 'PERCENT') {
            grandTotal -= grandTotal * (discountVal / 100);
        }

        // Avoid negative total
        if (grandTotal < 0) grandTotal = 0;

        form.setValue('totalPrice', Number(grandTotal.toFixed(2)));
        form.setValue('gst', Number(totalGst.toFixed(2)));

    }, [JSON.stringify(quoteItems), discountType, discountValue, form]);

    const onSubmit = async (data: any) => {
        if (!selectedCompanyId) return;

        try {
            const payload = { ...data, companyId: selectedCompanyId };

            if (isNew) {
                const response = await api.post(`/api/companies/${selectedCompanyId}/quotes`, payload);
                toast.success("Quote created successfully");
                // Navigate to the edit page for the newly created quote
                navigate(`/quotes/${response.data.id}`);
            } else {
                await api.put(`/api/companies/${selectedCompanyId}/quotes/${id}`, payload);
                toast.success("Quote updated successfully");
                // Stay on the same page - no navigation
            }
        } catch (error: any) {
            console.error(error);
            const errorMessage = typeof error?.response?.data === 'string'
                ? error.response.data
                : error?.response?.data?.message || "Failed to save quote";

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

    const handleCancel = async () => {
        if (!selectedCompanyId || !id) return;

        try {
            await api.put(`/api/companies/${selectedCompanyId}/quotes/${id}`, { ...quote, status: 'CANCELLED' });
            toast.success("Quote cancelled");
            // Update local state by forcing a refresh or manually setting form
            form.setValue('status', 'CANCELLED');
            setIsCancelDialogOpen(false);
        } catch (error) {
            console.error(error);
            toast.error("Failed to cancel quote");
        }
    }

    const handleDelete = async () => {
        if (!selectedCompanyId || !id) return;

        try {
            await api.delete(`/api/companies/${selectedCompanyId}/quotes/${id}`);
            toast.success("Quote deleted");
            navigate('/quotes');
        } catch (error) {
            toast.error("Failed to delete quote");
        }
    }

    const handleCopyLink = async () => {
        if (!selectedCompanyId || !id) return;
        try {
            const response = await api.get(`/api/companies/${selectedCompanyId}/quotes/${id}/public-link`);
            await navigator.clipboard.writeText(response.data);
            toast.success("Public link copied to clipboard");
            setIsLinkConfirmOpen(false);
        } catch (error) {
            console.error(error);
            toast.error("Failed to generate public link");
        }
    };

    const handleDownloadPdf = async () => {
        if (!selectedCompanyId || isNew || !id) return;
        try {
            const response = await api.get(`/api/companies/${selectedCompanyId}/quotes/${id}/pdf`, {
                responseType: 'blob',
            });

            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', `quote-${quote?.quoteNumber || id}.pdf`);
            document.body.appendChild(link);
            link.click();
            link.remove();
        } catch (error) {
            console.error(error);
            toast.error("Failed to download PDF");
        }
    };

    if (!selectedCompanyId) return <div>Please select a company first.</div>;
    if (isQuoteLoading) return <div><Loader2 className="animate-spin" /> Loading...</div>;

    const watchedTotal = form.watch("totalPrice") || 0;
    const watchedGst = form.watch("gst") || 0;

    return (
        <div className="container mx-auto py-6 max-w-5xl space-y-8">
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                    <Button variant="ghost" size="icon" onClick={() => navigate('/quotes')}>
                        <ArrowLeft className="h-4 w-4" />
                    </Button>
                    <div className="flex items-center gap-3">
                        <h1 className="text-3xl font-bold">{isNew ? 'New Quote' : `Quote #${quote?.quoteNumber || ''}${quote?.quoteRevision && quote.quoteRevision > 0 ? ` (Rev ${quote.quoteRevision})` : ''}`}</h1>
                        {!isNew && status && (
                            <Badge variant={getQuoteStatusColor(status) as any}>
                                {QUOTE_STATUS_LABELS[status] || status}
                            </Badge>
                        )}
                    </div>
                    {/* Related Invoices Badge ... */}
                </div>
                <div className="flex items-center gap-2">
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
                                        }}
                                    />
                                </div>
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-4">
                                    {/* ... Hidden Client ID ... */}
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
                                                    <Input {...field} required disabled={isReadOnly} />
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
                                                    <Input {...field} required disabled={isReadOnly} />
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
                                <CardTitle>Quote Details</CardTitle>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                <FormField
                                    control={form.control}
                                    name="quoteDate"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Date</FormLabel>
                                            <FormControl>
                                                <Input type="date" {...field} disabled={isReadOnly} />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name="expiryDate"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Expiry Date</FormLabel>
                                            <FormControl>
                                                <Input type="date" {...field} disabled={isReadOnly} />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />

                                {/* Removed Status Field - It is now in the header */}


                            </CardContent>
                        </Card>

                    </div>

                    <QuoteLineItems form={form} disabled={isReadOnly} />

                    <div className="grid grid-cols-1 md:grid-cols-12 gap-6">
                        <div className="md:col-span-8 space-y-6">
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

                            <QuoteAttachments companyId={String(selectedCompanyId)} quoteId={id || ''} />
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
                                                        <Input type="number" {...field} min="0" disabled={isReadOnly} />
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
                                {status === 'PENDING' && (
                                    <Button variant="outline" onClick={async () => {
                                        try {
                                            await api.post(`/api/companies/${selectedCompanyId}/quotes/${id}/send-quote`);
                                            toast.success("Quote sent to customer successfully");
                                        } catch (e) {
                                            toast.error("Failed to send quote");
                                        }
                                    }} type="button">
                                        Send to Customer
                                    </Button>
                                )}
                                {status !== 'CANCELLED' && (
                                    <Button variant="outline" onClick={async () => {
                                        if (quote?.relatedInvoices && quote.relatedInvoices.length > 0) {
                                            if (!confirm("This quote has already been invoiced. Do you want to create another invoice?")) return;
                                        }
                                        try {
                                            const { data: invoice } = await api.get(`/api/companies/${selectedCompanyId}/invoices/quote/${id}`);
                                            toast.success("Converted to Invoice");
                                            navigate(`/invoices/${invoice.id}`);
                                        } catch (e) {
                                            toast.error("Failed to convert");
                                        }
                                    }} type="button">
                                        Convert to Invoice
                                    </Button>
                                )}
                            </div>

                            {/* Secondary Actions */}
                            <div className="flex flex-wrap gap-2">
                                {status !== 'CANCELLED' && (
                                    <Button variant="secondary" onClick={() => setIsCancelDialogOpen(true)} type="button">
                                        Cancel Quote
                                    </Button>
                                )}
                                {(status === 'PENDING' || status === 'SENT' || status === 'REJECTED' || status === 'ACCEPTED') && (
                                    <Button variant="outline" onClick={async () => {
                                        try {
                                            const { data: newQuote } = await api.post(`/api/companies/${selectedCompanyId}/quotes/${id}/revise`);
                                            toast.success("Quote revision created successfully");
                                            navigate(`/quotes/${newQuote.id}`);
                                        } catch (e) {
                                            console.error(e);
                                            toast.error("Failed to create revision");
                                        }
                                    }} type="button" title="Create a new version (e.g. Rev 2) of this same quote number. Best for small changes.">
                                        Add Revision
                                    </Button>
                                )}
                                <Button variant="outline" onClick={async () => {
                                    try {
                                        const { data: newQuote } = await api.post(`/api/companies/${selectedCompanyId}/quotes/${id}/copy`);
                                        toast.success("Quote duplicated successfully");
                                        navigate(`/quotes/${newQuote.id}`);
                                    } catch (e) {
                                        console.error(e);
                                        toast.error("Failed to duplicate quote");
                                    }
                                }} type="button" title="Create a completely new quote with a new quote number. Best for using this as a template.">
                                    Duplicate Quote
                                </Button>
                                <Button variant="destructive" onClick={() => setIsDeleteDialogOpen(true)} type="button">
                                    <Trash className="mr-2 h-4 w-4" />
                                    Delete Quote
                                </Button>
                            </div>
                        </div>
                    )}
                </form>
            </Form>

            <AlertDialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>Are you absolutely sure?</AlertDialogTitle>
                        <AlertDialogDescription>
                            This action cannot be undone. This will permanently delete the quote.
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

            <AlertDialog open={isLinkConfirmOpen} onOpenChange={setIsLinkConfirmOpen}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>Copy Public Link</AlertDialogTitle>
                        <AlertDialogDescription className="space-y-2">
                            <p>This will generate a public-facing link for this quote that anyone can view without logging in.</p>
                            <p><strong>Note:</strong> This is a read-only view. The client can accept or reject the quote.</p>
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel>Cancel</AlertDialogCancel>
                        <AlertDialogAction onClick={handleCopyLink}>
                            Generate & Copy Link
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>

            <AlertDialog open={isCancelDialogOpen} onOpenChange={setIsCancelDialogOpen}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>Cancel Quote?</AlertDialogTitle>
                        <AlertDialogDescription>
                            Are you sure you want to cancel this quote? It will become read-only and not editable.
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel>Keep Quote</AlertDialogCancel>
                        <AlertDialogAction onClick={handleCancel}>
                            Yes, Cancel it
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>
        </div >
    );
}
