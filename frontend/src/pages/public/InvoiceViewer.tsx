
import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import axios from 'axios';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { INVOICE_STATUS_LABELS } from '@/lib/invoice-status';
import { CreditCard, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';

interface InvoiceItem {
    itemDescription: string;
    price: number;
    quantity: number;
    gst: number;
    total: number;
}

interface Invoice {
    id: string;
    invoiceNumber: string;
    invoiceDate: string;
    dueDate: string;
    status: keyof typeof INVOICE_STATUS_LABELS;
    clientFirstname: string;
    clientLastname: string;
    clientEntityName?: string;
    subtotal: number; // Assuming Frontend calculates or BE sends it
    gst: number;
    totalPrice: number;
    discountValue: number;
    discountType: 'PERCENT' | 'DOLLAR';
    notes?: string;
    invoiceItems: InvoiceItem[];
    paymentLink?: string;
}

export default function InvoiceViewer() {
    const [searchParams] = useSearchParams();
    const token = searchParams.get('token');

    const [invoice, setInvoice] = useState<Invoice | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        if (!token) {
            setError('Missing access token');
            setLoading(false);
            return;
        }

        const fetchInvoice = async () => {
            try {
                // Use the new public endpoint
                const response = await axios.get<Invoice>(`/api/public/invoices/view?token=${token}`);
                setInvoice(response.data);
            } catch (err) {
                console.error(err);
                setError('Failed to load invoice. The link may have expired.');
            } finally {
                setLoading(false);
            }
        };

        fetchInvoice();
    }, [token]);

    const handlePayNow = () => {
        if (invoice?.paymentLink) {
            window.location.href = invoice.paymentLink;
        }
    };

    if (loading) return <div className="p-8 text-center text-muted-foreground"><Loader2 className="w-8 h-8 animate-spin mx-auto mb-4" />Loading invoice...</div>;
    if (error) return <div className="p-8 text-center text-destructive bg-destructive/10 rounded-lg mx-4 mt-8 max-w-lg md:mx-auto">{error}</div>;
    if (!invoice) return <div className="p-8 text-center muted">Invoice not found</div>;

    // Calculate subtotal if not provided
    const subtotal = invoice.totalPrice - invoice.gst;

    return (
        <div className="flex justify-center min-h-screen bg-gray-50 py-10 px-4">
            <Card className="max-w-4xl w-full shadow-lg">
                {/* ... existing header ... */}
                <CardHeader className="border-b">
                    <div className="flex justify-between items-start">
                        <div>
                            <CardTitle className="text-3xl font-bold">Invoice {invoice.invoiceNumber}</CardTitle>
                            <CardDescription>
                                Issued to {invoice.clientEntityName ? `${invoice.clientEntityName} (` : ''}
                                {invoice.clientFirstname} {invoice.clientLastname}
                                {invoice.clientEntityName ? ')' : ''}
                            </CardDescription>
                        </div>

                        <div className="text-right">
                            <Badge variant={invoice.status === 'PAID' ? 'success' : 'secondary'} className="text-lg">
                                {INVOICE_STATUS_LABELS[invoice.status] || invoice.status}
                            </Badge>
                            <div className="text-sm text-muted-foreground mt-2">
                                Due: {invoice.dueDate}
                            </div>
                        </div>
                    </div>
                </CardHeader>
                <CardContent className="pt-6">
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>Description</TableHead>
                                <TableHead className="text-right">Price</TableHead>
                                <TableHead className="text-right">Qty</TableHead>
                                <TableHead className="text-right">GST</TableHead>
                                <TableHead className="text-right">Total</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {invoice.invoiceItems.map((item, i) => (
                                <TableRow key={i}>
                                    <TableCell>{item.itemDescription}</TableCell>
                                    <TableCell className="text-right">${Number(item.price).toFixed(2)}</TableCell>
                                    <TableCell className="text-right">{item.quantity}</TableCell>
                                    <TableCell className="text-right">{item.gst > 0 ? '10%' : '0%'}</TableCell>
                                    <TableCell className="text-right font-medium">${Number(item.total).toFixed(2)}</TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>

                    <div className="flex justify-end mt-6">
                        <div className="w-1/3 space-y-2">
                            {/* ... totals ... */}
                            <div className="flex justify-between text-sm">
                                <span>Subtotal</span>
                                <span>${subtotal?.toFixed(2)}</span>
                            </div>
                            <div className="flex justify-between text-sm">
                                <span>GST</span>
                                <span>${Number(invoice.gst).toFixed(2)}</span>
                            </div>
                            {invoice.discountValue > 0 && (
                                <div className="flex justify-between text-sm text-green-600">
                                    <span>Discount</span>
                                    <span>
                                        -{invoice.discountType === 'DOLLAR' ? '$' : ''}
                                        {Number(invoice.discountValue).toFixed(2)}
                                        {invoice.discountType === 'PERCENT' ? '%' : ''}
                                    </span>
                                </div>
                            )}
                            <div className="flex justify-between font-bold text-lg border-t pt-2">
                                <span>Total</span>
                                <span>${Number(invoice.totalPrice).toFixed(2)}</span>
                            </div>
                        </div>
                    </div>

                    {invoice.notes && (
                        <div className="mt-8">
                            <h4 className="text-sm font-medium mb-2">Notes</h4>
                            <p className="text-sm text-muted-foreground whitespace-pre-wrap p-4 bg-gray-50 rounded-md">
                                {invoice.notes}
                            </p>
                        </div>
                    )}

                    {invoice.paymentLink && invoice.status !== 'PAID' && (
                        <div className="mt-8 flex justify-end">
                            <Button onClick={handlePayNow} size="lg" className="w-full sm:w-auto bg-green-600 hover:bg-green-700">
                                <CreditCard className="mr-2 h-5 w-5" />
                                Pay Now Online Securely via Stripe ${Number(invoice.totalPrice).toFixed(2)}
                            </Button>
                        </div>
                    )}
                </CardContent>
            </Card>
        </div>
    );
}
