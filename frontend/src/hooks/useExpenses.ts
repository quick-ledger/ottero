import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useApi } from './useApi';
import type { PaginatedResponse, Expense, ExpenseSummary } from '@/types';

interface ExpenseSearchParams {
    page: number;
    size: number;
    searchTerm?: string;
    companyId: string | null;
}

export const useExpenses = ({ page, size, searchTerm, companyId }: ExpenseSearchParams) => {
    const api = useApi();

    return useQuery({
        queryKey: ['expenses', companyId, page, size, searchTerm],
        queryFn: async () => {
            if (!companyId) return null;

            let url = `/api/companies/${companyId}/expenses`;
            const params: Record<string, string | number> = { page, size };

            if (searchTerm) {
                url += `/search`;
                params.searchTerm = searchTerm;
            }

            const { data } = await api.get<PaginatedResponse<Expense>>(url, { params });
            return data;
        },
        enabled: !!companyId,
        placeholderData: (previousData) => previousData,
    });
};

export const useExpense = (id: string, companyId: string | null) => {
    const api = useApi();
    return useQuery({
        queryKey: ['expense', id],
        queryFn: async () => {
            if (!companyId || id === 'new') return null;
            const { data } = await api.get<Expense>(`/api/companies/${companyId}/expenses/${id}`);
            return data;
        },
        enabled: !!companyId && id !== 'new',
    });
};

export const useExpenseSummary = (companyId: string | null, financialYear?: string) => {
    const api = useApi();
    return useQuery({
        queryKey: ['expense-summary', companyId, financialYear],
        queryFn: async () => {
            if (!companyId) return null;
            const params = financialYear ? { financialYear } : {};
            const { data } = await api.get<ExpenseSummary>(
                `/api/companies/${companyId}/expenses/summary`,
                { params }
            );
            return data;
        },
        enabled: !!companyId,
    });
};

export const useDeleteExpense = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ id, companyId }: { id: string; companyId: string }) => {
            await api.delete(`/api/companies/${companyId}/expenses/${id}`);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['expenses'] });
            queryClient.invalidateQueries({ queryKey: ['expense-summary'] });
        },
    });
};
