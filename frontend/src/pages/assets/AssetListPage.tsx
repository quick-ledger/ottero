import { useNavigate } from 'react-router-dom';
import { useAssets, useDeleteAsset } from '@/hooks/useAssets';
import { useAppStore } from '@/store/useAppStore';
import { Button } from '@/components/ui/button';
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table';
import { Plus, Pencil, Trash2 } from 'lucide-react';
import { Link } from 'react-router-dom';
import { toast } from 'sonner';

export default function AssetListPage() {
    const navigate = useNavigate();
    const { selectedCompanyId } = useAppStore();
    const { data: assets, isLoading, error } = useAssets({ companyId: selectedCompanyId });
    const deleteAsset = useDeleteAsset();

    const handleDelete = async (id: string) => {
        if (!selectedCompanyId) return;
        if (confirm('Are you sure you want to delete this asset?')) {
            try {
                await deleteAsset.mutateAsync({ id, companyId: selectedCompanyId });
                toast.success('Asset deleted');
            } catch (e) {
                toast.error('Failed to delete asset');
            }
        }
    };

    if (error) return <div className="p-8 text-destructive">Error loading assets.</div>;

    return (
        <div className="container mx-auto py-10">
            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Assets</h1>
                    <p className="text-muted-foreground mt-1">Manage your company assets.</p>
                </div>
                <div className="flex gap-2">
                    <Button variant="outline" onClick={() => navigate('/assets/def')}>
                        Manage Definitions
                    </Button>
                    <Button onClick={() => navigate('/assets/new')}>
                        <Plus className="mr-2 h-4 w-4" />
                        New Asset
                    </Button>
                </div>
            </div>

            <div className="border rounded-md">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>Name</TableHead>
                            <TableHead>Description</TableHead>
                            <TableHead className="text-right">Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {isLoading ? (
                            <TableRow>
                                <TableCell colSpan={3} className="h-24 text-center">
                                    Loading assets...
                                </TableCell>
                            </TableRow>
                        ) : (!assets || assets.length === 0) ? (
                            <TableRow>
                                <TableCell colSpan={3} className="h-24 text-center">
                                    No assets found.
                                </TableCell>
                            </TableRow>
                        ) : (
                            assets.map((asset) => (
                                <TableRow key={asset.id}>
                                    <TableCell className="font-medium">
                                        <Link to={`/assets/${asset.id}`} className="hover:underline">
                                            {asset.name}
                                        </Link>
                                    </TableCell>
                                    <TableCell>{asset.description}</TableCell>
                                    <TableCell className="text-right">
                                        <div className="flex justify-end gap-2">
                                            <Button variant="ghost" size="icon" asChild>
                                                <Link to={`/assets/${asset.id}`}>
                                                    <Pencil className="h-4 w-4" />
                                                </Link>
                                            </Button>
                                            <Button variant="ghost" size="icon" className="text-destructive" onClick={() => handleDelete(asset.id)}>
                                                <Trash2 className="h-4 w-4" />
                                            </Button>
                                        </div>
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
            </div>
        </div>
    );
}
