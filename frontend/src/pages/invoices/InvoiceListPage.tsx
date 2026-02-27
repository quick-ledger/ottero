import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useInvoices } from '@/hooks/useInvoices';
import { useAppStore } from '@/store/useAppStore';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table';
import { Pagination } from '@/components/ui/pagination-controls';
import { Plus, Search, FileText, RefreshCw } from 'lucide-react';
import { useDebounce } from '@/hooks/useDebounce';

import { getInvoiceStatusLabel, getInvoiceStatusColor } from '@/lib/invoice-status';

export default function InvoiceListPage() {
    const navigate = useNavigate();
    const { selectedCompanyId } = useAppStore();
    const [page, setPage] = useState(0);
    const [searchTerm, setSearchTerm] = useState('');
    const debouncedSearchTerm = useDebounce(searchTerm, 500);

    const { data, isLoading } = useInvoices({
        page,
        size: 10,
        searchTerm: debouncedSearchTerm,
        companyId: selectedCompanyId
    });

    return (
        <div className="container mx-auto py-10">
            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Invoices</h1>
                    <p className="text-muted-foreground mt-1">View and manage your invoices.</p>
                </div>
                <Button onClick={() => navigate('/invoices/new')}>
                    <Plus className="mr-2 h-4 w-4" />
                    New Invoice
                </Button>
            </div>

            <div className="flex items-center py-4 relative max-w-sm">
                <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                    placeholder="Search invoices..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="pl-9"
                />
            </div>

            <div className="border rounded-md">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>Invoice #</TableHead>
                            <TableHead>Status</TableHead>
                            <TableHead>Client</TableHead>
                            <TableHead>Issue Date</TableHead>
                            <TableHead>Due Date</TableHead>
                            <TableHead className="text-right">Total</TableHead>
                            <TableHead className="text-right">Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {isLoading ? (
                            <TableRow>
                                <TableCell colSpan={7} className="h-24 text-center">
                                    Loading invoices...
                                </TableCell>
                            </TableRow>
                        ) : (!data || data.content.length === 0) ? (
                            <TableRow>
                                <TableCell colSpan={7} className="h-24 text-center">
                                    No invoices found.
                                </TableCell>
                            </TableRow>
                        ) : (
                            data?.content.map((invoice) => (
                                <TableRow key={invoice.id}>
                                    <TableCell className="font-medium">
                                        <Link to={`/invoices/${invoice.id}`} className="hover:underline flex items-center gap-2">
                                            <FileText className="h-4 w-4 text-muted-foreground" />
                                            {invoice.invoiceNumber}
                                            {invoice.isRecurring && (
                                                <RefreshCw className="h-4 w-4 text-blue-500" title="Recurring invoice" />
                                            )}
                                        </Link>
                                    </TableCell>
                                    <TableCell>
                                        <Badge variant={getInvoiceStatusColor(invoice.status) as any}>
                                            {getInvoiceStatusLabel(invoice.status)}
                                        </Badge>
                                    </TableCell>
                                    <TableCell>
                                        {invoice.clientFirstname} {invoice.clientLastname}
                                        {invoice.clientEntityName && <div className="text-xs text-muted-foreground">{invoice.clientEntityName}</div>}
                                    </TableCell>
                                    <TableCell>{invoice.issueDate}</TableCell>
                                    <TableCell>{invoice.dueDate}</TableCell>
                                    <TableCell className="text-right">
                                        ${invoice.totalPrice.toFixed(2)}
                                    </TableCell>
                                    <TableCell className="text-right">
                                        <Button variant="ghost" size="sm" asChild>
                                            <Link to={`/invoices/${invoice.id}`}>Edit</Link>
                                        </Button>
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
            </div>

            {data && (
                <Pagination
                    currentPage={data.number}
                    totalPages={data.totalPages}
                    onPageChange={setPage}
                />
            )}
        </div>
    );
}
