import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useApi } from './useApi';
import type { PaginatedResponse, Job, JobNote, JobAttachment } from '@/types';

interface JobSearchParams {
    page: number;
    size: number;
    searchTerm?: string;
    companyId: string | null;
}

export const useJobs = ({ page, size, searchTerm, companyId }: JobSearchParams) => {
    const api = useApi();

    return useQuery({
        queryKey: ['jobs', companyId, page, size, searchTerm],
        queryFn: async () => {
            if (!companyId) return null;

            let url = `/api/companies/${companyId}/jobs`;
            const params: Record<string, string | number> = { page, size };

            if (searchTerm) {
                url += `/search`;
                params.searchTerm = searchTerm;
            }

            const { data } = await api.get<PaginatedResponse<Job>>(url, { params });
            return data;
        },
        enabled: !!companyId,
        placeholderData: (previousData) => previousData,
    });
};

export const useJob = (id: string, companyId: string | null) => {
    const api = useApi();
    return useQuery({
        queryKey: ['job', id],
        queryFn: async () => {
            if (!companyId || id === 'new') return null;
            const { data } = await api.get<Job>(`/api/companies/${companyId}/jobs/${id}`);
            return data;
        },
        enabled: !!companyId && id !== 'new',
    });
};

export const useDeleteJob = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ id, companyId }: { id: string; companyId: string }) => {
            await api.delete(`/api/companies/${companyId}/jobs/${id}`);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['jobs'] });
        },
    });
};

// Job Notes
export const useAddJobNote = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({
            jobId,
            companyId,
            note
        }: {
            jobId: string;
            companyId: string;
            note: { noteText: string; noteDate?: string }
        }) => {
            const { data } = await api.post<JobNote>(
                `/api/companies/${companyId}/jobs/${jobId}/notes`,
                note
            );
            return data;
        },
        onSuccess: (_, { jobId }) => {
            queryClient.invalidateQueries({ queryKey: ['job', jobId] });
        },
    });
};

export const useDeleteJobNote = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({
            jobId,
            noteId,
            companyId
        }: {
            jobId: string;
            noteId: string;
            companyId: string
        }) => {
            await api.delete(`/api/companies/${companyId}/jobs/${jobId}/notes/${noteId}`);
        },
        onSuccess: (_, { jobId }) => {
            queryClient.invalidateQueries({ queryKey: ['job', jobId] });
        },
    });
};

// Job Attachments
export const useUploadJobAttachment = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({
            jobId,
            companyId,
            file
        }: {
            jobId: string;
            companyId: string;
            file: File
        }) => {
            const formData = new FormData();
            formData.append('file', file);
            const { data } = await api.post<JobAttachment>(
                `/api/companies/${companyId}/jobs/${jobId}/attachments`,
                formData,
                { headers: { 'Content-Type': 'multipart/form-data' } }
            );
            return data;
        },
        onSuccess: (_, { jobId }) => {
            queryClient.invalidateQueries({ queryKey: ['job', jobId] });
        },
    });
};

export const useDeleteJobAttachment = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({
            jobId,
            attachmentId,
            companyId
        }: {
            jobId: string;
            attachmentId: string;
            companyId: string
        }) => {
            await api.delete(`/api/companies/${companyId}/jobs/${jobId}/attachments/${attachmentId}`);
        },
        onSuccess: (_, { jobId }) => {
            queryClient.invalidateQueries({ queryKey: ['job', jobId] });
        },
    });
};

// Quote Links
export const useLinkQuote = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({
            jobId,
            quoteId,
            companyId
        }: {
            jobId: string;
            quoteId: string;
            companyId: string
        }) => {
            await api.post(`/api/companies/${companyId}/jobs/${jobId}/quotes/${quoteId}`);
        },
        onSuccess: (_, { jobId }) => {
            queryClient.invalidateQueries({ queryKey: ['job', jobId] });
        },
    });
};

export const useUnlinkQuote = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({
            jobId,
            quoteId,
            companyId
        }: {
            jobId: string;
            quoteId: string;
            companyId: string
        }) => {
            await api.delete(`/api/companies/${companyId}/jobs/${jobId}/quotes/${quoteId}`);
        },
        onSuccess: (_, { jobId }) => {
            queryClient.invalidateQueries({ queryKey: ['job', jobId] });
        },
    });
};

// Invoice Links
export const useLinkInvoice = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({
            jobId,
            invoiceId,
            companyId
        }: {
            jobId: string;
            invoiceId: string;
            companyId: string
        }) => {
            await api.post(`/api/companies/${companyId}/jobs/${jobId}/invoices/${invoiceId}`);
        },
        onSuccess: (_, { jobId }) => {
            queryClient.invalidateQueries({ queryKey: ['job', jobId] });
        },
    });
};

export const useUnlinkInvoice = () => {
    const api = useApi();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({
            jobId,
            invoiceId,
            companyId
        }: {
            jobId: string;
            invoiceId: string;
            companyId: string
        }) => {
            await api.delete(`/api/companies/${companyId}/jobs/${jobId}/invoices/${invoiceId}`);
        },
        onSuccess: (_, { jobId }) => {
            queryClient.invalidateQueries({ queryKey: ['job', jobId] });
        },
    });
};
