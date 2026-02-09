import { useAppStore } from '@/store/useAppStore';
import { useInvoices } from '@/hooks/useInvoices';
import { useQuotes } from '@/hooks/useQuotes';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Loader2, FileText, Banknote, Plus, Users } from 'lucide-react';
import { useCustomers } from '@/hooks/useCustomers';

import { Button } from '@/components/ui/button';
import { useNavigate } from 'react-router-dom';
import { Badge } from '@/components/ui/badge';
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table"

export default function DashboardPage() {
    const { selectedCompanyId } = useAppStore();
    const navigate = useNavigate();

    const { data: invoicesData, isLoading: isLoadingInvoices } = useInvoices({
        page: 0,
        size: 5,
        companyId: selectedCompanyId
    });

    const { data: quotesData, isLoading: isLoadingQuotes } = useQuotes({
        page: 0,
        size: 5,
        companyId: selectedCompanyId
    });

    const { data: customersData, isLoading: isLoadingCustomers } = useCustomers({
        page: 0,
        size: 1,
        companyId: selectedCompanyId
    });



    if (!selectedCompanyId) {
        return (
            <div className="flex h-full items-center justify-center p-8">
                <Card className="w-full max-w-md text-center">
                    <CardHeader>
                        <CardTitle>No Company Selected</CardTitle>
                        <CardDescription>Please select or create a company to view the dashboard.</CardDescription>
                    </CardHeader>
                </Card>
            </div>
        );
    }

    const totalInvoices = invoicesData?.totalElements || 0;
    const totalQuotes = quotesData?.totalElements || 0;
    const totalCustomers = customersData?.totalElements || 0;


    return (
        <div className="container mx-auto p-4 md:p-6 space-y-6 md:space-y-8">
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                <h1 className="text-2xl md:text-3xl font-bold tracking-tight">Dashboard</h1>
                <div className="flex w-full sm:w-auto gap-2">
                    <Button onClick={() => navigate('/quotes/new')} variant="outline" className="flex-1 sm:flex-none">
                        <Plus className="mr-2 h-4 w-4" /> New Quote
                    </Button>
                    <Button onClick={() => navigate('/invoices/new')} className="flex-1 sm:flex-none">
                        <Plus className="mr-2 h-4 w-4" /> New Invoice
                    </Button>
                </div>
            </div>

            {/* Recent Items */}
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-7">
                <Card className="col-span-4">
                    <CardHeader>
                        <CardTitle>Recent Invoices</CardTitle>
                        <CardDescription>
                            You made {totalInvoices} invoices in total.
                        </CardDescription>
                    </CardHeader>
                    <CardContent>
                        {isLoadingInvoices ? (
                            <div className="flex justify-center p-4">
                                <Loader2 className="h-6 w-6 animate-spin" />
                            </div>
                        ) : invoicesData?.content?.length === 0 ? (
                            <p className="text-sm text-muted-foreground text-center py-4">No invoices found.</p>
                        ) : (
                            <Table>
                                <TableHeader>
                                    <TableRow>
                                        <TableHead className="hidden sm:table-cell">System #</TableHead>
                                        <TableHead>Client</TableHead>
                                        <TableHead>Total</TableHead>
                                        <TableHead className="text-right">Status</TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {invoicesData?.content.map((invoice) => (
                                        <TableRow key={invoice.id} onClick={() => navigate(`/invoices/${invoice.id}`)} className="cursor-pointer">
                                            <TableCell className="font-medium hidden sm:table-cell">{invoice.invoiceNumber}</TableCell>
                                            <TableCell>{invoice.clientFirstname} {invoice.clientLastname}</TableCell>
                                            <TableCell>${invoice.totalPrice?.toFixed(2)}</TableCell>
                                            <TableCell className="text-right">
                                                <Badge variant={invoice.status === 'PAID' ? 'success' : invoice.status === 'DRAFT' ? 'secondary' : 'default'}>
                                                    {invoice.status}
                                                </Badge>
                                            </TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        )}
                    </CardContent>
                </Card>
                <Card className="col-span-3">
                    <CardHeader>
                        <CardTitle>Recent Quotes</CardTitle>
                        <CardDescription>
                            Latest {quotesData?.content?.length || 0} quotes from {totalQuotes} total.
                        </CardDescription>
                    </CardHeader>
                    <CardContent>
                        {isLoadingQuotes ? (
                            <div className="flex justify-center p-4">
                                <Loader2 className="h-6 w-6 animate-spin" />
                            </div>
                        ) : quotesData?.content?.length === 0 ? (
                            <p className="text-sm text-muted-foreground text-center py-4">No quotes found.</p>
                        ) : (
                            <div className="space-y-4">
                                {quotesData?.content.map((quote) => (
                                    <div key={quote.id} onClick={() => navigate(`/quotes/${quote.id}`)} className="flex items-center justify-between p-2 hover:bg-muted/50 rounded-lg cursor-pointer transition-colors border">
                                        <div className="space-y-1">
                                            <p className="text-sm font-medium leading-none">{quote.quoteNumber}</p>
                                            <p className="text-xs text-muted-foreground">{quote.clientFirstname} {quote.clientLastname}</p>
                                        </div>
                                        <div className="flex items-center gap-2">
                                            <span className="text-sm font-bold">${quote.totalPrice?.toFixed(2)}</span>
                                            <Badge variant="outline" className="text-xs">{quote.status}</Badge>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </CardContent>
                </Card>
            </div>

            {/* Stats Cards */}
            <div className="grid gap-4 grid-cols-2 lg:grid-cols-4">
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Total Invoices</CardTitle>
                        <Banknote className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        {isLoadingInvoices ? (
                            <Loader2 className="h-4 w-4 animate-spin" />
                        ) : (
                            <div className="text-2xl font-bold">{totalInvoices}</div>
                        )}
                        <p className="text-xs text-muted-foreground">All time invoices</p>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Total Quotes</CardTitle>
                        <FileText className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        {isLoadingQuotes ? (
                            <Loader2 className="h-4 w-4 animate-spin" />
                        ) : (
                            <div className="text-2xl font-bold">{totalQuotes}</div>
                        )}
                        <p className="text-xs text-muted-foreground">All time quotes</p>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Total Customers</CardTitle>
                        <Users className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        {isLoadingCustomers ? (
                            <Loader2 className="h-4 w-4 animate-spin" />
                        ) : (
                            <div className="text-2xl font-bold">{totalCustomers}</div>
                        )}
                        <p className="text-xs text-muted-foreground">Active clients</p>
                    </CardContent>
                </Card>
            </div>

            {/* Quick Actions */}
            <div className="grid gap-4 grid-cols-2 lg:grid-cols-4">
                <Button variant="outline" className="h-24 flex flex-col gap-2 bg-blue-50/50 hover:bg-blue-100/50 text-blue-700 border-blue-200 hover:border-blue-300 transition-all" onClick={() => navigate('/quotes/new')}>
                    <FileText className="h-6 w-6" />
                    Create Quote
                </Button>
                <Button variant="outline" className="h-24 flex flex-col gap-2 bg-green-50/50 hover:bg-green-100/50 text-green-700 border-green-200 hover:border-green-300 transition-all" onClick={() => navigate('/invoices/new')}>
                    <Banknote className="h-6 w-6" />
                    Create Invoice
                </Button>
                <Button variant="outline" className="h-24 flex flex-col gap-2 bg-violet-50/50 hover:bg-violet-100/50 text-violet-700 border-violet-200 hover:border-violet-300 transition-all text-wrap" onClick={() => navigate('/customers/new')}>
                    <Users className="h-6 w-6" />
                    Add Customer
                </Button>
            </div>
        </div >
    );
}
