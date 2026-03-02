import { useParams, useNavigate } from 'react-router-dom';
import { useSelectedCompanyId } from '@/store/useAppStore';
import { usePurchaseOrder } from '@/hooks/usePurchaseOrders';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { ArrowLeft } from 'lucide-react';

const PurchaseOrderEditPage = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const companyId = useSelectedCompanyId();
    const isNew = id === 'new';

    const { data: purchaseOrder, isLoading } = usePurchaseOrder(id || 'new', companyId);

    if (isLoading && !isNew) {
        return <div className="p-8">Loading...</div>;
    }

    return (
        <div className="container mx-auto py-8 px-4 max-w-4xl">
            <Button variant="ghost" onClick={() => navigate('/purchase-orders')} className="mb-4">
                <ArrowLeft className="h-4 w-4 mr-2" />
                Back to Purchase Orders
            </Button>

            <Card>
                <CardHeader>
                    <CardTitle>
                        {isNew ? 'New Purchase Order' : `Purchase Order ${purchaseOrder?.poNumber}`}
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="text-center py-12 text-muted-foreground">
                        <p>Purchase Order edit form coming soon.</p>
                        <p className="text-sm mt-2">
                            This page will allow you to create and edit purchase orders with line items.
                        </p>
                    </div>
                </CardContent>
            </Card>
        </div>
    );
};

export default PurchaseOrderEditPage;
