import { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useForm, useFieldArray } from 'react-hook-form'; // useFieldArray for dynamic rows
import { zodResolver } from '@hookform/resolvers/zod';
import { useAssetDefinitionWithCompany, useCreateAssetDefinition, useUpdateAssetDefinition } from '@/hooks/useAssetDefinitions';
import { AssetDefinitionSchema, type AssetDefinitionFormValues } from '@/types/schemas';
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
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { toast } from 'sonner';
import { Trash2, Plus } from 'lucide-react';

export default function AssetDefEditPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const companyId = useSelectedCompanyId();
    const isNew = !id || id === 'new';

    const { data: assetDef, isLoading: isLoadingDef } = useAssetDefinitionWithCompany(id, companyId);
    const createDef = useCreateAssetDefinition();
    const updateDef = useUpdateAssetDefinition();

    const form = useForm<AssetDefinitionFormValues>({
        resolver: zodResolver(AssetDefinitionSchema),
        defaultValues: {
            name: '',
            productDescription: '',
            rows: [],
        },
    });

    const { fields, append, remove } = useFieldArray({
        control: form.control,
        name: "rows",
    });

    useEffect(() => {
        if (assetDef) {
            form.reset({
                name: assetDef.name || '',
                productDescription: assetDef.productDescription || '',
                rows: assetDef.rows || [],
            });
        }
    }, [assetDef, form]);

    const onSubmit = async (values: AssetDefinitionFormValues) => {
        if (!companyId) {
            toast.error('No company selected');
            return;
        }

        try {
            if (isNew) {
                await createDef.mutateAsync({ ...values, companyId });
                toast.success('Definition created successfully');
                navigate('/assets/def');
            } else {
                if (!id) return;
                await updateDef.mutateAsync({ id, companyId, ...values });
                toast.success('Definition updated successfully');
                navigate('/assets/def');
            }
        } catch (error) {
            console.error('Failed to save definition', error);
            toast.error('Failed to save definition');
        }
    };

    if (!isNew && isLoadingDef) {
        return <div className="p-8">Loading definition details...</div>;
    }

    return (
        <div className="container max-w-4xl py-10">
            <Card>
                <CardHeader>
                    <CardTitle>{isNew ? 'New Asset Definition' : 'Edit Asset Definition'}</CardTitle>
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
                                            <FormLabel>Asset Title *</FormLabel>
                                            <FormControl>
                                                <Input placeholder="Vehicle" {...field} />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name="productDescription"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Description</FormLabel>
                                            <FormControl>
                                                <Input placeholder="Company Vehicles" {...field} />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                            </div>

                            <div>
                                <div className="flex justify-between items-center mb-4">
                                    <h3 className="text-lg font-medium">Attributes</h3>
                                    <Button type="button" size="sm" variant="outline" onClick={() => append({
                                        name: '',
                                        description: '',
                                        required: 'yes',
                                        valueType: 'string',
                                        defaultValue: '',
                                        unit: ''
                                    })}>
                                        <Plus className="mr-2 h-4 w-4" />
                                        Add Attribute
                                    </Button>
                                </div>

                                <div className="border rounded-md overflow-x-auto">
                                    <Table>
                                        <TableHeader>
                                            <TableRow>
                                                <TableHead className="min-w-[150px]">Name</TableHead>
                                                <TableHead className="min-w-[150px]">Description</TableHead>
                                                <TableHead className="w-[100px]">Required</TableHead>
                                                <TableHead className="w-[120px]">Type</TableHead>
                                                <TableHead className="min-w-[120px]">Default</TableHead>
                                                <TableHead className="w-[100px]">Unit</TableHead>
                                                <TableHead className="w-[50px]"></TableHead>
                                            </TableRow>
                                        </TableHeader>
                                        <TableBody>
                                            {fields.map((field, index) => (
                                                <TableRow key={field.id}>
                                                    <TableCell>
                                                        <FormField
                                                            control={form.control}
                                                            name={`rows.${index}.name`}
                                                            render={({ field }) => (
                                                                <FormControl>
                                                                    <Input {...field} placeholder="Attribute Name" />
                                                                </FormControl>
                                                            )}
                                                        />
                                                    </TableCell>
                                                    <TableCell>
                                                        <FormField
                                                            control={form.control}
                                                            name={`rows.${index}.description`}
                                                            render={({ field }) => (
                                                                <FormControl>
                                                                    <Input {...field} placeholder="Desc" />
                                                                </FormControl>
                                                            )}
                                                        />
                                                    </TableCell>
                                                    <TableCell>
                                                        <FormField
                                                            control={form.control}
                                                            name={`rows.${index}.required`}
                                                            render={({ field }) => (
                                                                <Select onValueChange={field.onChange} defaultValue={field.value}>
                                                                    <FormControl>
                                                                        <SelectTrigger>
                                                                            <SelectValue />
                                                                        </SelectTrigger>
                                                                    </FormControl>
                                                                    <SelectContent>
                                                                        <SelectItem value="yes">Yes</SelectItem>
                                                                        <SelectItem value="no">No</SelectItem>
                                                                    </SelectContent>
                                                                </Select>
                                                            )}
                                                        />
                                                    </TableCell>
                                                    <TableCell>
                                                        <FormField
                                                            control={form.control}
                                                            name={`rows.${index}.valueType`}
                                                            render={({ field }) => (
                                                                <Select onValueChange={field.onChange} defaultValue={field.value}>
                                                                    <FormControl>
                                                                        <SelectTrigger>
                                                                            <SelectValue />
                                                                        </SelectTrigger>
                                                                    </FormControl>
                                                                    <SelectContent>
                                                                        <SelectItem value="string">String</SelectItem>
                                                                        <SelectItem value="number">Number</SelectItem>
                                                                    </SelectContent>
                                                                </Select>
                                                            )}
                                                        />
                                                    </TableCell>
                                                    <TableCell>
                                                        <FormField
                                                            control={form.control}
                                                            name={`rows.${index}.defaultValue`}
                                                            render={({ field }) => (
                                                                <FormControl>
                                                                    <Input {...field} />
                                                                </FormControl>
                                                            )}
                                                        />
                                                    </TableCell>
                                                    <TableCell>
                                                        <FormField
                                                            control={form.control}
                                                            name={`rows.${index}.unit`}
                                                            render={({ field }) => (
                                                                <FormControl>
                                                                    <Input {...field} placeholder="kg, m" />
                                                                </FormControl>
                                                            )}
                                                        />
                                                    </TableCell>
                                                    <TableCell>
                                                        <Button
                                                            type="button"
                                                            variant="ghost"
                                                            size="icon"
                                                            className="text-destructive"
                                                            onClick={() => remove(index)}
                                                        >
                                                            <Trash2 className="h-4 w-4" />
                                                        </Button>
                                                    </TableCell>
                                                </TableRow>
                                            ))}
                                        </TableBody>
                                    </Table>
                                </div>
                            </div>

                            <div className="flex justify-end gap-4">
                                <Button type="button" variant="outline" onClick={() => navigate('/assets/def')}>
                                    Cancel
                                </Button>
                                <Button type="submit" disabled={createDef.isPending || updateDef.isPending}>
                                    {isNew ? 'Create Definition' : 'Save Changes'}
                                </Button>
                            </div>
                        </form>
                    </Form>
                </CardContent>
            </Card>
        </div>
    );
}
