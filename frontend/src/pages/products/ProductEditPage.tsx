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
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
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
        },
    });

    useEffect(() => {
        if (product) {
            form.reset({
                name: product.name || '',
                description: product.description || '',
                price: product.price || 0,
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
