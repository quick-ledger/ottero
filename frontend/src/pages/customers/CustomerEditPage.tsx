import { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useCustomer, useCreateCustomer, useUpdateCustomer } from '@/hooks/useCustomers';
import { CustomerSchema, type CustomerFormValues } from '@/types/schemas';
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

export default function CustomerEditPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const companyId = useSelectedCompanyId();
    const isNew = !id || id === 'new';

    const { data: customer, isLoading: isLoadingCustomer } = useCustomer(id);
    const createCustomer = useCreateCustomer();
    const updateCustomer = useUpdateCustomer();

    const form = useForm<CustomerFormValues>({
        resolver: zodResolver(CustomerSchema),
        defaultValues: {
            firstName: '',
            lastName: '',
            email: '',
            clientEntityName: '',
            phoneNumber: '',
        },
    });

    useEffect(() => {
        if (customer) {
            form.reset({
                firstName: customer.firstName || '',
                lastName: customer.lastName || '',
                email: customer.email || '',
                // @ts-ignore
                clientEntityName: customer.clientEntityName || '', // Old field overlap?
                // @ts-ignore
                phoneNumber: customer.phoneNumber || customer.phone || '',
            });
        }
    }, [customer, form]);

    const onSubmit = async (values: CustomerFormValues) => {
        if (!companyId) {
            toast.error('No company selected');
            return;
        }

        try {
            if (isNew) {
                await createCustomer.mutateAsync({ ...values, companyId });
                toast.success('Customer created successfully');
                navigate('/customers');
            } else {
                if (!id) return;
                await updateCustomer.mutateAsync({ id, companyId, ...values });
                toast.success('Customer updated successfully');
                navigate('/customers');
            }
        } catch (error) {
            console.error('Failed to save customer', error);
            toast.error('Failed to save customer');
        }
    };

    if (!isNew && isLoadingCustomer) {
        return <div className="p-8">Loading customer details...</div>;
    }

    return (
        <div className="container max-w-3xl py-10">
            <Card>
                <CardHeader>
                    <CardTitle>{isNew ? 'New Customer' : 'Edit Customer'}</CardTitle>
                </CardHeader>
                <CardContent>
                    <Form {...form}>
                        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                <FormField
                                    control={form.control}
                                    name="firstName"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>First Name *</FormLabel>
                                            <FormControl>
                                                <Input placeholder="John" {...field} />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name="lastName"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Last Name *</FormLabel>
                                            <FormControl>
                                                <Input placeholder="Doe" {...field} />
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
                                            <FormLabel>Email *</FormLabel>
                                            <FormControl>
                                                <Input type="email" placeholder="john.doe@example.com" {...field} />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name="phoneNumber"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Phone *</FormLabel>
                                            <FormControl>
                                                <Input placeholder="0400 000 000" {...field} required />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                                <div className="md:col-span-2">
                                    <FormField
                                        control={form.control}
                                        name="clientEntityName"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormLabel>Company / Entity Name</FormLabel>
                                                <FormControl>
                                                    <Input placeholder="ACME Inc." {...field} />
                                                </FormControl>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />
                                </div>
                            </div>

                            <div className="flex justify-end gap-4">
                                <Button type="button" variant="outline" onClick={() => navigate('/customers')}>
                                    Cancel
                                </Button>
                                <Button type="submit" disabled={createCustomer.isPending || updateCustomer.isPending}>
                                    {isNew ? 'Create Customer' : 'Save Changes'}
                                </Button>
                            </div>
                        </form>
                    </Form>
                </CardContent>
            </Card>
        </div>
    );
}
