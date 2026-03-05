import { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { useSelectedCompanyId } from '@/store/useAppStore';
import { useAsset, useSaveAsset } from '@/hooks/useAssets';
import type { Asset, AssetStatus, DepreciationMethod } from '@/types';
import { ASSET_STATUS_LABELS, DEPRECIATION_METHOD_LABELS } from '@/types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { toast } from 'sonner';
import { ArrowLeft } from 'lucide-react';

const AssetEditPage = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const companyId = useSelectedCompanyId();
    const isNew = id === 'new';

    const { data: asset, isLoading } = useAsset(id || 'new', companyId);
    const saveAsset = useSaveAsset();

    const {
        register,
        handleSubmit,
        reset,
        watch,
        setValue,
        formState: { errors },
    } = useForm<Partial<Asset>>({
        defaultValues: {
            status: 'ACTIVE',
            depreciationMethod: 'NONE',
        },
    });

    const status = watch('status');
    const depreciationMethod = watch('depreciationMethod');

    useEffect(() => {
        if (asset) {
            reset(asset);
        }
    }, [asset, reset]);

    const onSubmit = async (data: Partial<Asset>) => {
        if (!companyId) return;

        try {
            await saveAsset.mutateAsync({
                asset: { ...data, id: isNew ? undefined : id },
                companyId,
            });
            toast.success(isNew ? 'Asset created' : 'Asset updated');
            navigate('/assets');
        } catch (error) {
            toast.error('Failed to save asset');
        }
    };

    if (isLoading && !isNew) {
        return <div className="p-8">Loading...</div>;
    }

    return (
        <div className="container mx-auto py-8 px-4 max-w-2xl">
            <Button variant="ghost" onClick={() => navigate('/assets')} className="mb-4">
                <ArrowLeft className="h-4 w-4 mr-2" />
                Back to Assets
            </Button>

            <Card>
                <CardHeader>
                    <CardTitle>{isNew ? 'New Asset' : 'Edit Asset'}</CardTitle>
                </CardHeader>
                <CardContent>
                    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                        <div className="space-y-2">
                            <Label htmlFor="name">Name *</Label>
                            <Input
                                id="name"
                                {...register('name', { required: 'Name is required' })}
                                placeholder="Asset name"
                            />
                            {errors.name && (
                                <p className="text-sm text-red-500">{errors.name.message}</p>
                            )}
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div className="space-y-2">
                                <Label htmlFor="code">Asset Code</Label>
                                <Input
                                    id="code"
                                    {...register('code')}
                                    placeholder="e.g., ASSET-001"
                                />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="serialNumber">Serial Number</Label>
                                <Input
                                    id="serialNumber"
                                    {...register('serialNumber')}
                                    placeholder="Serial number"
                                />
                            </div>
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="description">Description</Label>
                            <Textarea
                                id="description"
                                {...register('description')}
                                placeholder="Asset description"
                            />
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div className="space-y-2">
                                <Label htmlFor="location">Location</Label>
                                <Input
                                    id="location"
                                    {...register('location')}
                                    placeholder="Where is this asset?"
                                />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="status">Status</Label>
                                <Select
                                    value={status}
                                    onValueChange={(value) => setValue('status', value as AssetStatus)}
                                >
                                    <SelectTrigger>
                                        <SelectValue placeholder="Select status" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        {Object.entries(ASSET_STATUS_LABELS).map(([key, label]) => (
                                            <SelectItem key={key} value={key}>
                                                {label}
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                            </div>
                        </div>

                        <div className="border-t pt-4 mt-4">
                            <h3 className="font-medium mb-4">Financial Information</h3>

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <Label htmlFor="purchaseDate">Purchase Date</Label>
                                    <Input
                                        id="purchaseDate"
                                        type="date"
                                        {...register('purchaseDate')}
                                    />
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="purchasePrice">Purchase Price</Label>
                                    <Input
                                        id="purchasePrice"
                                        type="number"
                                        step="0.01"
                                        {...register('purchasePrice', { valueAsNumber: true })}
                                        placeholder="0.00"
                                    />
                                </div>
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
                                <div className="space-y-2">
                                    <Label htmlFor="currentValue">Current Value</Label>
                                    <Input
                                        id="currentValue"
                                        type="number"
                                        step="0.01"
                                        {...register('currentValue', { valueAsNumber: true })}
                                        placeholder="0.00"
                                    />
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="depreciationMethod">Depreciation Method</Label>
                                    <Select
                                        value={depreciationMethod}
                                        onValueChange={(value) =>
                                            setValue('depreciationMethod', value as DepreciationMethod)
                                        }
                                    >
                                        <SelectTrigger>
                                            <SelectValue placeholder="Select method" />
                                        </SelectTrigger>
                                        <SelectContent>
                                            {Object.entries(DEPRECIATION_METHOD_LABELS).map(
                                                ([key, label]) => (
                                                    <SelectItem key={key} value={key}>
                                                        {label}
                                                    </SelectItem>
                                                )
                                            )}
                                        </SelectContent>
                                    </Select>
                                </div>
                            </div>

                            {depreciationMethod !== 'NONE' && (
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
                                    <div className="space-y-2">
                                        <Label htmlFor="usefulLifeYears">Useful Life (Years)</Label>
                                        <Input
                                            id="usefulLifeYears"
                                            type="number"
                                            {...register('usefulLifeYears', { valueAsNumber: true })}
                                            placeholder="e.g., 5"
                                        />
                                    </div>

                                    <div className="space-y-2">
                                        <Label htmlFor="salvageValue">Salvage Value</Label>
                                        <Input
                                            id="salvageValue"
                                            type="number"
                                            step="0.01"
                                            {...register('salvageValue', { valueAsNumber: true })}
                                            placeholder="0.00"
                                        />
                                    </div>
                                </div>
                            )}
                        </div>

                        <div className="flex gap-2 pt-4">
                            <Button type="submit" disabled={saveAsset.isPending}>
                                {saveAsset.isPending ? 'Saving...' : 'Save Asset'}
                            </Button>
                            <Button
                                type="button"
                                variant="outline"
                                onClick={() => navigate('/assets')}
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

export default AssetEditPage;
