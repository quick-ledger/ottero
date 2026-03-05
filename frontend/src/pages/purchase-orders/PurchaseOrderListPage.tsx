import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useSelectedCompanyId } from '@/store/useAppStore';
import { usePurchaseOrders, useDeletePurchaseOrder } from '@/hooks/usePurchaseOrders';
import { PURCHASE_ORDER_STATUS_LABELS } from '@/types';
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

const PurchaseOrderListPage = () => {
    const companyId = useSelectedCompanyId();
    const [page, setPage] = useState(0);
    const pageSize = 20;

    const { data, isLoading } = usePurchaseOrders({
        page,
        size: pageSize,
        companyId,
    });

    const deletePO = useDeletePurchaseOrder();

    const handleDelete = async (id: string, poNumber: string) => {
        if (!confirm(`Delete purchase order "${poNumber}"?`)) return;

        try {
            await deletePO.mutateAsync({ id, companyId: companyId! });
            toast.success('Purchase order deleted');
        } catch (error) {
            toast.error('Failed to delete purchase order');
        }
    };

    const getStatusColor = (status: string) => {
        switch (status) {
            case 'DRAFT':
                return 'bg-gray-100 text-gray-800';
            case 'SENT':
                return 'bg-blue-100 text-blue-800';
            case 'PARTIALLY_RECEIVED':
                return 'bg-yellow-100 text-yellow-800';
            case 'RECEIVED':
                return 'bg-green-100 text-green-800';
            case 'CANCELLED':
                return 'bg-red-100 text-red-800';
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
                <h1 className="text-2xl font-bold">Purchase Orders</h1>
                <Button asChild className="shrink-0">
                    <Link to="/purchase-orders/new">
                        <Plus className="h-4 w-4 mr-2" />
                        New Purchase Order
                    </Link>
                </Button>
            </div>

            <div className="rounded-md border overflow-x-auto">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>PO Number</TableHead>
                            <TableHead>Supplier</TableHead>
                            <TableHead>Order Date</TableHead>
                            <TableHead>Status</TableHead>
                            <TableHead className="text-right">Total</TableHead>
                            <TableHead className="w-[100px]">Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {data?.content?.map((po) => (
                            <TableRow key={po.id}>
                                <TableCell className="font-medium">{po.poNumber}</TableCell>
                                <TableCell>{po.supplierName}</TableCell>
                                <TableCell>
                                    {new Date(po.orderDate).toLocaleDateString()}
                                </TableCell>
                                <TableCell>
                                    <span
                                        className={`px-2 py-1 rounded-full text-xs ${getStatusColor(
                                            po.status
                                        )}`}
                                    >
                                        {PURCHASE_ORDER_STATUS_LABELS[po.status]}
                                    </span>
                                </TableCell>
                                <TableCell className="text-right">
                                    ${po.totalAmount.toFixed(2)}
                                </TableCell>
                                <TableCell>
                                    <div className="flex gap-2">
                                        <Button variant="ghost" size="icon" asChild>
                                            <Link to={`/purchase-orders/${po.id}`}>
                                                <Pencil className="h-4 w-4" />
                                            </Link>
                                        </Button>
                                        {po.status === 'DRAFT' && (
                                            <Button
                                                variant="ghost"
                                                size="icon"
                                                onClick={() => handleDelete(po.id, po.poNumber)}
                                            >
                                                <Trash2 className="h-4 w-4" />
                                            </Button>
                                        )}
                                    </div>
                                </TableCell>
                            </TableRow>
                        ))}
                        {(!data?.content || data.content.length === 0) && (
                            <TableRow>
                                <TableCell colSpan={6} className="text-center py-8">
                                    No purchase orders found
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

export default PurchaseOrderListPage;
