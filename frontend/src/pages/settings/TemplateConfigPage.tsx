import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button } from '@/components/ui/button';
import {
    Form,
    FormControl,
    FormDescription,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Separator } from '@/components/ui/separator';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { toast } from 'sonner';
import { useAppStore } from '@/store/useAppStore';
import { useApi } from '@/hooks/useApi';

const TemplateConfigSchema = z.object({
    logoWidth: z.string().optional(),
    logoHeight: z.string().optional(),
    logoPosition: z.enum(['left', 'center', 'right']).optional(),
    notes: z.string().optional(),
});

type TemplateConfigFormValues = z.infer<typeof TemplateConfigSchema>;

const DEFAULT_NOTES_EXAMPLE = `Thank you for your business!

DEPOSIT TERMS:
A 50% deposit is required to commence work. The remaining balance is due upon completion.

PAYMENT TERMS:
Payment is due within 30 days of invoice date. Late payments may incur additional fees.
We accept bank transfer, credit card, or check.`;

export default function TemplateConfigPage() {
    const navigate = useNavigate();
    const { selectedCompanyId } = useAppStore();
    const api = useApi();
    const [isLoading, setIsLoading] = useState(false);
    const [isSaving, setIsSaving] = useState(false);

    const form = useForm<TemplateConfigFormValues>({
        resolver: zodResolver(TemplateConfigSchema),
        defaultValues: {
            logoWidth: '150',
            logoHeight: '80',
            logoPosition: 'left',
            notes: '',
        },
    });

    // Load existing configuration
    useEffect(() => {
        const loadConfig = async () => {
            if (!selectedCompanyId) return;

            try {
                setIsLoading(true);
                const { data } = await api.get(`/api/companies/${selectedCompanyId}/template-config`);
                if (data) {
                    form.reset(data);
                }
            } catch (error: any) {
                // If 404, it means no config exists yet - that's okay
                if (error.response?.status !== 404) {
                    console.error('Failed to load template config:', error);
                    toast.error('Failed to load template configuration');
                }
            } finally {
                setIsLoading(false);
            }
        };

        loadConfig();
    }, [selectedCompanyId]);

    const onSubmit = async (values: TemplateConfigFormValues) => {
        if (!selectedCompanyId) {
            toast.error('No company selected');
            return;
        }

        try {
            setIsSaving(true);
            await api.post(`/api/companies/${selectedCompanyId}/template-config`, values);
            toast.success('Template configuration saved successfully');
        } catch (error) {
            console.error('Failed to save template config:', error);
            toast.error('Failed to save template configuration');
        } finally {
            setIsSaving(false);
        }
    };

    if (!selectedCompanyId) {
        return (
            <div className="container max-w-3xl py-10">
                <Card>
                    <CardContent className="pt-10 text-center">
                        <p className="text-muted-foreground">Please select a company first</p>
                        <Button onClick={() => navigate('/companies')} className="mt-4">
                            Go to Companies
                        </Button>
                    </CardContent>
                </Card>
            </div>
        );
    }

    if (isLoading) {
        return (
            <div className="container max-w-3xl py-10">
                <Card>
                    <CardContent className="pt-10 text-center">
                        <p className="text-muted-foreground">Loading configuration...</p>
                    </CardContent>
                </Card>
            </div>
        );
    }

    return (
        <div className="container max-w-3xl py-10">
            <Card>
                <CardHeader>
                    <CardTitle>Quote & Invoice Template Configuration</CardTitle>
                    <CardDescription>
                        Configure how your quotes and invoices are displayed
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <Form {...form}>
                        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
                            {/* Logo Settings */}
                            <div>
                                <h3 className="text-lg font-medium mb-4">Logo Settings</h3>
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                    <FormField
                                        control={form.control}
                                        name="logoWidth"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormLabel>Logo Max Width (px)</FormLabel>
                                                <FormControl>
                                                    <Input
                                                        type="number"
                                                        placeholder="150"
                                                        {...field}
                                                    />
                                                </FormControl>
                                                <FormDescription>
                                                    Maximum width of logo in PDFs (default: 150px)
                                                </FormDescription>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />
                                    <FormField
                                        control={form.control}
                                        name="logoHeight"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormLabel>Logo Max Height (px)</FormLabel>
                                                <FormControl>
                                                    <Input
                                                        type="number"
                                                        placeholder="80"
                                                        {...field}
                                                    />
                                                </FormControl>
                                                <FormDescription>
                                                    Maximum height of logo in PDFs (default: 80px)
                                                </FormDescription>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />
                                </div>

                                <FormField
                                    control={form.control}
                                    name="logoPosition"
                                    render={({ field }) => (
                                        <FormItem className="mt-6">
                                            <FormLabel>Logo Position</FormLabel>
                                            <FormControl>
                                                <RadioGroup
                                                    onValueChange={field.onChange}
                                                    defaultValue={field.value}
                                                    className="flex gap-4"
                                                >
                                                    <div className="flex items-center space-x-2">
                                                        <RadioGroupItem value="left" id="left" />
                                                        <label htmlFor="left" className="cursor-pointer">Left</label>
                                                    </div>
                                                    <div className="flex items-center space-x-2">
                                                        <RadioGroupItem value="center" id="center" />
                                                        <label htmlFor="center" className="cursor-pointer">Center</label>
                                                    </div>
                                                    <div className="flex items-center space-x-2">
                                                        <RadioGroupItem value="right" id="right" />
                                                        <label htmlFor="right" className="cursor-pointer">Right</label>
                                                    </div>
                                                </RadioGroup>
                                            </FormControl>
                                            <FormDescription>
                                                Logo alignment in PDF header (default: left)
                                            </FormDescription>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                            </div>

                            <Separator />

                            {/* Footer & Notes */}
                            <div>
                                <h3 className="text-lg font-medium mb-4">Footer & Notes</h3>
                                <FormField
                                    control={form.control}
                                    name="notes"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Template Notes</FormLabel>
                                            <FormControl>
                                                <Textarea
                                                    placeholder={DEFAULT_NOTES_EXAMPLE}
                                                    className="min-h-[200px] font-mono text-sm"
                                                    {...field}
                                                />
                                            </FormControl>
                                            <FormDescription>
                                                This text will appear at the bottom of your quotes and invoices. You can include payment terms, deposit requirements, or any other information.
                                            </FormDescription>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                            </div>

                            <div className="flex justify-end gap-4 pt-4">
                                <Button
                                    type="button"
                                    variant="outline"
                                    onClick={() => navigate(-1)}
                                >
                                    Cancel
                                </Button>
                                <Button
                                    type="submit"
                                    disabled={isSaving}
                                >
                                    {isSaving ? 'Saving...' : 'Save Configuration'}
                                </Button>
                            </div>
                        </form>
                    </Form>
                </CardContent>
            </Card>
        </div>
    );
}
