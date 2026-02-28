import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { ExpenseSchema, type ExpenseFormValues } from '@/types/schemas';

export const useExpenseForm = () => {
    const form = useForm<ExpenseFormValues>({
        resolver: zodResolver(ExpenseSchema),
        defaultValues: {
            expenseDate: new Date().toISOString().split('T')[0],
            amount: 0,
            category: 'OTHER',
            vendor: '',
            expenseDescription: '',
            status: 'PENDING',
            taxDeductible: true,
            gstClaimable: true,
            paymentMethod: 'CARD',
            notes: '',
        },
    });

    return { form };
};
