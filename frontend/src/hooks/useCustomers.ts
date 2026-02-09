import { useQuery } from '@tanstack/react-query';
import { useApi } from '@/hooks/useApi';
import type { Customer, PaginatedResponse } from '@/types';

interface CustomerSearchParams {
    page: number;
    size: number;
    searchTerm?: string;
    companyId: string | null;
}

export const useCustomers = ({ page, size, searchTerm, companyId }: CustomerSearchParams) => {
    const api = useApi();

    return useQuery({
        queryKey: ['customers', companyId, page, size, searchTerm],
        queryFn: async () => {
            if (!companyId) return null;

            let url = `/api/companies/${companyId}/clients`;
            const params: any = { page, size };

            if (searchTerm) {
                url += `/search`; // Assuming search endpoint exists or filtering happens
                params.searchTerm = searchTerm;
            }

            const { data } = await api.get<PaginatedResponse<Customer>>(url, { params });
            return data;
        },
        enabled: !!companyId,
    });
};



export const useCustomer = (id: string | undefined) => {
    const api = useApi();

    return useQuery({
        queryKey: ['customer', id],
        queryFn: async () => {
            if (!id || id === 'new') return null;
            // The endpoint for a single client might be /api/clients/{id} based on REST standards,
            // OR /api/companies/{companyId}/clients/{id}. 
            // Checking old code logic usually implies /api/clients/{id} is standard for globally unique IDs.
            // Let's assume /api/clients/{id} for now, if it fails we check the legacy code.
            const { data } = await api.get<Customer>(`/api/clients/${id}`);
            return data;
        },
        enabled: !!id && id !== 'new',
    });
};

import { useMutation, useQueryClient } from '@tanstack/react-query';

export const useCreateCustomer = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ companyId, ...customer }: Partial<Customer> & { companyId: string }) => {
            const { data } = await api.post<Customer>(`/api/companies/${companyId}/clients`, customer);
            return data;
        },
        onSuccess: (_, variables) => {
            queryClient.invalidateQueries({ queryKey: ['customers', variables.companyId] });
        },
    });
};

export const useUpdateCustomer = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ id, companyId, ...customer }: Partial<Customer> & { id: string, companyId: string }) => {
            // Usually update is PUT /api/clients/{id}
            const { data } = await api.put<Customer>(`/api/clients/${id}`, customer);
            return data;
        },
        onSuccess: (_, variables) => {
            queryClient.invalidateQueries({ queryKey: ['customers', variables.companyId] });
            queryClient.invalidateQueries({ queryKey: ['customer', variables.id] });
        },
    });
};

export const useCustomerSearch = (companyId: string | null, searchTerm: string) => {
    const api = useApi();
    return useQuery({
        queryKey: ['customers-search', companyId, searchTerm],
        queryFn: async () => {
            if (!companyId || !searchTerm) return [];
            const { data } = await api.get<PaginatedResponse<Customer>>(`/api/companies/${companyId}/clients?size=100`);
            return data.content.filter(c =>
                c.firstName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                c.lastName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                c.clientEntityName?.toLowerCase().includes(searchTerm.toLowerCase())
            );
        },
        enabled: !!companyId && searchTerm.length > 0
    })
}
