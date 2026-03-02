import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { JobSchema, type JobFormValues } from '@/types/schemas';

export const useJobForm = () => {
    const form = useForm<JobFormValues>({
        resolver: zodResolver(JobSchema),
        defaultValues: {
            title: '',
            jobDescription: '',
            location: '',
            status: 'SCHEDULED',
            scheduledDate: new Date().toISOString().split('T')[0],
        },
    });

    return { form };
};
