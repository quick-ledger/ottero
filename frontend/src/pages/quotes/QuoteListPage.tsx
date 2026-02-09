import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useQuotes } from '@/hooks/useQuotes';
import { useAppStore } from '@/store/useAppStore';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Switch } from '@/components/ui/switch';
import { Label } from '@/components/ui/label';
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table';
import { Pagination } from '@/components/ui/pagination-controls';
import { Plus, Search, FileText } from 'lucide-react';
import { useDebounce } from '@/hooks/useDebounce'; // We need to create this

import { getQuoteStatusLabel, getQuoteStatusColor } from '@/lib/quote-status';

export default function QuoteListPage() {
    const navigate = useNavigate();
    const { selectedCompanyId } = useAppStore();
    const [page, setPage] = useState(0);
    const [searchTerm, setSearchTerm] = useState('');
    const [showAllRevisions, setShowAllRevisions] = useState(false);
    const debouncedSearchTerm = useDebounce(searchTerm, 500);

    const { data, isLoading } = useQuotes({
        page,
        size: 10,
        searchTerm: debouncedSearchTerm,
        companyId: selectedCompanyId,
        showAllRevisions
    });

    return (
        <div className="container mx-auto py-10">
            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Quotes</h1>
                    <p className="text-muted-foreground mt-1">View and manage your quotes.</p>
                </div>
                <Button onClick={() => navigate('/quotes/new')}>
                    <Plus className="mr-2 h-4 w-4" />
                    New Quote
                </Button>
            </div>

            <div className="flex items-center justify-between py-4 gap-4">
                <div className="flex items-center relative max-w-sm flex-1">
                    <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                    <Input
                        placeholder="Search quotes..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="pl-9"
                    />
                </div>

                <div className="flex items-center space-x-2">
                    <Switch
                        id="show-all-revisions"
                        checked={showAllRevisions}
                        onCheckedChange={setShowAllRevisions}
                    />
                    <Label htmlFor="show-all-revisions" className="cursor-pointer">
                        Show all revisions
                    </Label>
                </div>
            </div>

            <div className="border rounded-md">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>Quote #</TableHead>
                            <TableHead>Revision</TableHead>
                            <TableHead>Status</TableHead>
                            <TableHead>Client</TableHead>
                            <TableHead>Date</TableHead>
                            <TableHead className="text-right">Total</TableHead>
                            <TableHead className="text-right">Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {isLoading ? (
                            <TableRow>
                                <TableCell colSpan={7} className="h-24 text-center">
                                    Loading quotes...
                                </TableCell>
                            </TableRow>
                        ) : (!data || data.content.length === 0) ? (
                            <TableRow>
                                <TableCell colSpan={7} className="h-24 text-center">
                                    No quotes found.
                                </TableCell>
                            </TableRow>
                        ) : (
                            data?.content.map((quote) => (
                                <TableRow key={quote.id}>
                                    <TableCell className="font-medium">
                                        <Link to={`/quotes/${quote.id}`} className="hover:underline flex items-center">
                                            <FileText className="mr-2 h-4 w-4 text-muted-foreground" />
                                            {quote.quoteNumber}
                                        </Link>
                                    </TableCell>
                                    <TableCell>{quote.quoteRevision}</TableCell>
                                    <TableCell>
                                        <Badge variant={getQuoteStatusColor(quote.status) as any}>
                                            {getQuoteStatusLabel(quote.status)}
                                        </Badge>
                                    </TableCell>
                                    <TableCell>
                                        {quote.clientFirstname} {quote.clientLastname}
                                        {quote.clientEntityName && <div className="text-xs text-muted-foreground">{quote.clientEntityName}</div>}
                                    </TableCell>
                                    <TableCell>{quote.quoteDate}</TableCell>
                                    <TableCell className="text-right">
                                        ${quote.totalPrice.toFixed(2)}
                                    </TableCell>
                                    <TableCell className="text-right">
                                        <Button variant="ghost" size="sm" asChild>
                                            <Link to={`/quotes/${quote.id}`}>Edit</Link>
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
