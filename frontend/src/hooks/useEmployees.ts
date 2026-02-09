import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useApi } from '@/hooks/useApi';
import type { EmployeeFormValues } from '@/types/schemas';

export interface Employee extends EmployeeFormValues {
    id: string;
}

interface EmployeeSearchParams {
    page?: number;
    size?: number;
    companyId: string | null;
}

export const useEmployees = ({ companyId }: EmployeeSearchParams) => {
    const api = useApi();

    return useQuery({
        queryKey: ['employees', companyId],
        queryFn: async () => {
            if (!companyId) return [];
            // Try company scoped
            try {
                const { data } = await api.get<Employee[]>(`/api/companies/${companyId}/employees`);
                return data;
            } catch (e) {
                // The legacy URL was /employee, which is weird.
                // Let's assume standard pattern or global
                const { data } = await api.get<Employee[]>('/api/employees');
                return data;
            }
        },
        enabled: !!companyId,
    });
};

export const useEmployee = (id: string | undefined) => {
    const api = useApi();

    return useQuery({
        queryKey: ['employee', id],
        queryFn: async () => {
            if (!id || id === 'new') return null;
            // Try company scoped PUT, but GET usually has ID.
            const { data } = await api.get<Employee>(`/api/employees/${id}`);
            return data;
        },
        enabled: !!id && id !== 'new',
    });
};

export const useCreateEmployee = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ companyId, ...employee }: EmployeeFormValues & { companyId: string }) => {
            const { data } = await api.post<Employee>(`/api/companies/${companyId}/employees`, employee);
            return data;
        },
        onSuccess: (_, variables) => {
            queryClient.invalidateQueries({ queryKey: ['employees', variables.companyId] });
        },
    });
};

export const useUpdateEmployee = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ id, companyId, ...employee }: EmployeeFormValues & { id: string, companyId: string }) => {
            const { data } = await api.put<Employee>(`/api/employees/${id}`, employee);
            return data;
        },
        onSuccess: (_, variables) => {
            queryClient.invalidateQueries({ queryKey: ['employees', variables.companyId] });
            queryClient.invalidateQueries({ queryKey: ['employee', variables.id] });
        },
    });
};

export const useDeleteEmployee = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ id }: { id: string, companyId: string }) => {
            await api.delete(`/api/employees/${id}`);
        },
        onSuccess: (_, variables) => {
            queryClient.invalidateQueries({ queryKey: ['employees', variables.companyId] });
        },
    });
}

