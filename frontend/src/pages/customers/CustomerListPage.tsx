import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCustomers } from '@/hooks/useCustomers';
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
import { Plus, Search, User } from 'lucide-react';
import { useDebounce } from '@/hooks/useDebounce';

export default function CustomerListPage() {
    const navigate = useNavigate();
    const { selectedCompanyId } = useAppStore();
    const [page, setPage] = useState(0);
    const [searchTerm, setSearchTerm] = useState('');
    const debouncedSearchTerm = useDebounce(searchTerm, 500);

    const { data, isLoading } = useCustomers({
        page,
        size: 10,
        searchTerm: debouncedSearchTerm,
        companyId: selectedCompanyId
    });

    return (
        <div className="container mx-auto py-10">
            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Customers</h1>
                    <p className="text-muted-foreground mt-1">Manage your client base.</p>
                </div>
                <Button onClick={() => navigate('/customers/new')}>
                    <Plus className="mr-2 h-4 w-4" />
                    New Customer
                </Button>
            </div>

            <div className="flex items-center py-4 relative max-w-sm">
                <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                    placeholder="Search customers..."
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
                            <TableHead>Entity</TableHead>
                            <TableHead>Email</TableHead>
                            <TableHead>Phone</TableHead>
                            <TableHead className="text-right">Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {isLoading ? (
                            <TableRow>
                                <TableCell colSpan={5} className="h-24 text-center">
                                    Loading customers...
                                </TableCell>
                            </TableRow>
                        ) : (!data || data.content.length === 0) ? (
                            <TableRow>
                                <TableCell colSpan={5} className="h-24 text-center">
                                    No customers found.
                                </TableCell>
                            </TableRow>
                        ) : (
                            data?.content.map((customer) => (
                                <TableRow key={customer.id}>
                                    <TableCell className="font-medium">
                                        <Link to={`/customers/${customer.id}`} className="hover:underline flex items-center">
                                            <User className="mr-2 h-4 w-4 text-muted-foreground" />
                                            {customer.firstName} {customer.lastName}
                                        </Link>
                                    </TableCell>
                                    <TableCell>{customer.clientEntityName}</TableCell>
                                    <TableCell>{customer.email}</TableCell>
                                    <TableCell>{customer.phoneNumber}</TableCell>
                                    <TableCell className="text-right">
                                        <Button variant="ghost" size="sm" asChild>
                                            <Link to={`/customers/${customer.id}`}>Edit</Link>
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
