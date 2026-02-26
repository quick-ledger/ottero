import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { useMonthlyRevenue } from '@/hooks/useStats';
import { Loader2, TrendingUp } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

interface IncomeChartProps {
    companyId: number | null;
}

export default function IncomeChart({ companyId }: IncomeChartProps) {
    const { data, isLoading, error } = useMonthlyRevenue(companyId);

    const formatMonth = (month: string) => {
        const [year, m] = month.split('-');
        const date = new Date(parseInt(year), parseInt(m) - 1);
        return date.toLocaleDateString('en-AU', { month: 'short' });
    };

    const formatCurrency = (value: number) => {
        return new Intl.NumberFormat('en-AU', {
            style: 'currency',
            currency: 'AUD',
            minimumFractionDigits: 0,
            maximumFractionDigits: 0,
        }).format(value);
    };

    const totalRevenue = data?.reduce((sum, item) => sum + item.revenue, 0) || 0;

    return (
        <Card className="col-span-4">
            <CardHeader>
                <div className="flex items-center justify-between">
                    <div>
                        <CardTitle className="flex items-center gap-2">
                            <TrendingUp className="h-5 w-5" />
                            Income Trends
                        </CardTitle>
                        <CardDescription>Monthly revenue for the last 6 months</CardDescription>
                    </div>
                    {!isLoading && data && (
                        <div className="text-right">
                            <p className="text-2xl font-bold">{formatCurrency(totalRevenue)}</p>
                            <p className="text-xs text-muted-foreground">Total (6 months)</p>
                        </div>
                    )}
                </div>
            </CardHeader>
            <CardContent>
                {isLoading ? (
                    <div className="flex justify-center items-center h-[250px]">
                        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
                    </div>
                ) : error ? (
                    <div className="flex justify-center items-center h-[250px] text-muted-foreground">
                        Failed to load revenue data
                    </div>
                ) : data && data.length > 0 ? (
                    <ResponsiveContainer width="100%" height={250}>
                        <BarChart data={data} margin={{ top: 10, right: 10, left: 10, bottom: 0 }}>
                            <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                            <XAxis
                                dataKey="month"
                                tickFormatter={formatMonth}
                                tick={{ fontSize: 12 }}
                                tickLine={false}
                                axisLine={false}
                            />
                            <YAxis
                                tickFormatter={(value) => `$${value / 1000}k`}
                                tick={{ fontSize: 12 }}
                                tickLine={false}
                                axisLine={false}
                                width={50}
                            />
                            <Tooltip
                                formatter={(value: number) => [formatCurrency(value), 'Revenue']}
                                labelFormatter={formatMonth}
                                contentStyle={{
                                    backgroundColor: 'hsl(var(--background))',
                                    border: '1px solid hsl(var(--border))',
                                    borderRadius: '6px',
                                }}
                            />
                            <Bar
                                dataKey="revenue"
                                fill="hsl(var(--primary))"
                                radius={[4, 4, 0, 0]}
                            />
                        </BarChart>
                    </ResponsiveContainer>
                ) : (
                    <div className="flex justify-center items-center h-[250px] text-muted-foreground">
                        No revenue data available
                    </div>
                )}
            </CardContent>
        </Card>
    );
}
