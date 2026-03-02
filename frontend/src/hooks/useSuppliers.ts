import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useApi } from '@/hooks/useApi';
import type { Supplier, PaginatedResponse } from '@/types';

interface UseSupplierParams {
    page?: number;
    size?: number;
    companyId: number | null;
}

export const useSuppliers = ({ page = 0, size = 20, companyId }: UseSupplierParams) => {
    const api = useApi();
    return useQuery<PaginatedResponse<Supplier>>({
        queryKey: ['suppliers', companyId, page, size],
        queryFn: async () => {
            const { data } = await api.get(`/api/companies/${companyId}/suppliers`, {
                params: { page, size },
            });
            return data;
        },
        enabled: !!companyId,
    });
};

export const useAllSuppliers = (companyId: number | null) => {
    const api = useApi();
    return useQuery<Supplier[]>({
        queryKey: ['suppliers', 'all', companyId],
        queryFn: async () => {
            const { data } = await api.get(`/api/companies/${companyId}/suppliers/all`);
            return data;
        },
        enabled: !!companyId,
    });
};

export const useSupplier = (id: string, companyId: number | null) => {
    const api = useApi();
    return useQuery<Supplier>({
        queryKey: ['suppliers', companyId, id],
        queryFn: async () => {
            const { data } = await api.get(`/api/companies/${companyId}/suppliers/${id}`);
            return data;
        },
        enabled: !!companyId && id !== 'new',
    });
};

export const useSaveSupplier = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ supplier, companyId }: { supplier: Partial<Supplier>; companyId: number }) => {
            if (supplier.id) {
                const { data } = await api.put(
                    `/api/companies/${companyId}/suppliers/${supplier.id}`,
                    supplier
                );
                return data;
            } else {
                const { data } = await api.post(`/api/companies/${companyId}/suppliers`, supplier);
                return data;
            }
        },
        onSuccess: (_, { companyId }) => {
            queryClient.invalidateQueries({ queryKey: ['suppliers', companyId] });
        },
    });
};

export const useDeleteSupplier = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ id, companyId }: { id: string; companyId: number }) => {
            await api.delete(`/api/companies/${companyId}/suppliers/${id}`);
        },
        onSuccess: (_, { companyId }) => {
            queryClient.invalidateQueries({ queryKey: ['suppliers', companyId] });
        },
    });
};
