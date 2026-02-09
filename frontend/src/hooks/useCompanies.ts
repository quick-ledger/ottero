import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useApi } from '@/hooks/useApi';
import type { Company } from '@/types';

export const useCompanies = () => {
    const api = useApi();

    return useQuery({
        queryKey: ['companies'],
        queryFn: async () => {
            const { data } = await api.get<Company[]>('/api/companies');
            return data;
        },
    });
};

export const useCompany = (id: string | undefined) => {
    const api = useApi();

    return useQuery({
        queryKey: ['company', id],
        queryFn: async () => {
            if (!id || id === 'new') return null;
            const { data } = await api.get<Company>(`/api/companies/${id}`);
            return data;
        },
        enabled: !!id && id !== 'new',
    });
};

export const useCreateCompany = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async (company: Partial<Company>) => {
            const { data } = await api.post<Company>('/api/companies', company);
            return data;
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['companies'] });
        },
    });
};

export const useUpdateCompany = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ id, ...company }: Partial<Company> & { id: string }) => {
            const { data } = await api.put<Company>(`/api/companies/${id}`, company);
            return data;
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['companies'] });
        },
    });
};

export const useDeleteCompany = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async (id: string) => {
            await api.delete(`/api/companies/${id}`);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['companies'] });
        },
    })
}
