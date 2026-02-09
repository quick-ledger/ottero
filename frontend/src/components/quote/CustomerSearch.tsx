import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useDebounce } from '@/hooks/useDebounce';
import { useApi } from '@/hooks/useApi';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import {
    Card,
} from '@/components/ui/card';
import { Loader2, Search, X } from 'lucide-react';
import { useAppStore } from '@/store/useAppStore';

interface ClientDto {
    id: string;
    firstName: string;
    lastName: string;
    email?: string;
    phone?: string;
    entityName?: string;
}

interface CustomerSearchProps {
    onSelect: (client: ClientDto) => void;
    disabled?: boolean;
}

export const CustomerSearch = ({ onSelect, disabled }: CustomerSearchProps) => {
    const [searchTerm, setSearchTerm] = useState('');
    const [isOpen, setIsOpen] = useState(false);
    const debouncedSearchTerm = useDebounce(searchTerm, 500);
    const { selectedCompanyId } = useAppStore();
    const api = useApi();

    const { data: clients, isLoading } = useQuery({
        queryKey: ['clients', 'search', selectedCompanyId, debouncedSearchTerm],
        queryFn: async () => {
            if (!debouncedSearchTerm || debouncedSearchTerm.length < 2) return [];
            const { data } = await api.get<ClientDto[]>(`/api/companies/${selectedCompanyId}/clients/search`, {
                params: { searchTerm: debouncedSearchTerm }
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

    const handleSelect = (client: ClientDto) => {
        onSelect(client);
        setSearchTerm('');
        setIsOpen(false);
    };

    return (
        <div className="relative w-full">
            <div className="relative">
                <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                    placeholder="Search client by name, email, or phone..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    disabled={disabled}
                    className="pl-9"
                    onFocus={() => {
                        if (debouncedSearchTerm.length >= 2 && clients && clients.length > 0) {
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
                    ) : clients && clients.length > 0 ? (
                        <div className="py-1">
                            {clients.map((client) => (
                                <div
                                    key={client.id}
                                    className="px-4 py-2 hover:bg-muted cursor-pointer text-sm"
                                    onClick={() => handleSelect(client)}
                                >
                                    <div className="font-medium">
                                        {client.firstName} {client.lastName}
                                        {client.entityName && <span className="text-muted-foreground ml-2">({client.entityName})</span>}
                                    </div>
                                    <div className="text-xs text-muted-foreground">
                                        {client.email} {client.phone && `• ${client.phone}`}
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        debouncedSearchTerm.length >= 2 && (
                            <div className="p-4 text-sm text-center text-muted-foreground">
                                No clients found.
                            </div>
                        )
                    )}
                </Card>
            )}
        </div>
    );
};
