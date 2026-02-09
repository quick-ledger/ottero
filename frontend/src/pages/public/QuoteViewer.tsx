import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import axios from 'axios';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter } from '@/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Textarea } from '@/components/ui/textarea';
import { Badge } from '@/components/ui/badge';
import { toast } from 'sonner';
import { getQuoteStatusLabel, getQuoteStatusColor } from '@/lib/quote-status';

interface QuoteItem {
    itemDescription: string;
    price: number;
    quantity: number;
    gst: number;
    total: number;
}

interface Quote {
    id: string;
    quoteNumber: string;
    quoteDate: string;
    expiryDate: string;
    status: 'PENDING' | 'SENT' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED';
    clientFirstname: string;
    clientLastname: string;
    subtotal: number;
    gst: number;
    totalPrice: number;
    discountValue: number;
    clientNotes?: string;
    quoteItems: QuoteItem[];
}

export default function QuoteViewer() {
    const [searchParams] = useSearchParams();
    const token = searchParams.get('token');
    // const { id } = useParams(); // ID is not really needed if token has it, but URL might look like /quotes/:id?token=...

    const [quote, setQuote] = useState<Quote | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [notes, setNotes] = useState('');
    const [actioning, setActioning] = useState(false);

    useEffect(() => {
        if (!token) {
            setError('Missing access token');
            setLoading(false);
            return;
        }

        const fetchQuote = async () => {
            try {
                // Use the new public endpoint
                const response = await axios.get<Quote>(`/api/public/quotes/view?token=${token}`);
                setQuote(response.data);
                if (response.data.clientNotes) {
                    setNotes(response.data.clientNotes);
                }
            } catch (err) {
                console.error(err);
                setError('Failed to load quote. The link may have expired.');
            } finally {
                setLoading(false);
            }
        };

        fetchQuote();
    }, [token]);

    const handleAction = async (status: 'ACCEPTED' | 'REJECTED') => {
        if (!quote || !token) return;
        if (!confirm(`Are you sure you want to ${status.toLowerCase()} this quote?`)) return;

        setActioning(true);
        try {
            await axios.post(`/api/public/quotes/action?token=${token}`, {
                status: status,  // Send the NEW status (ACCEPTED or REJECTED)
                clientNotes: notes
            });
            // Update local state to reflect the change
            setQuote(prev => prev ? { ...prev, status: status as any } : null);
            toast.success(`Quote ${status.toLowerCase()} successfully`);
        } catch (err) {
            console.error(err);
            toast.error('Failed to update quote status');
        } finally {
            setActioning(false);
        }
    };

    if (loading) return <div className="p-8 text-center">Loading quote...</div>;
    if (error) return <div className="p-8 text-center text-destructive">{error}</div>;
    if (!quote) return <div className="p-8 text-center">Quote not found</div>;

    const isActionable = quote.status === 'SENT' || quote.status === 'PENDING';

    return (
        <div className="flex justify-center min-h-screen bg-gray-50 py-10 px-4">
            <Card className="max-w-4xl w-full shadow-lg">
                <CardHeader className="border-b">
                    <div className="flex justify-between items-start">
                        <div>
                            <CardTitle className="text-3xl font-bold">Quote {quote.quoteNumber}</CardTitle>
                            <CardDescription>Issued to {quote.clientFirstname} {quote.clientLastname}</CardDescription>
                        </div>

                        <div className="text-right">
                            <Badge variant={getQuoteStatusColor(quote.status) as any} className="text-lg">
                                {getQuoteStatusLabel(quote.status)}
                            </Badge>
                            <div className="text-sm text-muted-foreground mt-2">
                                Expires: {quote.expiryDate}
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
                            {quote.quoteItems.map((item, i) => (
                                <TableRow key={i}>
                                    <TableCell>{item.itemDescription}</TableCell>
                                    <TableCell className="text-right">${item.price?.toFixed(2)}</TableCell>
                                    <TableCell className="text-right">{item.quantity}</TableCell>
                                    <TableCell className="text-right">${item.gst?.toFixed(2)}</TableCell>
                                    <TableCell className="text-right font-medium">${item.total?.toFixed(2)}</TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>

                    <div className="flex justify-end mt-6">
                        <div className="w-1/3 space-y-2">
                            <div className="flex justify-between text-sm">
                                <span>Subtotal</span>
                                <span>${quote.subtotal?.toFixed(2)}</span>
                            </div>
                            <div className="flex justify-between text-sm">
                                <span>GST</span>
                                <span>${quote.gst?.toFixed(2)}</span>
                            </div>
                            <div className="flex justify-between text-sm">
                                <span>Discount</span>
                                <span>-${quote.discountValue?.toFixed(2)}</span>
                            </div>
                            <div className="flex justify-between font-bold text-lg border-t pt-2">
                                <span>Total</span>
                                <span>${quote.totalPrice?.toFixed(2)}</span>
                            </div>
                        </div>
                    </div>

                    <div className="mt-8">
                        <label className="block text-sm font-medium mb-2">Notes / Comments</label>
                        <Textarea
                            value={notes}
                            onChange={(e) => setNotes(e.target.value)}
                            placeholder="Add any notes here..."
                            disabled={!isActionable || actioning}
                        />
                    </div>
                </CardContent>
                {isActionable && (
                    <CardFooter className="flex justify-end gap-3 border-t bg-gray-50/50 p-6">
                        <Button
                            variant="destructive"
                            disabled={actioning}
                            onClick={() => handleAction('REJECTED')}
                        >
                            Reject Quote
                        </Button>
                        <Button
                            disabled={actioning}
                            onClick={() => handleAction('ACCEPTED')}
                        >
                            Accept Quote
                        </Button>
                    </CardFooter>
                )}
            </Card>
        </div>
    );
}
