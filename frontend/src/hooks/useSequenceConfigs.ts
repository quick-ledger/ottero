import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useApi } from '@/hooks/useApi';

export interface SequenceConfig {
    id?: string;
    prefix: string;
    postfix: string;
    currentNumber: number;
    numberPadding: number;
    type: 'QUOTE' | 'INVOICE';
}

export const useSequenceConfig = (companyId: string | null, type: 'QUOTE' | 'INVOICE') => {
    const api = useApi();

    return useQuery({
        queryKey: ['sequence-config', companyId, type],
        queryFn: async () => {
            if (!companyId) return null;
            const { data } = await api.get<SequenceConfig>(`/api/companies/${companyId}/sequence-configs/type/${type}`);
            return data;
        },
        enabled: !!companyId,
    });
};

export const useUpdateSequenceConfig = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ companyId, config }: { companyId: string, config: SequenceConfig }) => {
            const { data } = await api.put<SequenceConfig>(`/api/companies/${companyId}/sequence-configs`, config);
            return data;
        },
        onSuccess: (_, variables) => {
            queryClient.invalidateQueries({ queryKey: ['sequence-config', variables.companyId, variables.config.type] });
        },
    });
};
