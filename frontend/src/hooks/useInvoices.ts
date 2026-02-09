import { useQuery } from '@tanstack/react-query';
import { useApi } from './useApi';
import type { PaginatedResponse, Invoice } from '@/types'; // Need to add Invoice type to index.ts

interface InvoiceSearchParams {
    page: number;
    size: number;
    searchTerm?: string;
    companyId: string | null;
}

export const useInvoices = ({ page, size, searchTerm, companyId }: InvoiceSearchParams) => {
    const api = useApi();

    return useQuery({
        queryKey: ['invoices', companyId, page, size, searchTerm],
        queryFn: async () => {
            if (!companyId) return null;

            let url = `/api/companies/${companyId}/invoices`;
            const params: any = { page, size };

            if (searchTerm) {
                url += `/search`;
                params.searchTerm = searchTerm;
            }

            const { data } = await api.get<PaginatedResponse<Invoice>>(url, { params });
            return data;
        },
        enabled: !!companyId,
        placeholderData: (previousData) => previousData,
    });
};

export const useInvoice = (id: string, companyId: string | null) => {
    const api = useApi();
    return useQuery({
        queryKey: ['invoice', id],
        queryFn: async () => {
            if (!companyId || id === 'new') return null;
            const { data } = await api.get<Invoice>(`/api/companies/${companyId}/invoices/${id}`);
            return data;
        },
        enabled: !!companyId && id !== 'new',
    });
}
