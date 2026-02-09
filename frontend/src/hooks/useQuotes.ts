import { useQuery } from '@tanstack/react-query';
import { useApi } from './useApi';
import type { PaginatedResponse, Quote } from '@/types';

interface QuoteSearchParams {
    page: number;
    size: number;
    searchTerm?: string;
    companyId: string | null;
    showAllRevisions?: boolean;
}

export const useQuotes = ({ page, size, searchTerm, companyId, showAllRevisions = false }: QuoteSearchParams) => {
    const api = useApi();

    return useQuery({
        queryKey: ['quotes', companyId, page, size, searchTerm, showAllRevisions],
        queryFn: async () => {
            if (!companyId) return null;

            let url = `/api/companies/${companyId}/quotes`;

            // Native Query (latest revisions) requires snake_case 'quote_date'
            // JPA Query (all revisions) requires camelCase 'quoteDate'
            const sortField = showAllRevisions ? 'quoteDate' : 'quote_date';

            const params: any = {
                page,
                size,
                sort: `${sortField},desc`
            };

            if (searchTerm) {
                url += `/search`;
                params.searchTerm = searchTerm;
                if (showAllRevisions) {
                    params.showAllRevisions = true;
                }
            } else {
                // Use different endpoint based on showAllRevisions flag
                url += showAllRevisions ? `` : `/quotenumbers`;
                if (showAllRevisions) {
                    params.showAllRevisions = true;
                }
            }

            const { data } = await api.get<PaginatedResponse<Quote>>(url, { params });
            return data;
        },
        enabled: !!companyId, // Only fetch if companyId is present
        placeholderData: (previousData) => previousData, // Keep previous data while fetching new page
    });
};

export const useQuote = (id: string, companyId: string | null) => {
    const api = useApi();
    return useQuery({
        queryKey: ['quote', id],
        queryFn: async () => {
            if (!companyId || id === 'new') return null;
            const { data } = await api.get<Quote>(`/api/companies/${companyId}/quotes/${id}`);
            return data;
        },
        enabled: !!companyId && id !== 'new',
    });
}
