import { useState } from 'react';
import { useSelectedCompanyId } from '@/store/useAppStore';
import { useStockMovements } from '@/hooks/useInventory';
import { STOCK_MOVEMENT_TYPE_LABELS } from '@/types';
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { ChevronLeft, ChevronRight } from 'lucide-react';

const StockMovementsPage = () => {
    const companyId = useSelectedCompanyId();
    const [page, setPage] = useState(0);
    const pageSize = 20;

    const { data, isLoading } = useStockMovements({
        page,
        size: pageSize,
        companyId,
    });

    if (isLoading) {
        return <div className="p-8">Loading...</div>;
    }

    return (
        <div className="container mx-auto py-8 px-4">
            <h1 className="text-2xl font-bold mb-6">Stock Movements</h1>

            <div className="rounded-md border">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>Date</TableHead>
                            <TableHead>Product</TableHead>
                            <TableHead>Type</TableHead>
                            <TableHead>Change</TableHead>
                            <TableHead>Before</TableHead>
                            <TableHead>After</TableHead>
                            <TableHead>Reference</TableHead>
                            <TableHead>Notes</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {data?.content?.map((movement) => (
                            <TableRow key={movement.id}>
                                <TableCell>
                                    {new Date(movement.createdDate).toLocaleDateString()}
                                </TableCell>
                                <TableCell>{movement.productName}</TableCell>
                                <TableCell>
                                    {STOCK_MOVEMENT_TYPE_LABELS[movement.movementType]}
                                </TableCell>
                                <TableCell
                                    className={
                                        movement.quantityChange > 0
                                            ? 'text-green-600'
                                            : 'text-red-600'
                                    }
                                >
                                    {movement.quantityChange > 0 ? '+' : ''}
                                    {movement.quantityChange}
                                </TableCell>
                                <TableCell>{movement.quantityBefore}</TableCell>
                                <TableCell>{movement.quantityAfter}</TableCell>
                                <TableCell>
                                    {movement.referenceNumber || movement.referenceType}
                                </TableCell>
                                <TableCell className="max-w-xs truncate">
                                    {movement.notes}
                                </TableCell>
                            </TableRow>
                        ))}
                        {(!data?.content || data.content.length === 0) && (
                            <TableRow>
                                <TableCell colSpan={8} className="text-center py-8">
                                    No stock movements found
                                </TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </div>

            {data && data.totalPages > 1 && (
                <div className="flex items-center justify-between mt-4">
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

export default StockMovementsPage;
