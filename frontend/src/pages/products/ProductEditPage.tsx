import { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useProduct, useCreateProduct, useUpdateProduct } from '@/hooks/useProducts';
import { ProductSchema, type ProductFormValues } from '@/types/schemas';
import { useSelectedCompanyId } from '@/store/useAppStore';
import { Button } from '@/components/ui/button';
import {
    Form,
    FormControl,
    FormDescription,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Switch } from '@/components/ui/switch';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { toast } from 'sonner';

export default function ProductEditPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const companyId = useSelectedCompanyId();
    const isNew = !id || id === 'new';

    const { data: product, isLoading: isLoadingProduct } = useProduct(id);
    const createProduct = useCreateProduct();
    const updateProduct = useUpdateProduct();

    const form = useForm<ProductFormValues>({
        resolver: zodResolver(ProductSchema),
        defaultValues: {
            name: '',
            description: '',
            price: 0,
            trackInventory: false,
            quantityOnHand: 0,
            reorderPoint: undefined,
            reorderQuantity: undefined,
        },
    });

    const trackInventory = form.watch('trackInventory');

    useEffect(() => {
        if (product) {
            form.reset({
                name: product.name || '',
                description: product.description || '',
                price: product.price || 0,
                trackInventory: product.trackInventory || false,
                quantityOnHand: product.quantityOnHand || 0,
                reorderPoint: product.reorderPoint,
                reorderQuantity: product.reorderQuantity,
            });
        }
    }, [product, form]);

    const onSubmit = async (values: ProductFormValues) => {
        if (!companyId) {
            toast.error('No company selected');
            return;
        }

        try {
            if (isNew) {
                await createProduct.mutateAsync({ ...values, companyId });
                toast.success('Product created successfully');
                navigate('/products');
            } else {
                if (!id) return;
                await updateProduct.mutateAsync({ id, companyId, ...values });
                toast.success('Product updated successfully');
                navigate('/products');
            }
        } catch (error) {
            console.error('Failed to save product', error);
            toast.error('Failed to save product');
        }
    };

    if (!isNew && isLoadingProduct) {
        return <div className="p-8">Loading product details...</div>;
    }

    return (
        <div className="container max-w-3xl py-10">
            <Card>
                <CardHeader>
                    <CardTitle>{isNew ? 'New Product' : 'Edit Product'}</CardTitle>
                </CardHeader>
                <CardContent>
                    <Form {...form}>
                        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
                            <FormField
                                control={form.control}
                                name="name"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Product Name *</FormLabel>
                                        <FormControl>
                                            <Input placeholder="Widget A" {...field} />
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
                                            <Input placeholder="Description of the product" {...field} />
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

                            {/* Inventory Section */}
                            <div className="border-t pt-6 mt-6">
                                <h3 className="text-lg font-medium mb-4">Inventory Tracking</h3>

                                <FormField
                                    control={form.control}
                                    name="trackInventory"
                                    render={({ field }) => (
                                        <FormItem className="flex flex-row items-center justify-between rounded-lg border p-4">
                                            <div className="space-y-0.5">
                                                <FormLabel className="text-base">Track Inventory</FormLabel>
                                                <FormDescription>
                                                    Enable stock tracking for this product
                                                </FormDescription>
                                            </div>
                                            <FormControl>
                                                <Switch
                                                    checked={field.value}
                                                    onCheckedChange={field.onChange}
                                                />
                                            </FormControl>
                                        </FormItem>
                                    )}
                                />

                                {trackInventory && (
                                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mt-4">
                                        <FormField
                                            control={form.control}
                                            name="quantityOnHand"
                                            render={({ field }) => (
                                                <FormItem>
                                                    <FormLabel>Quantity on Hand</FormLabel>
                                                    <FormControl>
                                                        <Input
                                                            type="number"
                                                            placeholder="0"
                                                            {...field}
                                                            onChange={(e) => field.onChange(e.target.valueAsNumber || 0)}
                                                        />
                                                    </FormControl>
                                                    <FormMessage />
                                                </FormItem>
                                            )}
                                        />
                                        <FormField
                                            control={form.control}
                                            name="reorderPoint"
                                            render={({ field }) => (
                                                <FormItem>
                                                    <FormLabel>Reorder Point</FormLabel>
                                                    <FormControl>
                                                        <Input
                                                            type="number"
                                                            placeholder="e.g., 10"
                                                            {...field}
                                                            onChange={(e) => field.onChange(e.target.valueAsNumber || undefined)}
                                                        />
                                                    </FormControl>
                                                    <FormDescription>
                                                        Alert when stock falls below
                                                    </FormDescription>
                                                    <FormMessage />
                                                </FormItem>
                                            )}
                                        />
                                        <FormField
                                            control={form.control}
                                            name="reorderQuantity"
                                            render={({ field }) => (
                                                <FormItem>
                                                    <FormLabel>Reorder Quantity</FormLabel>
                                                    <FormControl>
                                                        <Input
                                                            type="number"
                                                            placeholder="e.g., 50"
                                                            {...field}
                                                            onChange={(e) => field.onChange(e.target.valueAsNumber || undefined)}
                                                        />
                                                    </FormControl>
                                                    <FormDescription>
                                                        Suggested order quantity
                                                    </FormDescription>
                                                    <FormMessage />
                                                </FormItem>
                                            )}
                                        />
                                    </div>
                                )}
                            </div>

                            <div className="flex justify-end gap-4">
                                <Button type="button" variant="outline" onClick={() => navigate('/products')}>
                                    Cancel
                                </Button>
                                <Button type="submit" disabled={createProduct.isPending || updateProduct.isPending}>
                                    {isNew ? 'Create Product' : 'Save Changes'}
                                </Button>
                            </div>
                        </form>
                    </Form>
                </CardContent>
            </Card>
        </div>
    );
}
