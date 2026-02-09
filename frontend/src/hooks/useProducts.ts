import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useApi } from '@/hooks/useApi';
import type { PaginatedResponse, Product } from '@/types'; // Need Product type

interface ProductSearchParams {
    page: number;
    size: number;
    searchTerm?: string;
    companyId: string | null;
}

export const useProducts = ({ page, size, searchTerm, companyId }: ProductSearchParams) => {
    const api = useApi();

    return useQuery({
        queryKey: ['products', companyId, page, size, searchTerm],
        queryFn: async () => {
            if (!companyId) return null;

            let url = `/api/companies/${companyId}/products`;
            const params: any = { page, size };

            if (searchTerm) {
                url += `/search`;
                params.searchTerm = searchTerm;
            }

            const { data } = await api.get<PaginatedResponse<Product>>(url, { params });
            return data;
        },
        enabled: !!companyId,
    });
};

export const useProduct = (id: string | undefined) => {
    const api = useApi();

    return useQuery({
        queryKey: ['product', id],
        queryFn: async () => {
            if (!id || id === 'new') return null;
            const { data } = await api.get<Product>(`/api/products/${id}`);
            return data;
        },
        enabled: !!id && id !== 'new',
    });
};

export const useCreateProduct = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ companyId, ...product }: Partial<Product> & { companyId: string }) => {
            const { data } = await api.post<Product>(`/api/companies/${companyId}/products`, product);
            return data;
        },
        onSuccess: (_, variables) => {
            queryClient.invalidateQueries({ queryKey: ['products', variables.companyId] });
        },
    });
};

export const useUpdateProduct = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ id, companyId, ...product }: Partial<Product> & { id: string, companyId: string }) => {
            const { data } = await api.put<Product>(`/api/products/${id}`, product);
            return data;
        },
        onSuccess: (_, variables) => {
            queryClient.invalidateQueries({ queryKey: ['products', variables.companyId] });
            queryClient.invalidateQueries({ queryKey: ['product', variables.id] });
        },
    });
};
