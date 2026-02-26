import { useQuery } from '@tanstack/react-query';
import { useApi } from './useApi';

export interface MonthlyRevenue {
    month: string;
    revenue: number;
    invoiceCount: number;
}

export interface OutstandingInvoices {
    count: number;
    totalAmount: number;
}

export interface TopCustomer {
    customerId: number;
    customerName: string;
    totalRevenue: number;
    invoiceCount: number;
}

export function useMonthlyRevenue(companyId: number | null) {
    const api = useApi();

    return useQuery<MonthlyRevenue[]>({
        queryKey: ['stats', 'revenue', companyId],
        queryFn: async () => {
            const response = await api.get(`/api/companies/${companyId}/stats/revenue`);
            return response.data;
        },
        enabled: !!companyId,
        staleTime: 5 * 60 * 1000, // 5 minutes
    });
}

export function useOutstandingInvoices(companyId: number | null) {
    const api = useApi();

    return useQuery<OutstandingInvoices>({
        queryKey: ['stats', 'outstanding', companyId],
        queryFn: async () => {
            const response = await api.get(`/api/companies/${companyId}/stats/outstanding`);
            return response.data;
        },
        enabled: !!companyId,
        staleTime: 5 * 60 * 1000,
    });
}

export function useTopCustomers(companyId: number | null, limit = 5) {
    const api = useApi();

    return useQuery<TopCustomer[]>({
        queryKey: ['stats', 'top-customers', companyId, limit],
        queryFn: async () => {
            const response = await api.get(`/api/companies/${companyId}/stats/top-customers?limit=${limit}`);
            return response.data;
        },
        enabled: !!companyId,
        staleTime: 5 * 60 * 1000,
    });
}
