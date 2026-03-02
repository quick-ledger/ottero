import { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { useSelectedCompanyId } from '@/store/useAppStore';
import { useSupplier, useSaveSupplier } from '@/hooks/useSuppliers';
import type { Supplier } from '@/types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Switch } from '@/components/ui/switch';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { toast } from 'sonner';
import { ArrowLeft } from 'lucide-react';

const SupplierEditPage = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const companyId = useSelectedCompanyId();
    const isNew = id === 'new';

    const { data: supplier, isLoading } = useSupplier(id || 'new', companyId);
    const saveSupplier = useSaveSupplier();

    const {
        register,
        handleSubmit,
        reset,
        watch,
        setValue,
        formState: { errors },
    } = useForm<Partial<Supplier>>({
        defaultValues: {
            isActive: true,
        },
    });

    const isActive = watch('isActive');

    useEffect(() => {
        if (supplier) {
            reset(supplier);
        }
    }, [supplier, reset]);

    const onSubmit = async (data: Partial<Supplier>) => {
        if (!companyId) return;

        try {
            await saveSupplier.mutateAsync({
                supplier: { ...data, id: isNew ? undefined : id },
                companyId,
            });
            toast.success(isNew ? 'Supplier created' : 'Supplier updated');
            navigate('/suppliers');
        } catch (error) {
            toast.error('Failed to save supplier');
        }
    };

    if (isLoading && !isNew) {
        return <div className="p-8">Loading...</div>;
    }

    return (
        <div className="container mx-auto py-8 px-4 max-w-2xl">
            <Button variant="ghost" onClick={() => navigate('/suppliers')} className="mb-4">
                <ArrowLeft className="h-4 w-4 mr-2" />
                Back to Suppliers
            </Button>

            <Card>
                <CardHeader>
                    <CardTitle>{isNew ? 'New Supplier' : 'Edit Supplier'}</CardTitle>
                </CardHeader>
                <CardContent>
                    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                        <div className="space-y-2">
                            <Label htmlFor="name">Name *</Label>
                            <Input
                                id="name"
                                {...register('name', { required: 'Name is required' })}
                                placeholder="Supplier name"
                            />
                            {errors.name && (
                                <p className="text-sm text-red-500">{errors.name.message}</p>
                            )}
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="contactName">Contact Name</Label>
                            <Input
                                id="contactName"
                                {...register('contactName')}
                                placeholder="Contact person"
                            />
                        </div>

                        <div className="grid grid-cols-2 gap-4">
                            <div className="space-y-2">
                                <Label htmlFor="email">Email</Label>
                                <Input
                                    id="email"
                                    type="email"
                                    {...register('email')}
                                    placeholder="email@example.com"
                                />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="phone">Phone</Label>
                                <Input
                                    id="phone"
                                    {...register('phone')}
                                    placeholder="Phone number"
                                />
                            </div>
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="address">Address</Label>
                            <Textarea
                                id="address"
                                {...register('address')}
                                placeholder="Supplier address"
                            />
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="paymentTerms">Payment Terms</Label>
                            <Input
                                id="paymentTerms"
                                {...register('paymentTerms')}
                                placeholder="e.g., Net 30"
                            />
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="notes">Notes</Label>
                            <Textarea
                                id="notes"
                                {...register('notes')}
                                placeholder="Additional notes"
                            />
                        </div>

                        <div className="flex items-center space-x-2">
                            <Switch
                                id="isActive"
                                checked={isActive}
                                onCheckedChange={(checked) => setValue('isActive', checked)}
                            />
                            <Label htmlFor="isActive">Active</Label>
                        </div>

                        <div className="flex gap-2 pt-4">
                            <Button type="submit" disabled={saveSupplier.isPending}>
                                {saveSupplier.isPending ? 'Saving...' : 'Save Supplier'}
                            </Button>
                            <Button
                                type="button"
                                variant="outline"
                                onClick={() => navigate('/suppliers')}
                            >
                                Cancel
                            </Button>
                        </div>
                    </form>
                </CardContent>
            </Card>
        </div>
    );
};

export default SupplierEditPage;
