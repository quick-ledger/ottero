import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useApi } from '@/hooks/useApi';
import { z } from 'zod';

// We don't have a strict schema for Asset instances yet as they are dynamic
// But we know they have at least these:
export interface Asset {
    id: string;
    name: string;
    description?: string;
    productDefinitionId?: string; // or definitionId
    attributes?: Record<string, any>; // Dynamic attributes
    // Add other common fields if known
}

export type AssetFormValues = {
    name: string;
    description?: string;
    productDefinitionId: string;
    attributes: Record<string, any>;
};

interface AssetSearchParams {
    companyId: string | null;
}

export const useAssets = ({ companyId }: AssetSearchParams) => {
    const api = useApi();

    return useQuery({
        queryKey: ['assets', companyId],
        queryFn: async () => {
            if (!companyId) return [];
            // Trying company scoped first as per v2 pattern
            try {
                const { data } = await api.get<Asset[]>(`/api/companies/${companyId}/assets`);
                return data;
            } catch (e) {
                // Fallback to legacy if needed, or maybe it is /api/assets?
                // But /api/assets in legacy likely required auth context or cookie, which we have.
                // Let's assume v2 backend has consistent routes. 
                // If /api/assets returns ALL assets for user, we might need to filter by company or backend does it.
                // Let's stick to company scoped path if we can.
                console.warn("Failed to fetch company assets, trying global...", e);
                const { data } = await api.get<Asset[]>('/api/assets');
                return data;
            }
        },
        enabled: !!companyId,
    });
};

export const useAsset = (id: string | undefined) => {
    const api = useApi();

    return useQuery({
        queryKey: ['asset', id],
        queryFn: async () => {
            if (!id || id === 'new') return null;
            // Try specific endpoint
            const { data } = await api.get<Asset>(`/api/assets/${id}`);
            return data;
        },
        enabled: !!id && id !== 'new',
    });
};

export const useCreateAsset = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ companyId, ...asset }: AssetFormValues & { companyId: string }) => {
            // Assuming structure: POST /api/companies/{id}/assets
            const { data } = await api.post<Asset>(`/api/companies/${companyId}/assets`, asset);
            return data;
        },
        onSuccess: (_, variables) => {
            queryClient.invalidateQueries({ queryKey: ['assets', variables.companyId] });
        },
    });
};

export const useUpdateAsset = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ id, companyId, ...asset }: AssetFormValues & { id: string, companyId: string }) => {
            // Assuming PUT /api/assets/{id} or company scoped
            const { data } = await api.put<Asset>(`/api/assets/${id}`, asset);
            return data;
        },
        onSuccess: (_, variables) => {
            queryClient.invalidateQueries({ queryKey: ['assets', variables.companyId] });
            queryClient.invalidateQueries({ queryKey: ['asset', variables.id] });
        },
    });
};

export const useDeleteAsset = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ id, companyId }: { id: string, companyId: string }) => {
            await api.delete(`/api/assets/${id}`);
        },
        onSuccess: (_, variables) => {
            queryClient.invalidateQueries({ queryKey: ['assets', variables.companyId] });
        },
    });
}
