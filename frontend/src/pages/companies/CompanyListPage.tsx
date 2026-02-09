import { Link, useNavigate } from 'react-router-dom';
import { useCompanies } from '@/hooks/useCompanies';
import { useAppStore } from '@/store/useAppStore';
import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Check, Plus } from 'lucide-react';

export default function CompanyListPage() {
    const { data: companies, isLoading, error } = useCompanies();
    const { selectedCompanyId, actions } = useAppStore();
    const navigate = useNavigate();

    const handleSelect = (id: string, name: string) => {
        actions.setSelectedCompany(id, name);
        navigate('/'); // Go to dashboard after selection
    };

    if (isLoading) return <div className="p-8">Loading companies...</div>;
    if (error) return <div className="p-8 text-destructive">Error loading companies. Is the backend running?</div>;

    return (
        <div className="container mx-auto py-10">
            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Companies</h1>
                    <p className="text-orange-600 font-bold mt-1">
                        You need to create a company first to be able to create quotes and invoices.
                    </p>
                </div>
                {(!companies || companies.length === 0) && (
                    <Button onClick={() => navigate('/companies/new')}>
                        <Plus className="mr-2 h-4 w-4" />
                        New Company
                    </Button>
                )}
            </div>

            <div className="border rounded-md">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>Name</TableHead>
                            <TableHead>Email</TableHead>
                            <TableHead>ABN</TableHead>
                            <TableHead className="text-right">Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {companies?.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={4} className="h-24 text-center">
                                    No companies found. Create a new one to get started.
                                </TableCell>
                            </TableRow>
                        ) : (
                            companies?.map((company) => (
                                <TableRow key={company.id}>
                                    <TableCell className="font-medium">
                                        <Link to={`/companies/${company.id}`} className="hover:underline">
                                            {company.name}
                                        </Link>
                                    </TableCell>
                                    <TableCell>{company.email}</TableCell>
                                    <TableCell>{company.abn}</TableCell>
                                    <TableCell className="text-right space-x-2">
                                        {selectedCompanyId === company.id ? (
                                            <Button variant="secondary" size="sm" disabled>
                                                <Check className="mr-2 h-4 w-4" />
                                                Selected
                                            </Button>
                                        ) : (
                                            <Button size="sm" onClick={() => handleSelect(company.id, company.name)}>
                                                Select
                                            </Button>
                                        )}
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
            </div>
        </div>
    );
}
