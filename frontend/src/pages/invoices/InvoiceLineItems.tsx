import type { UseFormReturn } from 'react-hook-form';
import { useFieldArray } from 'react-hook-form';
import type { InvoiceFormValues } from '@/types/schemas';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table';
import {
    FormControl,
    FormField,
    FormItem,
    FormMessage,
} from '@/components/ui/form';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Trash2, Plus } from 'lucide-react';
import { ItemSearch, type SelectedItem } from '@/components/line-items/ItemSearch';

interface InvoiceLineItemsProps {
    form: UseFormReturn<InvoiceFormValues>;
    disabled?: boolean;
}

export default function InvoiceLineItems({ form, disabled }: InvoiceLineItemsProps) {
    const { fields, append, remove } = useFieldArray({
        control: form.control,
        name: "invoiceItems",
    });

    const items = form.watch("invoiceItems");
    const formatCurrency = (val: number) => `$${val.toFixed(2)}`;

    return (
        <div className="space-y-4">
            <div className="flex justify-between items-center">
                <h3 className="text-lg font-medium">Items</h3>
                <Button
                    type="button"
                    variant="secondary"
                    size="sm"
                    disabled={disabled}
                    onClick={() => append({
                        itemOrder: fields.length + 1,
                        itemDescription: '',
                        quantity: 1,
                        price: 0,
                        total: 0,
                        gst: 0
                    })}
                >
                    <Plus className="mr-2 h-4 w-4" />
                    Add Item
                </Button>
            </div>

            <div className="border rounded-md overflow-visible">
                <Table className="overflow-visible">
                    <TableHeader>
                        <TableRow>
                            <TableHead className="w-[50px]">#</TableHead>
                            <TableHead className="w-[40%]">Description</TableHead>
                            <TableHead>Qty</TableHead>
                            <TableHead>Price (Ex GST)</TableHead>
                            <TableHead>GST</TableHead>
                            <TableHead className="text-right">Total (Inc GST)</TableHead>
                            <TableHead className="w-[50px]"></TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {fields.map((field, index) => {
                            const qty = Number(items[index]?.quantity) || 0;
                            const price = Number(items[index]?.price) || 0;
                            const gstRate = Number(items[index]?.gst) === 10 ? 0.1 : 0;

                            let total = qty * price;
                            if (gstRate > 0) total = total * 1.1;

                            return (
                                <TableRow key={field.id}>
                                    <TableCell className="font-medium">{index + 1}</TableCell>
                                    <TableCell>
                                        <ItemSearch
                                            value={items[index]?.itemDescription || ''}
                                            productItemId={items[index]?.productItemId}
                                            serviceItemId={items[index]?.serviceItemId}
                                            disabled={disabled}
                                            onSelect={(item: SelectedItem) => {
                                                const opts = { shouldValidate: true, shouldDirty: true };
                                                form.setValue(`invoiceItems.${index}.itemDescription`, item.name, opts);
                                                form.setValue(`invoiceItems.${index}.price`, item.price, opts);
                                                if (item.type === 'product') {
                                                    form.setValue(`invoiceItems.${index}.productItemId`, item.id || null, opts);
                                                    form.setValue(`invoiceItems.${index}.serviceItemId`, null, opts);
                                                } else if (item.type === 'service') {
                                                    form.setValue(`invoiceItems.${index}.serviceItemId`, item.id || null, opts);
                                                    form.setValue(`invoiceItems.${index}.productItemId`, null, opts);
                                                }
                                            }}
                                            onCustomText={(text: string) => {
                                                const opts = { shouldValidate: true, shouldDirty: true };
                                                form.setValue(`invoiceItems.${index}.itemDescription`, text, opts);
                                                form.setValue(`invoiceItems.${index}.productItemId`, null, opts);
                                                form.setValue(`invoiceItems.${index}.serviceItemId`, null, opts);
                                            }}
                                        />
                                        <FormField
                                            control={form.control}
                                            name={`invoiceItems.${index}.itemDescription`}
                                            render={() => (
                                                <FormItem>
                                                    <FormMessage />
                                                </FormItem>
                                            )}
                                        />
                                    </TableCell>
                                    <TableCell>
                                        <FormField
                                            control={form.control}
                                            name={`invoiceItems.${index}.quantity`}
                                            render={({ field }) => (
                                                <FormItem>
                                                    <FormControl>
                                                        <Input type="number" {...field} value={field.value ?? ''} min={0} disabled={disabled} />
                                                    </FormControl>
                                                    <FormMessage />
                                                </FormItem>
                                            )}
                                        />
                                    </TableCell>
                                    <TableCell>
                                        <FormField
                                            control={form.control}
                                            name={`invoiceItems.${index}.price`}
                                            render={({ field }) => (
                                                <FormItem>
                                                    <FormControl>
                                                        <Input type="number" {...field} value={field.value ?? ''} min={0} step="0.01" disabled={disabled} />
                                                    </FormControl>
                                                    <FormMessage />
                                                </FormItem>
                                            )}
                                        />
                                    </TableCell>
                                    <TableCell>
                                        <FormField
                                            control={form.control}
                                            name={`invoiceItems.${index}.gst`}
                                            render={({ field }) => (
                                                <FormItem>
                                                    <Select onValueChange={(val) => field.onChange(Number(val))} value={String(field.value)} disabled={disabled}>
                                                        <FormControl>
                                                            <SelectTrigger>
                                                                <SelectValue placeholder="GST" />
                                                            </SelectTrigger>
                                                        </FormControl>
                                                        <SelectContent>
                                                            <SelectItem value="0">0%</SelectItem>
                                                            <SelectItem value="10">10%</SelectItem>
                                                        </SelectContent>
                                                    </Select>
                                                </FormItem>
                                            )}
                                        />
                                    </TableCell>
                                    <TableCell className="text-right align-middle">
                                        {formatCurrency(total)}
                                    </TableCell>
                                    <TableCell>
                                        <Button
                                            type="button"
                                            variant="ghost"
                                            size="sm"
                                            onClick={() => remove(index)}
                                            disabled={disabled}
                                            className="text-destructive hover:bg-destructive/10"
                                        >
                                            <Trash2 className="h-4 w-4" />
                                        </Button>
                                    </TableCell>
                                </TableRow>
                            );
                        })}
                        {fields.length === 0 && (
                            <TableRow>
                                <TableCell colSpan={7} className="h-24 text-center text-muted-foreground">
                                    No items. Click "Add Item" to start.
                                </TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </div>
        </div>
    );
}
