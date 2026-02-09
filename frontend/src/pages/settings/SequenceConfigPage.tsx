import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { useSequenceConfig, useUpdateSequenceConfig, type SequenceConfig } from '@/hooks/useSequenceConfigs';
import { useSelectedCompanyId } from '@/store/useAppStore';
import { Button } from '@/components/ui/button';
import {
    Form,
    FormControl,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Separator } from '@/components/ui/separator';
import { toast } from 'sonner';

function SequenceForm({ type, companyId }: { type: 'QUOTE' | 'INVOICE', companyId: string }) {
    const { data: config, isLoading } = useSequenceConfig(companyId, type);
    const updateConfig = useUpdateSequenceConfig();

    const form = useForm<SequenceConfig>({
        defaultValues: {
            prefix: '',
            postfix: '',
            currentNumber: 1000,
            numberPadding: 4,
            type: type,
        },
    });

    useEffect(() => {
        if (config) {
            form.reset(config);
        }
    }, [config, form]);

    const onSubmit = async (values: SequenceConfig) => {
        try {
            await updateConfig.mutateAsync({ companyId, config: { ...values, type } });
            toast.success(`${type} sequence updated`);
        } catch (error) {
            console.error(error);
            toast.error(`Failed to update ${type} sequence`);
        }
    };

    // Preset patterns for quick setup
    const presets = [
        { name: 'Simple Sequential', prefix: type === 'INVOICE' ? 'INV-' : 'Q-', postfix: '', padding: 4 },
        { name: 'With Year', prefix: type === 'INVOICE' ? 'INV-{YYYY}-' : 'Q-{YYYY}-', postfix: '', padding: 4 },
        { name: 'Year/Month', prefix: '{YYYY}{MM}-', postfix: '', padding: 3 },
        { name: 'Compact', prefix: '{YY}', postfix: '', padding: 5 },
    ];

    const applyPreset = (preset: typeof presets[0]) => {
        form.setValue('prefix', preset.prefix);
        form.setValue('postfix', preset.postfix);
        form.setValue('numberPadding', preset.padding);
    };

    const prefix = form.watch('prefix') || '';
    const postfix = form.watch('postfix') || '';
    const currentNumber = form.watch('currentNumber') || 0;
    const numberPadding = form.watch('numberPadding') || 0;

    // Generate real-time example with date placeholders
    const generateExample = () => {
        const now = new Date();
        const year = now.getFullYear().toString();
        const shortYear = year.substring(2);
        const month = (now.getMonth() + 1).toString().padStart(2, '0');
        const day = now.getDate().toString().padStart(2, '0');

        let examplePrefix = prefix;
        examplePrefix = examplePrefix.replace(/{YYYY}/g, year);
        examplePrefix = examplePrefix.replace(/{YY}/g, shortYear);
        examplePrefix = examplePrefix.replace(/{MM}/g, month);
        examplePrefix = examplePrefix.replace(/{DD}/g, day);

        let examplePostfix = postfix;
        examplePostfix = examplePostfix.replace(/{YYYY}/g, year);
        examplePostfix = examplePostfix.replace(/{YY}/g, shortYear);
        examplePostfix = examplePostfix.replace(/{MM}/g, month);
        examplePostfix = examplePostfix.replace(/{DD}/g, day);

        const paddedNumber = numberPadding > 0
            ? currentNumber.toString().padStart(numberPadding, '0')
            : currentNumber.toString();

        return `${examplePrefix}${paddedNumber}${examplePostfix}`;
    };

    const example = generateExample();

    if (isLoading) return <div>Loading...</div>;

    return (
        <Card>
            <CardHeader>
                <CardTitle>{type === 'QUOTE' ? 'Quote' : 'Invoice'} Numbering</CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
                {/* Preset Patterns */}
                <div className="space-y-2">
                    <label className="text-sm font-medium">Quick Presets (optional)</label>
                    <div className="flex flex-wrap gap-2">
                        {presets.map((preset) => (
                            <Button
                                key={preset.name}
                                type="button"
                                variant="outline"
                                size="sm"
                                onClick={() => applyPreset(preset)}
                            >
                                {preset.name}
                            </Button>
                        ))}
                    </div>
                </div>

                <Separator />

                <Form {...form}>
                    <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
                        {/* Main Configuration */}
                        <div className="space-y-4">
                            <h3 className="text-sm font-medium">Custom Configuration</h3>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <FormField
                                    control={form.control}
                                    name="prefix"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Prefix (before the number)</FormLabel>
                                            <FormControl>
                                                <Input
                                                    placeholder="e.g., INV- or INV-{YYYY}-"
                                                    {...field}
                                                />
                                            </FormControl>
                                            <p className="text-xs text-muted-foreground">
                                                Optional. Can include {'{YYYY}'}, {'{YY}'}, {'{MM}'}, {'{DD}'}
                                            </p>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name="postfix"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Postfix (after the number)</FormLabel>
                                            <FormControl>
                                                <Input
                                                    placeholder="e.g., -AU or leave empty"
                                                    {...field}
                                                />
                                            </FormControl>
                                            <p className="text-xs text-muted-foreground">
                                                Optional. Can also include date placeholders
                                            </p>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <FormField
                                    control={form.control}
                                    name="currentNumber"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Next Number</FormLabel>
                                            <FormControl>
                                                <Input
                                                    type="number"
                                                    {...field}
                                                    onChange={e => field.onChange(parseInt(e.target.value))}
                                                />
                                            </FormControl>
                                            <p className="text-xs text-muted-foreground">
                                                The next {type.toLowerCase()} will use this number
                                            </p>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name="numberPadding"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Zero Padding</FormLabel>
                                            <FormControl>
                                                <Input
                                                    type="number"
                                                    min={0}
                                                    max={10}
                                                    {...field}
                                                    onChange={e => field.onChange(parseInt(e.target.value))}
                                                />
                                            </FormControl>
                                            <p className="text-xs text-muted-foreground">
                                                4 = 0001, 3 = 001, 0 = no padding
                                            </p>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                            </div>
                        </div>

                        {/* Live Preview */}
                        <div className="p-4 bg-muted rounded-lg border-2 border-dashed space-y-3">
                            <div className="flex items-center justify-between">
                                <span className="text-sm font-medium">Live Preview:</span>
                                <span className="font-mono font-bold text-foreground text-xl">{example}</span>
                            </div>
                            <div className="text-xs text-muted-foreground space-y-1">
                                <div>✓ Date placeholders are <strong>optional</strong> - only use if you want dates in your numbers</div>
                                <div>✓ Leave prefix/postfix empty for just numbers (e.g., 0001, 0002)</div>
                                <div>✓ Set padding to 0 if you don't want leading zeros</div>
                            </div>
                        </div>

                        {/* Available Placeholders Reference */}
                        <div className="p-3 bg-blue-50 dark:bg-blue-950 rounded-lg text-xs space-y-2">
                            <div className="font-medium text-blue-900 dark:text-blue-100">Available Date Placeholders:</div>
                            <div className="grid grid-cols-2 gap-2 text-blue-800 dark:text-blue-200">
                                <div><code className="bg-blue-100 dark:bg-blue-900 px-1.5 py-0.5 rounded">{'{YYYY}'}</code> = {new Date().getFullYear()} (full year)</div>
                                <div><code className="bg-blue-100 dark:bg-blue-900 px-1.5 py-0.5 rounded">{'{YY}'}</code> = {new Date().getFullYear().toString().substring(2)} (short year)</div>
                                <div><code className="bg-blue-100 dark:bg-blue-900 px-1.5 py-0.5 rounded">{'{MM}'}</code> = {(new Date().getMonth() + 1).toString().padStart(2, '0')} (month)</div>
                                <div><code className="bg-blue-100 dark:bg-blue-900 px-1.5 py-0.5 rounded">{'{DD}'}</code> = {new Date().getDate().toString().padStart(2, '0')} (day)</div>
                            </div>
                        </div>

                        <div className="flex justify-end">
                            <Button type="submit" disabled={updateConfig.isPending}>
                                Save {type === 'QUOTE' ? 'Quote' : 'Invoice'} Config
                            </Button>
                        </div>
                    </form>
                </Form>
            </CardContent>
        </Card>
    );
}

export default function SequenceConfigPage() {
    const companyId = useSelectedCompanyId();

    if (!companyId) return <div className="p-8">Please select a company.</div>;

    return (
        <div className="container mx-auto py-10 space-y-8">
            <h1 className="text-3xl font-bold">Sequence Configuration</h1>
            <SequenceForm type="QUOTE" companyId={companyId} />
            <SequenceForm type="INVOICE" companyId={companyId} />
        </div>
    );
}
