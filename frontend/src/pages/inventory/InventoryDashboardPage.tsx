import { useSelectedCompanyId } from '@/store/useAppStore';
import { useInventoryDashboard } from '@/hooks/useInventory';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Package, AlertTriangle, PackageX, DollarSign } from 'lucide-react';
import { Link } from 'react-router-dom';

const InventoryDashboardPage = () => {
    const companyId = useSelectedCompanyId();
    const { data: dashboard, isLoading } = useInventoryDashboard(companyId);

    if (isLoading) {
        return <div className="p-8">Loading...</div>;
    }

    if (!dashboard) {
        return <div className="p-8">No inventory data available</div>;
    }

    return (
        <div className="container mx-auto py-8 px-4">
            <h1 className="text-2xl font-bold mb-6">Inventory Dashboard</h1>

            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4 mb-8">
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Total Products</CardTitle>
                        <Package className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{dashboard.totalProducts}</div>
                        <p className="text-xs text-muted-foreground">with inventory tracking</p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Low Stock</CardTitle>
                        <AlertTriangle className="h-4 w-4 text-yellow-500" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{dashboard.lowStockCount}</div>
                        <p className="text-xs text-muted-foreground">below reorder point</p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Out of Stock</CardTitle>
                        <PackageX className="h-4 w-4 text-red-500" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{dashboard.outOfStockCount}</div>
                        <p className="text-xs text-muted-foreground">need restocking</p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Inventory Value</CardTitle>
                        <DollarSign className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            ${dashboard.totalInventoryValue.toLocaleString()}
                        </div>
                        <p className="text-xs text-muted-foreground">total stock value</p>
                    </CardContent>
                </Card>
            </div>

            {dashboard.lowStockAlerts.length > 0 && (
                <Card>
                    <CardHeader>
                        <CardTitle>Low Stock Alerts</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="space-y-2">
                            {dashboard.lowStockAlerts.map((alert) => (
                                <div
                                    key={alert.productId}
                                    className="flex items-center justify-between p-3 bg-yellow-50 rounded-lg"
                                >
                                    <div>
                                        <span className="font-medium">{alert.productName}</span>
                                        {alert.productCode && (
                                            <span className="text-muted-foreground ml-2">
                                                ({alert.productCode})
                                            </span>
                                        )}
                                    </div>
                                    <div className="text-sm">
                                        <span className="text-red-600 font-medium">
                                            {alert.quantityOnHand}
                                        </span>
                                        <span className="text-muted-foreground">
                                            {' '}/ reorder at {alert.reorderPoint}
                                        </span>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </CardContent>
                </Card>
            )}

            <div className="mt-6 flex gap-4">
                <Link
                    to="/inventory/movements"
                    className="text-primary hover:underline"
                >
                    View Stock Movements
                </Link>
                <Link
                    to="/purchase-orders"
                    className="text-primary hover:underline"
                >
                    Create Purchase Order
                </Link>
            </div>
        </div>
    );
};

export default InventoryDashboardPage;
