import { useNavigate } from 'react-router-dom';
import { useAssetDefinitions } from '@/hooks/useAssetDefinitions';
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
import { Plus, Settings } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function AssetDefListPage() {
    const navigate = useNavigate();
    const { selectedCompanyId } = useAppStore();

    // We pass arbitrary page/size as legacy api seems to define returning all
    const { data: assetDefs, isLoading, error } = useAssetDefinitions({
        page: 0,
        size: 100,
        companyId: selectedCompanyId
    });

    if (error) return <div className="p-8 text-destructive">Error loading asset definitions.</div>;

    return (
        <div className="container mx-auto py-10">
            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Asset Definitions</h1>
                    <p className="text-muted-foreground mt-1">Define types of assets (e.g. Vehicles, Laptops).</p>
                </div>
                <Button onClick={() => navigate('/assets/def/new')}>
                    <Plus className="mr-2 h-4 w-4" />
                    New Definition
                </Button>
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
                                    Loading definitions...
                                </TableCell>
                            </TableRow>
                        ) : (!assetDefs || assetDefs.length === 0) ? (
                            <TableRow>
                                <TableCell colSpan={3} className="h-24 text-center">
                                    No asset definitions found.
                                </TableCell>
                            </TableRow>
                        ) : (
                            assetDefs.map((def) => (
                                <TableRow key={def.id}>
                                    <TableCell className="font-medium">
                                        <Link to={`/assets/def/${def.id}`} className="hover:underline flex items-center">
                                            <Settings className="mr-2 h-4 w-4 text-muted-foreground" />
                                            {def.name}
                                        </Link>
                                    </TableCell>
                                    <TableCell>{def.productDescription}</TableCell>
                                    <TableCell className="text-right">
                                        <Button variant="ghost" size="sm" asChild>
                                            <Link to={`/assets/def/${def.id}`}>Edit</Link>
                                        </Button>
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
