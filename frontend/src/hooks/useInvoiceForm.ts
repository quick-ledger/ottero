import { useForm, useFieldArray } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { type InvoiceFormValues, InvoiceSchema } from '@/types/schemas';
import { useEffect } from 'react';

export const useInvoiceForm = (defaultValues?: Partial<InvoiceFormValues>) => {
    const form = useForm<InvoiceFormValues>({
        resolver: zodResolver(InvoiceSchema) as any,
        defaultValues: {
            invoiceItems: [],
            discountType: 'DOLLAR',
            discountValue: 0,
            gst: 0,
            totalPrice: 0,
            status: 'DRAFT',
            issueDate: new Date().toISOString().split('T')[0],
            dueDate: new Date(new Date().setDate(new Date().getDate() + 14)).toISOString().split('T')[0],
            isRecurring: false,
            recurringFrequency: 'MONTHLY',
            recurringAutoSend: false,
            ...defaultValues
        },
        mode: 'onChange'
    });

    const { fields, append, remove } = useFieldArray({
        control: form.control,
        name: "invoiceItems",
    });

    const discountType = form.watch("discountType");
    const discountValue = form.watch("discountValue");
    const items = form.watch("invoiceItems");

    useEffect(() => {
        let subTotal = 0;
        let totalGst = 0;

        items.forEach(item => {
            const qty = Number(item.quantity) || 0;
            const price = Number(item.price) || 0;
            const gstRate = Number(item.gst) === 10 ? 0.1 : 0;

            let lineTotal = qty * price;
            let lineGst = 0;

            if (gstRate > 0) {
                const totalIncGst = lineTotal * 1.1;
                lineGst = totalIncGst - lineTotal;
                lineTotal = totalIncGst;
            }

            subTotal += lineTotal;
            totalGst += lineGst;
        });

        // Apply Discount
        let finalTotal = subTotal;
        const discountVal = Number(discountValue) || 0;

        if (discountType === 'DOLLAR') {
            finalTotal = finalTotal - discountVal;
        } else {
            finalTotal = finalTotal - (finalTotal * discountVal / 100);
        }

        // Prevent negative total
        if (finalTotal < 0) finalTotal = 0;

        // Update form values
        form.setValue('totalPrice', Number(finalTotal.toFixed(2)));
        form.setValue('gst', Number(totalGst.toFixed(2)));

    }, [JSON.stringify(items), discountType, discountValue, form]);

    return { form, fields, append, remove };
}
