import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useApi } from '@/hooks/useApi';
import type { PaginatedResponse } from '@/types';
import type { AssetDefinitionFormValues } from '@/types/schemas';

// We might not have a strict type for AssetDefinition response yet, but it matches the form values mostly
export interface AssetDefinition extends AssetDefinitionFormValues {
    id: string;
    // potential metadata
}

interface AssetDefSearchParams {
    page: number;
    size: number;
    companyId: string | null;
}

export const useAssetDefinitions = ({ page, size, companyId }: AssetDefSearchParams) => {
    const api = useApi();

    return useQuery({
        queryKey: ['asset-definitions', companyId, page, size],
        queryFn: async () => {
            if (!companyId) return null;
            // Legacy endpoint: /api/companies/${selectedCompanyId}/product-definition
            // It seems it returns a list, not paginated in legacy code? 
            // Legacy: const response = await api.get(`/api/companies/${selectedCompanyId}/product-definition`);
            // setAssetDefs(response.data);
            // So it returns an array. We can wrap it in a pseudo-paginated structure if we want to keep consistency,
            // or just return the array. Let's return the array for now, or check if backend supports paging.
            // If legacy didn't send page params, likely it returns all.
            const { data } = await api.get<AssetDefinition[]>(`/api/companies/${companyId}/product-definition`);
            return data;
        },
        enabled: !!companyId,
    });
};

export const useAssetDefinition = (id: string | undefined) => {
    const api = useApi();
    const companyId = localStorage.getItem('selectedCompanyId'); // Fallback or strict requirement? simpler to pass it or rely on caching

    // In legacy: api.get(`/api/companies/${selectedCompanyId}/product-definition/${id}/tabular`)
    // We need companyId here.

    return useQuery({
        queryKey: ['asset-definition', id], // We might need companyId in key if ID is not globally unique
        queryFn: async () => {
            if (!id || id === 'new') return null;
            // We need to resolve companyId. Since hooks use closure, we can't easily access the store unless passed.
            // However, the legacy URL structure requires it.
            // We'll update the hook signature to require companyId.
            throw new Error("Use useAssetDefinitionWithCompany logic");
        },
        enabled: false // Disable this signature
    });
};

export const useAssetDefinitionWithCompany = (id: string | undefined, companyId: string | null) => {
    const api = useApi();

    return useQuery({
        queryKey: ['asset-definition', companyId, id],
        queryFn: async () => {
            if (!companyId || !id || id === 'new') return null;
            const { data } = await api.get<AssetDefinition>(`/api/companies/${companyId}/product-definition/${id}/tabular`);
            return data;
        },
        enabled: !!companyId && !!id && id !== 'new',
    });
}

export const useCreateAssetDefinition = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ companyId, ...def }: AssetDefinitionFormValues & { companyId: string }) => {
            const { data } = await api.post<AssetDefinition>(`/api/companies/${companyId}/product-definition`, def);
            return data;
        },
        onSuccess: (_, variables) => {
            queryClient.invalidateQueries({ queryKey: ['asset-definitions', variables.companyId] });
        },
    });
};

export const useUpdateAssetDefinition = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ id, companyId, ...def }: AssetDefinitionFormValues & { id: string, companyId: string }) => {
            const { data } = await api.put<AssetDefinition>(`/api/companies/${companyId}/product-definition`, { ...def, id });
            return data;
        },
        onSuccess: (_, variables) => {
            queryClient.invalidateQueries({ queryKey: ['asset-definitions', variables.companyId] });
            queryClient.invalidateQueries({ queryKey: ['asset-definition', variables.companyId, variables.id] });
        },
    });
};
