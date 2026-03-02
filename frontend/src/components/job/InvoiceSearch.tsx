import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useDebounce } from '@/hooks/useDebounce';
import { useApi } from '@/hooks/useApi';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Loader2, Search, X } from 'lucide-react';
import { useAppStore } from '@/store/useAppStore';

interface InvoiceDto {
    id: string;
    invoiceNumber: string;
    status: string;
    clientName?: string;
    totalPrice?: number;
}

interface InvoiceSearchProps {
    onSelect: (invoice: InvoiceDto) => void;
    disabled?: boolean;
    excludeIds?: string[];
}

export const InvoiceSearch = ({ onSelect, disabled, excludeIds = [] }: InvoiceSearchProps) => {
    const [searchTerm, setSearchTerm] = useState('');
    const [isOpen, setIsOpen] = useState(false);
    const debouncedSearchTerm = useDebounce(searchTerm, 500);
    const { selectedCompanyId } = useAppStore();
    const api = useApi();

    const { data: invoices, isLoading } = useQuery({
        queryKey: ['invoices', 'search', selectedCompanyId, debouncedSearchTerm],
        queryFn: async () => {
            if (!debouncedSearchTerm || debouncedSearchTerm.length < 2) return [];
            const { data } = await api.get<InvoiceDto[]>(`/api/companies/${selectedCompanyId}/invoices/search`, {
                params: { searchTerm: debouncedSearchTerm, lazy: true }
            });
            return data;
        },
        enabled: !!selectedCompanyId && debouncedSearchTerm.length >= 2,
    });

    useEffect(() => {
        if (debouncedSearchTerm.length >= 2) {
            setIsOpen(true);
        } else {
            setIsOpen(false);
        }
    }, [debouncedSearchTerm]);

    const handleSelect = (invoice: InvoiceDto) => {
        onSelect(invoice);
        setSearchTerm('');
        setIsOpen(false);
    };

    const filteredInvoices = invoices?.filter(i => !excludeIds.includes(String(i.id))) || [];

    return (
        <div className="relative w-full">
            <div className="relative">
                <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                    placeholder="Search invoice by number or client..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    disabled={disabled}
                    className="pl-9"
                    onFocus={() => {
                        if (debouncedSearchTerm.length >= 2 && filteredInvoices.length > 0) {
                            setIsOpen(true);
                        }
                    }}
                />
                {searchTerm && (
                    <Button
                        variant="ghost"
                        size="icon"
                        className="absolute right-0 top-0 h-9 w-9"
                        onClick={() => {
                            setSearchTerm('');
                            setIsOpen(false);
                        }}
                    >
                        <X className="h-4 w-4" />
                    </Button>
                )}
            </div>

            {isOpen && (
                <Card className="absolute z-50 w-full mt-1 max-h-60 overflow-auto shadow-lg">
                    {isLoading ? (
                        <div className="p-4 flex justify-center text-muted-foreground">
                            <Loader2 className="h-4 w-4 animate-spin mr-2" /> Searching...
                        </div>
                    ) : filteredInvoices.length > 0 ? (
                        <div className="py-1">
                            {filteredInvoices.map((invoice) => (
                                <div
                                    key={invoice.id}
                                    className="px-4 py-2 hover:bg-muted cursor-pointer text-sm"
                                    onClick={() => handleSelect(invoice)}
                                >
                                    <div className="flex items-center justify-between">
                                        <span className="font-medium">{invoice.invoiceNumber}</span>
                                        <Badge variant="outline" className="text-xs">{invoice.status}</Badge>
                                    </div>
                                    {invoice.clientName && (
                                        <div className="text-xs text-muted-foreground mt-1">
                                            {invoice.clientName}
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>
                    ) : (
                        debouncedSearchTerm.length >= 2 && (
                            <div className="p-4 text-sm text-center text-muted-foreground">
                                No invoices found.
                            </div>
                        )
                    )}
                </Card>
            )}
        </div>
    );
};
