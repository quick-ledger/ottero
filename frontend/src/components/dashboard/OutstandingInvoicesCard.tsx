import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { useOutstandingInvoices } from '@/hooks/useStats';
import { Loader2, AlertCircle } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';

interface OutstandingInvoicesCardProps {
    companyId: number | null;
}

export default function OutstandingInvoicesCard({ companyId }: OutstandingInvoicesCardProps) {
    const { data, isLoading, error } = useOutstandingInvoices(companyId);
    const navigate = useNavigate();

    const formatCurrency = (value: number) => {
        return new Intl.NumberFormat('en-AU', {
            style: 'currency',
            currency: 'AUD',
            minimumFractionDigits: 2,
        }).format(value);
    };

    return (
        <Card className="col-span-3 lg:col-span-1">
            <CardHeader className="pb-2">
                <CardTitle className="flex items-center gap-2 text-base">
                    <AlertCircle className="h-5 w-5 text-amber-500" />
                    Outstanding Invoices
                </CardTitle>
                <CardDescription>Invoices awaiting payment</CardDescription>
            </CardHeader>
            <CardContent>
                {isLoading ? (
                    <div className="flex justify-center items-center h-[100px]">
                        <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
                    </div>
                ) : error ? (
                    <div className="text-center text-muted-foreground py-4">
                        Failed to load data
                    </div>
                ) : data ? (
                    <div className="space-y-4">
                        <div className="flex items-baseline gap-2">
                            <span className="text-3xl font-bold">{data.count}</span>
                            <span className="text-muted-foreground">invoices</span>
                        </div>
                        <div>
                            <p className="text-sm text-muted-foreground">Total outstanding</p>
                            <p className="text-xl font-semibold text-amber-600">
                                {formatCurrency(data.totalAmount)}
                            </p>
                        </div>
                        {data.count > 0 && (
                            <Button
                                variant="outline"
                                size="sm"
                                className="w-full"
                                onClick={() => navigate('/invoices?status=SENT')}
                            >
                                View Unpaid Invoices
                            </Button>
                        )}
                    </div>
                ) : (
                    <div className="text-center text-muted-foreground py-4">
                        No outstanding invoices
                    </div>
                )}
            </CardContent>
        </Card>
    );
}
