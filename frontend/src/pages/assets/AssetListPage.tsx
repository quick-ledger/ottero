import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useSelectedCompanyId } from '@/store/useAppStore';
import { useAssets, useDeleteAsset } from '@/hooks/useAssets';
import { ASSET_STATUS_LABELS } from '@/types';
import { Button } from '@/components/ui/button';
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table';
import { Plus, Pencil, Trash2, ChevronLeft, ChevronRight } from 'lucide-react';
import { toast } from 'sonner';

const AssetListPage = () => {
    const companyId = useSelectedCompanyId();
    const [page, setPage] = useState(0);
    const pageSize = 20;

    const { data, isLoading } = useAssets({
        page,
        size: pageSize,
        companyId,
    });

    const deleteAsset = useDeleteAsset();

    const handleDelete = async (id: string, name: string) => {
        if (!confirm(`Delete asset "${name}"?`)) return;

        try {
            await deleteAsset.mutateAsync({ id, companyId: companyId! });
            toast.success('Asset deleted');
        } catch (error) {
            toast.error('Failed to delete asset');
        }
    };

    const getStatusColor = (status: string) => {
        switch (status) {
            case 'ACTIVE':
                return 'bg-green-100 text-green-800';
            case 'INACTIVE':
                return 'bg-gray-100 text-gray-800';
            case 'DISPOSED':
                return 'bg-red-100 text-red-800';
            case 'UNDER_MAINTENANCE':
                return 'bg-yellow-100 text-yellow-800';
            default:
                return 'bg-gray-100 text-gray-800';
        }
    };

    if (isLoading) {
        return <div className="p-8">Loading...</div>;
    }

    return (
        <div className="container mx-auto py-8 px-4">
            <div className="flex flex-col sm:flex-row justify-between items-start gap-4 mb-6">
                <div>
                    <h1 className="text-2xl font-bold">Assets</h1>
                    <p className="text-muted-foreground mt-1">
                        Company-owned equipment and property (vehicles, computers, machinery). Assets are not for sale and don't appear on invoices.
                    </p>
                </div>
                <Button asChild className="shrink-0">
                    <Link to="/assets/new">
                        <Plus className="h-4 w-4 mr-2" />
                        Add Asset
                    </Link>
                </Button>
            </div>

            <div className="rounded-md border overflow-x-auto">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>Name</TableHead>
                            <TableHead>Code</TableHead>
                            <TableHead>Location</TableHead>
                            <TableHead>Status</TableHead>
                            <TableHead className="text-right">Current Value</TableHead>
                            <TableHead className="w-[100px]">Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {data?.content?.map((asset) => (
                            <TableRow key={asset.id}>
                                <TableCell className="font-medium">{asset.name}</TableCell>
                                <TableCell>{asset.code}</TableCell>
                                <TableCell>{asset.location}</TableCell>
                                <TableCell>
                                    {asset.status && (
                                        <span
                                            className={`px-2 py-1 rounded-full text-xs ${getStatusColor(
                                                asset.status
                                            )}`}
                                        >
                                            {ASSET_STATUS_LABELS[asset.status]}
                                        </span>
                                    )}
                                </TableCell>
                                <TableCell className="text-right">
                                    {asset.currentValue != null
                                        ? `$${asset.currentValue.toFixed(2)}`
                                        : '-'}
                                </TableCell>
                                <TableCell>
                                    <div className="flex gap-2">
                                        <Button variant="ghost" size="icon" asChild>
                                            <Link to={`/assets/${asset.id}`}>
                                                <Pencil className="h-4 w-4" />
                                            </Link>
                                        </Button>
                                        <Button
                                            variant="ghost"
                                            size="icon"
                                            onClick={() => handleDelete(asset.id, asset.name)}
                                        >
                                            <Trash2 className="h-4 w-4" />
                                        </Button>
                                    </div>
                                </TableCell>
                            </TableRow>
                        ))}
                        {(!data?.content || data.content.length === 0) && (
                            <TableRow>
                                <TableCell colSpan={6} className="text-center py-8">
                                    No assets found
                                </TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </div>

            {data && data.totalPages > 1 && (
                <div className="flex flex-col sm:flex-row items-center justify-between gap-2 mt-4">
                    <div className="text-sm text-muted-foreground">
                        Page {page + 1} of {data.totalPages}
                    </div>
                    <div className="flex gap-2">
                        <Button
                            variant="outline"
                            size="sm"
                            onClick={() => setPage((p) => Math.max(0, p - 1))}
                            disabled={page === 0}
                        >
                            <ChevronLeft className="h-4 w-4" />
                            Previous
                        </Button>
                        <Button
                            variant="outline"
                            size="sm"
                            onClick={() => setPage((p) => p + 1)}
                            disabled={page >= data.totalPages - 1}
                        >
                            Next
                            <ChevronRight className="h-4 w-4" />
                        </Button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AssetListPage;
