import { useForm, useFieldArray } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { type QuoteFormValues, QuoteSchema } from '@/types/schemas';
import { useEffect } from 'react';

export const useQuoteForm = (defaultValues?: Partial<QuoteFormValues>) => {
    const form = useForm<QuoteFormValues>({
        resolver: zodResolver(QuoteSchema) as any,
        defaultValues: {
            quoteItems: [],
            discountType: 'DOLLAR',
            discountValue: 0,
            gst: 0,
            totalPrice: 0,
            status: 'PENDING',
            quoteRevision: 0,
            quoteDate: new Date().toISOString().split('T')[0],
            expiryDate: new Date(new Date().setDate(new Date().getDate() + 7)).toISOString().split('T')[0],
            clientFirstname: '',
            clientLastname: '',
            clientEntityName: '',
            clientEmail: '',
            clientPhone: '',
            clientId: '',
            notes: '',
            ...defaultValues
        },
        mode: 'onChange' // Auto-validate on change
    });

    const { fields, append, remove } = useFieldArray({
        control: form.control,
        name: "quoteItems",
    });

    // --- Calculation Logic ---
    const discountType = form.watch("discountType");
    const discountValue = form.watch("discountValue");
    const items = form.watch("quoteItems");

    useEffect(() => {
        // Debounce calculation or just run it? Since it's client side math, instant is fine.
        let subTotal = 0;
        let totalGst = 0;

        items.forEach(item => {
            // Safe parsing
            const qty = Number(item.quantity) || 0;
            const price = Number(item.price) || 0;
            const gstRate = Number(item.gst) === 10 ? 0.1 : 0;

            let lineTotal = qty * price;
            let lineGst = 0;

            if (gstRate > 0) {
                // Existing logic: "Your base price x 1.1 = GST inclusive price"
                // If price is base:
                // total = qty * price * 1.1; 
                // But wait, the old logic said: 
                // total = Number(item.quantity) * Number(item.price) * 1.1;
                // gstApplied += (total - Number(item.quantity) * Number(item.price));

                // So the price input IS EX-GST.
                const totalIncGst = lineTotal * 1.1;
                lineGst = totalIncGst - lineTotal;
                lineTotal = totalIncGst;
            }

            subTotal += lineTotal;
            totalGst += lineGst; // This might need adjustment if discount applies before GST? usually discount is pre-tax, but let's stick to simple first.

            // We don't necessarily need to update the item 'total' field in the form state 
            // unless we want to display it per row. Let's assume we do.
        });

        // Apply Discount
        // Old logic:
        // if (document.discountType === 'DOLLAR') total = total - document.discountValue;
        // else if (document.discountType === 'PERCENT') total = total - (total * document.discountValue / 100);

        // Note: Old logic applied discount AFTER GST addition to the total. This means discount is inclusive of GST.

        let finalTotal = subTotal;
        const discountVal = Number(discountValue) || 0;

        if (discountType === 'DOLLAR') {
            finalTotal = finalTotal - discountVal;
        } else {
            finalTotal = finalTotal - (finalTotal * discountVal / 100);
        }

        // Avoid infinite loops by checking if values actually changed
        // We only setValue if the calculated values differ from current form state
        const currentTotalPrice = form.getValues('totalPrice');
        const currentGst = form.getValues('gst');

        if (Math.abs(finalTotal - currentTotalPrice) > 0.01) {
            form.setValue('totalPrice', Number(finalTotal.toFixed(2)));
        }

        if (Math.abs(totalGst - currentGst) > 0.01) {
            form.setValue('gst', Number(totalGst.toFixed(2)));
        }

        // Updating items creates a re-render loop if we are watching items.
        // Solution: Calculate per-row totals in the render (UI) instead of storing them back to the form state 
        // OR only update if strictly different.
        // For now, I will NOT update items back to form state to avoid loop.
        // I will return the calculated values to be displayed.

    }, [items, discountType, discountValue, form]);

    return { form, fields, append, remove };
}
