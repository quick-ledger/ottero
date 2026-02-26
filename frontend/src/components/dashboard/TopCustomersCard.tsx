import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { useTopCustomers } from '@/hooks/useStats';
import { Loader2, Users } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

interface TopCustomersCardProps {
    companyId: number | null;
}

export default function TopCustomersCard({ companyId }: TopCustomersCardProps) {
    const { data, isLoading, error } = useTopCustomers(companyId);
    const navigate = useNavigate();

    const formatCurrency = (value: number) => {
        return new Intl.NumberFormat('en-AU', {
            style: 'currency',
            currency: 'AUD',
            minimumFractionDigits: 0,
        }).format(value);
    };

    return (
        <Card className="col-span-3 lg:col-span-2">
            <CardHeader className="pb-2">
                <CardTitle className="flex items-center gap-2 text-base">
                    <Users className="h-5 w-5" />
                    Top Customers
                </CardTitle>
                <CardDescription>By total revenue</CardDescription>
            </CardHeader>
            <CardContent>
                {isLoading ? (
                    <div className="flex justify-center items-center h-[180px]">
                        <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
                    </div>
                ) : error ? (
                    <div className="text-center text-muted-foreground py-4">
                        Failed to load data
                    </div>
                ) : data && data.length > 0 ? (
                    <div className="space-y-3">
                        {data.map((customer, index) => (
                            <div
                                key={customer.customerId}
                                onClick={() => navigate(`/customers/${customer.customerId}`)}
                                className="flex items-center justify-between p-2 hover:bg-muted/50 rounded-lg cursor-pointer transition-colors"
                            >
                                <div className="flex items-center gap-3">
                                    <div className="flex items-center justify-center w-6 h-6 rounded-full bg-primary/10 text-primary text-xs font-medium">
                                        {index + 1}
                                    </div>
                                    <div>
                                        <p className="text-sm font-medium">{customer.customerName}</p>
                                        <p className="text-xs text-muted-foreground">
                                            {customer.invoiceCount} invoice{customer.invoiceCount !== 1 ? 's' : ''}
                                        </p>
                                    </div>
                                </div>
                                <span className="text-sm font-semibold">
                                    {formatCurrency(customer.totalRevenue)}
                                </span>
                            </div>
                        ))}
                    </div>
                ) : (
                    <div className="flex flex-col items-center justify-center h-[180px] text-muted-foreground">
                        <Users className="h-10 w-10 mb-2 opacity-50" />
                        <p className="text-sm">No customer data yet</p>
                    </div>
                )}
            </CardContent>
        </Card>
    );
}
