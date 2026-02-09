import { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEmployee, useCreateEmployee, useUpdateEmployee } from '@/hooks/useEmployees';
import { EmployeeSchema, type EmployeeFormValues } from '@/types/schemas';
import { useSelectedCompanyId } from '@/store/useAppStore';
import { Button } from '@/components/ui/button';
import {
    Form,
    FormControl,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { toast } from 'sonner';

export default function EmployeeEditPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const companyId = useSelectedCompanyId();
    const isNew = !id || id === 'new';

    const { data: employee, isLoading: isLoadingEmployee } = useEmployee(id);
    const createEmployee = useCreateEmployee();
    const updateEmployee = useUpdateEmployee();

    const form = useForm<EmployeeFormValues>({
        resolver: zodResolver(EmployeeSchema),
        defaultValues: {
            name: '',
            email: '',
        },
    });

    useEffect(() => {
        if (employee) {
            form.reset({
                name: employee.name || '',
                email: employee.email || '',
            });
        }
    }, [employee, form]);

    const onSubmit = async (values: EmployeeFormValues) => {
        if (!companyId) {
            toast.error('No company selected');
            return;
        }

        try {
            if (isNew) {
                await createEmployee.mutateAsync({ ...values, companyId });
                toast.success('Employee created successfully');
                navigate('/employees');
            } else {
                if (!id) return;
                await updateEmployee.mutateAsync({ id, companyId, ...values });
                toast.success('Employee updated successfully');
                navigate('/employees');
            }
        } catch (error) {
            console.error('Failed to save employee', error);
            toast.error('Failed to save employee');
        }
    };

    if (!isNew && isLoadingEmployee) {
        return <div className="p-8">Loading employee details...</div>;
    }

    return (
        <div className="container max-w-3xl py-10">
            <Card>
                <CardHeader>
                    <CardTitle>{isNew ? 'New Employee' : 'Edit Employee'}</CardTitle>
                </CardHeader>
                <CardContent>
                    <Form {...form}>
                        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                <FormField
                                    control={form.control}
                                    name="name"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Name *</FormLabel>
                                            <FormControl>
                                                <Input placeholder="John Doe" {...field} />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name="email"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Email</FormLabel>
                                            <FormControl>
                                                <Input placeholder="john@example.com" type="email" {...field} value={field.value || ''} />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                            </div>

                            <div className="flex justify-end gap-4">
                                <Button type="button" variant="outline" onClick={() => navigate('/employees')}>
                                    Cancel
                                </Button>
                                <Button type="submit" disabled={createEmployee.isPending || updateEmployee.isPending}>
                                    {isNew ? 'Create Employee' : 'Save Changes'}
                                </Button>
                            </div>
                        </form>
                    </Form>
                </CardContent>
            </Card>
        </div>
    );
}
