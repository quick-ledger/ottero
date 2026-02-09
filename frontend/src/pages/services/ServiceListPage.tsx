import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useServices } from '@/hooks/useServices';
import { useAppStore } from '@/store/useAppStore';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table';
import { Pagination } from '@/components/ui/pagination-controls';
import { Plus, Search, Layers } from 'lucide-react';
import { useDebounce } from '@/hooks/useDebounce';

export default function ServiceListPage() {
    const navigate = useNavigate();
    const { selectedCompanyId } = useAppStore();
    const [page, setPage] = useState(0);
    const [searchTerm, setSearchTerm] = useState('');
    const debouncedSearchTerm = useDebounce(searchTerm, 500);

    const { data, isLoading, error } = useServices({
        page,
        size: 10,
        searchTerm: debouncedSearchTerm,
        companyId: selectedCompanyId
    });

    if (error) return <div className="p-8 text-destructive">Error loading services.</div>;

    return (
        <div className="container mx-auto py-10">
            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Services</h1>
                    <p className="text-muted-foreground mt-1">Manage your service offerings.</p>
                </div>
                <Button onClick={() => navigate('/services/new')}>
                    <Plus className="mr-2 h-4 w-4" />
                    New Service
                </Button>
            </div>

            <div className="flex items-center py-4 relative max-w-sm">
                <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                <Input
                    placeholder="Search services..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="pl-9"
                />
            </div>

            <div className="border rounded-md">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>Name</TableHead>
                            <TableHead>Description</TableHead>
                            <TableHead className="text-right">Price</TableHead>
                            <TableHead className="text-right">Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {isLoading ? (
                            <TableRow>
                                <TableCell colSpan={4} className="h-24 text-center">
                                    Loading services...
                                </TableCell>
                            </TableRow>
                        ) : data?.content.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={4} className="h-24 text-center">
                                    No services found.
                                </TableCell>
                            </TableRow>
                        ) : (
                            data?.content.map((service) => (
                                <TableRow key={service.id}>
                                    <TableCell className="font-medium">
                                        <Link to={`/services/${service.id}`} className="hover:underline flex items-center">
                                            <Layers className="mr-2 h-4 w-4 text-muted-foreground" />
                                            {service.name}
                                        </Link>
                                    </TableCell>
                                    <TableCell>{service.description}</TableCell>
                                    <TableCell className="text-right">${Number(service.price).toFixed(2)}</TableCell>
                                    <TableCell className="text-right">
                                        <Button variant="ghost" size="sm" asChild>
                                            <Link to={`/services/${service.id}`}>Edit</Link>
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
