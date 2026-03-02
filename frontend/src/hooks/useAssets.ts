import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useApi } from '@/hooks/useApi';
import type { Asset, PaginatedResponse } from '@/types';

interface UseAssetsParams {
    page?: number;
    size?: number;
    companyId: number | null;
}

export const useAssets = ({ page = 0, size = 20, companyId }: UseAssetsParams) => {
    const api = useApi();
    return useQuery<PaginatedResponse<Asset>>({
        queryKey: ['assets', companyId, page, size],
        queryFn: async () => {
            const { data } = await api.get(`/api/companies/${companyId}/assets`, {
                params: { page, size },
            });
            return data;
        },
        enabled: !!companyId,
    });
};

export const useAsset = (id: string, companyId: number | null) => {
    const api = useApi();
    return useQuery<Asset>({
        queryKey: ['assets', companyId, id],
        queryFn: async () => {
            const { data } = await api.get(`/api/companies/${companyId}/assets/${id}`);
            return data;
        },
        enabled: !!companyId && id !== 'new',
    });
};

export const useSaveAsset = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ asset, companyId }: { asset: Partial<Asset>; companyId: number }) => {
            if (asset.id) {
                const { data } = await api.put(
                    `/api/companies/${companyId}/assets/${asset.id}`,
                    asset
                );
                return data;
            } else {
                const { data } = await api.post(`/api/companies/${companyId}/assets`, asset);
                return data;
            }
        },
        onSuccess: (_, { companyId }) => {
            queryClient.invalidateQueries({ queryKey: ['assets', companyId] });
        },
    });
};

export const useDeleteAsset = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ id, companyId }: { id: string; companyId: number }) => {
            await api.delete(`/api/companies/${companyId}/assets/${id}`);
        },
        onSuccess: (_, { companyId }) => {
            queryClient.invalidateQueries({ queryKey: ['assets', companyId] });
        },
    });
};
