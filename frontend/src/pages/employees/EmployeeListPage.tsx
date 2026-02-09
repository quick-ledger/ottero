import { useNavigate } from 'react-router-dom';
import { useEmployees, useDeleteEmployee } from '@/hooks/useEmployees';
import { useAppStore } from '@/store/useAppStore';
import { Button } from '@/components/ui/button';
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table';
import { Plus, Pencil, Trash2 } from 'lucide-react';
import { Link } from 'react-router-dom';
import { toast } from 'sonner';

export default function EmployeeListPage() {
    const navigate = useNavigate();
    const { selectedCompanyId } = useAppStore();
    const { data: employees, isLoading } = useEmployees({ companyId: selectedCompanyId });
    const deleteEmployee = useDeleteEmployee();

    const handleDelete = async (id: string) => {
        if (!selectedCompanyId) return;
        if (confirm('Are you sure you want to delete this employee?')) {
            try {
                await deleteEmployee.mutateAsync({ id, companyId: selectedCompanyId });
                toast.success('Employee deleted');
            } catch (e) {
                toast.error('Failed to delete employee');
            }
        }
    };



    return (
        <div className="container mx-auto py-10">
            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Employees</h1>
                    <p className="text-muted-foreground mt-1">Manage your team.</p>
                </div>
                <Button onClick={() => navigate('/employees/new')}>
                    <Plus className="mr-2 h-4 w-4" />
                    New Employee
                </Button>
            </div>

            <div className="border rounded-md">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>Name</TableHead>
                            <TableHead>Email</TableHead>
                            <TableHead className="text-right">Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {isLoading ? (
                            <TableRow>
                                <TableCell colSpan={3} className="h-24 text-center">
                                    Loading employees...
                                </TableCell>
                            </TableRow>
                        ) : (!employees || employees.length === 0) ? (
                            <TableRow>
                                <TableCell colSpan={3} className="h-24 text-center">
                                    No employees found.
                                </TableCell>
                            </TableRow>
                        ) : (
                            employees.map((employee) => (
                                <TableRow key={employee.id}>
                                    <TableCell className="font-medium">
                                        <Link to={`/employees/${employee.id}`} className="hover:underline">
                                            {employee.name}
                                        </Link>
                                    </TableCell>
                                    <TableCell>{employee.email}</TableCell>
                                    <TableCell className="text-right">
                                        <div className="flex justify-end gap-2">
                                            <Button variant="ghost" size="icon" asChild>
                                                <Link to={`/employees/${employee.id}`}>
                                                    <Pencil className="h-4 w-4" />
                                                </Link>
                                            </Button>
                                            <Button variant="ghost" size="icon" className="text-destructive" onClick={() => handleDelete(employee.id)}>
                                                <Trash2 className="h-4 w-4" />
                                            </Button>
                                        </div>
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
