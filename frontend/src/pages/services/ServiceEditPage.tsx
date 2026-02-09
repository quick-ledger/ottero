import { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useService, useCreateService, useUpdateService } from '@/hooks/useServices';
import { ServiceSchema, type ServiceFormValues } from '@/types/schemas';
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

export default function ServiceEditPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const companyId = useSelectedCompanyId();
    const isNew = !id || id === 'new';

    const { data: service, isLoading: isLoadingService } = useService(id);
    const createService = useCreateService();
    const updateService = useUpdateService();

    const form = useForm<ServiceFormValues>({
        resolver: zodResolver(ServiceSchema),
        defaultValues: {
            name: '',
            description: '',
            price: 0,
        },
    });

    useEffect(() => {
        if (service) {
            form.reset({
                name: service.name || '',
                description: service.description || '',
                price: service.price || 0,
            });
        }
    }, [service, form]);

    const onSubmit = async (values: ServiceFormValues) => {
        if (!companyId) {
            toast.error('No company selected');
            return;
        }

        try {
            if (isNew) {
                await createService.mutateAsync({ ...values, companyId });
                toast.success('Service created successfully');
                navigate('/services');
            } else {
                if (!id) return;
                await updateService.mutateAsync({ id, companyId, ...values });
                toast.success('Service updated successfully');
                navigate('/services');
            }
        } catch (error) {
            console.error('Failed to save service', error);
            toast.error('Failed to save service');
        }
    };

    if (!isNew && isLoadingService) {
        return <div className="p-8">Loading service details...</div>;
    }

    return (
        <div className="container max-w-3xl py-10">
            <Card>
                <CardHeader>
                    <CardTitle>{isNew ? 'New Service' : 'Edit Service'}</CardTitle>
                </CardHeader>
                <CardContent>
                    <Form {...form}>
                        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
                            <FormField
                                control={form.control}
                                name="name"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Service Name *</FormLabel>
                                        <FormControl>
                                            <Input placeholder="Consulting" {...field} />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                            <FormField
                                control={form.control}
                                name="description"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Description</FormLabel>
                                        <FormControl>
                                            <Input placeholder="Hourly consulting rate" {...field} />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                            <FormField
                                control={form.control}
                                name="price"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Price ($) *</FormLabel>
                                        <FormControl>
                                            <Input type="number" step="0.01" placeholder="0.00" {...field} />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />

                            <div className="flex justify-end gap-4">
                                <Button type="button" variant="outline" onClick={() => navigate('/services')}>
                                    Cancel
                                </Button>
                                <Button type="submit" disabled={createService.isPending || updateService.isPending}>
                                    {isNew ? 'Create Service' : 'Save Changes'}
                                </Button>
                            </div>
                        </form>
                    </Form>
                </CardContent>
            </Card>
        </div>
    );
}
