import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useApi } from '@/hooks/useApi';
import type { PaginatedResponse, Service } from '@/types';

interface ServiceSearchParams {
    page: number;
    size: number;
    searchTerm?: string;
    companyId: string | null;
}

export const useServices = ({ page, size, searchTerm, companyId }: ServiceSearchParams) => {
    const api = useApi();

    return useQuery({
        queryKey: ['services', companyId, page, size, searchTerm],
        queryFn: async () => {
            if (!companyId) return null;

            let url = `/api/companies/${companyId}/services`;
            const params: any = { page, size };

            if (searchTerm) {
                url += `/search`;
                params.searchTerm = searchTerm;
            }

            const { data } = await api.get<PaginatedResponse<Service>>(url, { params });
            return data;
        },
        enabled: !!companyId,
    });
};

export const useService = (id: string | undefined) => {
    const api = useApi();

    return useQuery({
        queryKey: ['service', id],
        queryFn: async () => {
            if (!id || id === 'new') return null;
            const { data } = await api.get<Service>(`/api/services/${id}`);
            return data;
        },
        enabled: !!id && id !== 'new',
    });
};

export const useCreateService = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ companyId, ...service }: Partial<Service> & { companyId: string }) => {
            const { data } = await api.post<Service>(`/api/companies/${companyId}/services`, service);
            return data;
        },
        onSuccess: (_, variables) => {
            queryClient.invalidateQueries({ queryKey: ['services', variables.companyId] });
        },
    });
};

export const useUpdateService = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ id, companyId, ...service }: Partial<Service> & { id: string, companyId: string }) => {
            const { data } = await api.put<Service>(`/api/services/${id}`, service);
            return data;
        },
        onSuccess: (_, variables) => {
            queryClient.invalidateQueries({ queryKey: ['services', variables.companyId] });
            queryClient.invalidateQueries({ queryKey: ['service', variables.id] });
        },
    });
};
