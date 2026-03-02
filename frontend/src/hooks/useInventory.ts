import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useApi } from '@/hooks/useApi';
import type { InventoryDashboard, LowStockAlert, StockMovement, PaginatedResponse } from '@/types';

export const useInventoryDashboard = (companyId: number | null) => {
    const api = useApi();
    return useQuery<InventoryDashboard>({
        queryKey: ['inventory', 'dashboard', companyId],
        queryFn: async () => {
            const { data } = await api.get(`/api/companies/${companyId}/inventory/dashboard`);
            return data;
        },
        enabled: !!companyId,
    });
};

export const useLowStockAlerts = (companyId: number | null) => {
    const api = useApi();
    return useQuery<LowStockAlert[]>({
        queryKey: ['inventory', 'low-stock', companyId],
        queryFn: async () => {
            const { data } = await api.get(`/api/companies/${companyId}/inventory/low-stock`);
            return data;
        },
        enabled: !!companyId,
    });
};

interface UseStockMovementsParams {
    page?: number;
    size?: number;
    companyId: number | null;
}

export const useStockMovements = ({ page = 0, size = 20, companyId }: UseStockMovementsParams) => {
    const api = useApi();
    return useQuery<PaginatedResponse<StockMovement>>({
        queryKey: ['inventory', 'movements', companyId, page, size],
        queryFn: async () => {
            const { data } = await api.get(`/api/companies/${companyId}/inventory/movements`, {
                params: { page, size },
            });
            return data;
        },
        enabled: !!companyId,
    });
};

export const useProductStockMovements = (productId: string, companyId: number | null) => {
    const api = useApi();
    return useQuery<StockMovement[]>({
        queryKey: ['inventory', 'movements', 'product', companyId, productId],
        queryFn: async () => {
            const { data } = await api.get(
                `/api/companies/${companyId}/inventory/products/${productId}/movements`
            );
            return data;
        },
        enabled: !!companyId && !!productId,
    });
};

export const useAdjustStock = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ productId, companyId, newQuantity, reason }: { productId: string; companyId: number; newQuantity: number; reason: string }) => {
            const { data } = await api.post(
                `/api/companies/${companyId}/inventory/products/${productId}/adjust`,
                { newQuantity, reason }
            );
            return data;
        },
        onSuccess: (_, { companyId }) => {
            queryClient.invalidateQueries({ queryKey: ['inventory', companyId] });
            queryClient.invalidateQueries({ queryKey: ['products', companyId] });
        },
    });
};
