import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useForm, Controller } from 'react-hook-form';
import { useAsset, useCreateAsset, useUpdateAsset } from '@/hooks/useAssets';
import { useAssetDefinitions } from '@/hooks/useAssetDefinitions';
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
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { toast } from 'sonner';

export default function AssetEditPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const companyId = useSelectedCompanyId();
    const isNew = !id || id === 'new';

    const { data: asset, isLoading: isLoadingAsset } = useAsset(id);
    const { data: definitions } = useAssetDefinitions({ page: 0, size: 100, companyId });

    const createAsset = useCreateAsset();
    const updateAsset = useUpdateAsset();

    const [selectedDefId, setSelectedDefId] = useState<string>('');

    // Form logic is tricky because we have dynamic fields.
    // We'll use a loose schema or just basic values.
    const form = useForm({
        defaultValues: {
            name: '',
            description: '',
            productDefinitionId: '',
            attributes: {} as Record<string, any>,
        }
    });

    // Populate form when asset loads
    useEffect(() => {
        if (asset) {
            form.reset({
                name: asset.name || '',
                description: asset.description || '',
                productDefinitionId: asset.productDefinitionId || '',
                attributes: asset.attributes || {},
            });
            if (asset.productDefinitionId) {
                setSelectedDefId(asset.productDefinitionId);
            }
        }
    }, [asset, form]);

    // Handle definition change
    useEffect(() => {
        // Watch for manual changes if needed, but mainly controlled by Select
        const sub = form.watch((value, { name }) => {
            if (name === 'productDefinitionId' && value.productDefinitionId) {
                setSelectedDefId(value.productDefinitionId);
            }
        });
        return () => sub.unsubscribe();
    }, [form]);

    const onSubmit = async (values: any) => {
        if (!companyId) {
            toast.error('No company selected');
            return;
        }

        try {
            if (isNew) {
                await createAsset.mutateAsync({ ...values, companyId });
                toast.success('Asset created successfully');
                navigate('/assets');
            } else {
                if (!id) return;
                await updateAsset.mutateAsync({ id, companyId, ...values });
                toast.success('Asset updated successfully');
                navigate('/assets');
            }
        } catch (error) {
            console.error('Failed to save asset', error);
            toast.error('Failed to save asset');
        }
    };

    const selectedDef = definitions?.find(d => d.id === selectedDefId);

    if (!isNew && isLoadingAsset) {
        return <div className="p-8">Loading asset details...</div>;
    }

    return (
        <div className="container max-w-3xl py-10">
            <Card>
                <CardHeader>
                    <CardTitle>{isNew ? 'New Asset' : 'Edit Asset'}</CardTitle>
                </CardHeader>
                <CardContent>
                    <Form {...form}>
                        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">

                            <FormField
                                control={form.control}
                                name="productDefinitionId"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Asset Type *</FormLabel>
                                        <Select
                                            onValueChange={field.onChange}
                                            defaultValue={field.value}
                                            value={field.value}
                                        >
                                            <FormControl>
                                                <SelectTrigger>
                                                    <SelectValue placeholder="Select a type" />
                                                </SelectTrigger>
                                            </FormControl>
                                            <SelectContent>
                                                {definitions?.map(def => (
                                                    <SelectItem key={def.id || 'unknown'} value={def.id || 'unknown'}>
                                                        {def.name}
                                                    </SelectItem>
                                                ))}
                                            </SelectContent>
                                        </Select>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />

                            <FormField
                                control={form.control}
                                name="name"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Asset Name *</FormLabel>
                                        <FormControl>
                                            <Input placeholder="My Asset" {...field} />
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
                                            <Input placeholder="Description" {...field} />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />

                            {/* Dynamic Fields */}
                            {selectedDef && selectedDef.rows && selectedDef.rows.length > 0 && (
                                <div className="border-t pt-4 mt-4">
                                    <h3 className="text-lg font-medium mb-4">Attributes for {selectedDef.name}</h3>
                                    <div className="grid grid-cols-1 gap-4">
                                        {selectedDef.rows.map((row) => (
                                            <FormField
                                                key={row.name}
                                                control={form.control}
                                                name={`attributes.${row.name}`}
                                                render={({ field }) => (
                                                    <FormItem>
                                                        <FormLabel>
                                                            {row.name}
                                                            {row.unit ? ` (${row.unit})` : ''}
                                                            {row.required === 'yes' ? ' *' : ''}
                                                        </FormLabel>
                                                        <FormControl>
                                                            <Input
                                                                type={row.valueType === 'number' ? 'number' : 'text'}
                                                                placeholder={row.description}
                                                                {...field}
                                                                value={field.value || ''}
                                                                onChange={(e) => {
                                                                    const val = row.valueType === 'number'
                                                                        ? parseFloat(e.target.value)
                                                                        : e.target.value;
                                                                    field.onChange(val);
                                                                }}
                                                            />
                                                        </FormControl>
                                                        <FormMessage />
                                                    </FormItem>
                                                )}
                                            />
                                        ))}
                                    </div>
                                </div>
                            )}

                            <div className="flex justify-end gap-4">
                                <Button type="button" variant="outline" onClick={() => navigate('/assets')}>
                                    Cancel
                                </Button>
                                <Button type="submit" disabled={createAsset.isPending || updateAsset.isPending}>
                                    {isNew ? 'Create Asset' : 'Save Changes'}
                                </Button>
                            </div>
                        </form>
                    </Form>
                </CardContent>
            </Card>
        </div>
    );
}
