import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useApi } from '@/hooks/useApi';
import type { PurchaseOrder, PaginatedResponse } from '@/types';

interface UsePurchaseOrdersParams {
    page?: number;
    size?: number;
    companyId: number | null;
}

export const usePurchaseOrders = ({ page = 0, size = 20, companyId }: UsePurchaseOrdersParams) => {
    const api = useApi();
    return useQuery<PaginatedResponse<PurchaseOrder>>({
        queryKey: ['purchase-orders', companyId, page, size],
        queryFn: async () => {
            const { data } = await api.get(`/api/companies/${companyId}/purchase-orders`, {
                params: { page, size },
            });
            return data;
        },
        enabled: !!companyId,
    });
};

export const usePurchaseOrder = (id: string, companyId: number | null) => {
    const api = useApi();
    return useQuery<PurchaseOrder>({
        queryKey: ['purchase-orders', companyId, id],
        queryFn: async () => {
            const { data } = await api.get(`/api/companies/${companyId}/purchase-orders/${id}`);
            return data;
        },
        enabled: !!companyId && id !== 'new',
    });
};

export const useSavePurchaseOrder = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ purchaseOrder, companyId }: { purchaseOrder: Partial<PurchaseOrder>; companyId: number }) => {
            if (purchaseOrder.id) {
                const { data } = await api.put(
                    `/api/companies/${companyId}/purchase-orders/${purchaseOrder.id}`,
                    purchaseOrder
                );
                return data;
            } else {
                const { data } = await api.post(
                    `/api/companies/${companyId}/purchase-orders`,
                    purchaseOrder
                );
                return data;
            }
        },
        onSuccess: (_, { companyId }) => {
            queryClient.invalidateQueries({ queryKey: ['purchase-orders', companyId] });
        },
    });
};

export const useDeletePurchaseOrder = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ id, companyId }: { id: string; companyId: number }) => {
            await api.delete(`/api/companies/${companyId}/purchase-orders/${id}`);
        },
        onSuccess: (_, { companyId }) => {
            queryClient.invalidateQueries({ queryKey: ['purchase-orders', companyId] });
        },
    });
};

export const useSendPurchaseOrder = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ id, companyId }: { id: string; companyId: number }) => {
            const { data } = await api.post(`/api/companies/${companyId}/purchase-orders/${id}/send`);
            return data;
        },
        onSuccess: (_, { companyId }) => {
            queryClient.invalidateQueries({ queryKey: ['purchase-orders', companyId] });
        },
    });
};

interface ReceiveItemsDto {
    items: Array<{
        purchaseOrderItemId: string;
        quantityReceived: number;
    }>;
}

export const useReceivePurchaseOrderItems = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ id, companyId, receiveItems }: { id: string; companyId: number; receiveItems: ReceiveItemsDto }) => {
            const { data } = await api.post(
                `/api/companies/${companyId}/purchase-orders/${id}/receive`,
                receiveItems
            );
            return data;
        },
        onSuccess: (_, { companyId }) => {
            queryClient.invalidateQueries({ queryKey: ['purchase-orders', companyId] });
            queryClient.invalidateQueries({ queryKey: ['inventory', companyId] });
        },
    });
};
