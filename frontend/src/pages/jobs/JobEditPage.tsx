import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { useAppStore } from '@/store/useAppStore';
import { useJob, useDeleteJob, useAddJobNote, useDeleteJobNote, useDeleteJobAttachment, useLinkQuote, useUnlinkQuote, useLinkInvoice, useUnlinkInvoice, useAddJobTimeEntry, useUpdateJobTimeEntry, useDeleteJobTimeEntry } from '@/hooks/useJobs';
import { useJobForm } from '@/hooks/useJobForm';
import { useApi } from '@/hooks/useApi';
import { toast } from 'sonner';
import { Loader2, ArrowLeft, Save, Trash, Paperclip, Download, X, Lock, Sparkles, Plus, FileText, Calendar, MapPin, User, Link as LinkIcon, Clock, Pencil, DollarSign } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import {
    Form,
    FormControl,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from '@/components/ui/form';
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { FileUpload } from '@/components/FileUpload';
import { CustomerSearch } from '@/components/quote/CustomerSearch';
import { QuoteSearch } from '@/components/job/QuoteSearch';
import { InvoiceSearch } from '@/components/job/InvoiceSearch';
import { Badge } from '@/components/ui/badge';
import { Switch } from '@/components/ui/switch';
import { Label } from '@/components/ui/label';
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from '@/components/ui/alert-dialog';

import { JOB_STATUS_LABELS, type JobStatus } from '@/types';

interface UserProfile {
    subscriptionPlan: string;
}

export default function JobEditPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { user } = useAuth0();
    const { selectedCompanyId } = useAppStore();
    const api = useApi();
    const isNew = id === 'new' || !id;

    const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
    const [isSaving, setIsSaving] = useState(false);
    const [profile, setProfile] = useState<UserProfile | null>(null);
    const [profileLoading, setProfileLoading] = useState(true);
    const [newNoteText, setNewNoteText] = useState('');
    const [isAddingNote, setIsAddingNote] = useState(false);
    const [selectedCustomerName, setSelectedCustomerName] = useState<string | null>(null);

    // Time entry state
    const [timeEntryDate, setTimeEntryDate] = useState(new Date().toISOString().split('T')[0]);
    const [timeEntryHours, setTimeEntryHours] = useState('');
    const [timeEntryMinutes, setTimeEntryMinutes] = useState('');
    const [timeEntryDescription, setTimeEntryDescription] = useState('');
    const [timeEntryBillable, setTimeEntryBillable] = useState(true);
    const [timeEntryHourlyRate, setTimeEntryHourlyRate] = useState('');
    const [timeEntryEmployeeName, setTimeEntryEmployeeName] = useState('');
    const [isAddingTimeEntry, setIsAddingTimeEntry] = useState(false);
    const [editingTimeEntryId, setEditingTimeEntryId] = useState<string | null>(null);

    const { data: job, isLoading, refetch } = useJob(id || '', selectedCompanyId);
    const { form } = useJobForm();
    const deleteJob = useDeleteJob();
    const addNote = useAddJobNote();
    const deleteNote = useDeleteJobNote();
    const deleteAttachment = useDeleteJobAttachment();
    const linkQuote = useLinkQuote();
    const unlinkQuote = useUnlinkQuote();
    const linkInvoice = useLinkInvoice();
    const unlinkInvoice = useUnlinkInvoice();
    const addTimeEntry = useAddJobTimeEntry();
    const updateTimeEntry = useUpdateJobTimeEntry();
    const deleteTimeEntry = useDeleteJobTimeEntry();

    const isAdvancedPlan = profile?.subscriptionPlan?.toLowerCase() === 'advanced';

    // Fetch user profile
    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const response = await api.get('/api/users/profile', {
                    headers: { 'X-User-Id': user?.sub }
                });
                setProfile(response.data);
            } catch (error) {
                console.error('Failed to fetch profile', error);
            } finally {
                setProfileLoading(false);
            }
        };
        if (user?.sub) fetchProfile();
    }, [user?.sub]);

    // Populate form when job loads
    useEffect(() => {
        if (job) {
            form.reset({
                ...job,
                clientId: job.clientId || undefined,
                scheduledDate: job.scheduledDate?.substring(0, 10),
                completionDate: job.completionDate?.substring(0, 10),
            });
            if (job.clientName) {
                setSelectedCustomerName(job.clientName);
            }
        }
    }, [job, form]);


    const onSubmit = async (data: Record<string, unknown>) => {
        if (!selectedCompanyId) return;

        try {
            setIsSaving(true);
            const payload = { ...data, companyId: selectedCompanyId };

            if (isNew) {
                const response = await api.post(`/api/companies/${selectedCompanyId}/jobs`, payload);
                toast.success('Job created successfully');
                navigate(`/jobs/${response.data.id}`);
            } else {
                await api.put(`/api/companies/${selectedCompanyId}/jobs/${id}`, payload);
                toast.success('Job updated successfully');
                refetch();
            }
        } catch (error: unknown) {
            console.error(error);
            const errorMessage = (error as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Failed to save job';
            toast.error(errorMessage);
        } finally {
            setIsSaving(false);
        }
    };

    const handleDelete = async () => {
        if (!selectedCompanyId || !id) return;

        try {
            await deleteJob.mutateAsync({ id, companyId: selectedCompanyId });
            toast.success('Job deleted');
            navigate('/jobs');
        } catch {
            toast.error('Failed to delete job');
        }
    };

    const handleAddNote = async () => {
        if (!selectedCompanyId || !id || !newNoteText.trim()) return;

        setIsAddingNote(true);
        try {
            await addNote.mutateAsync({
                jobId: id,
                companyId: selectedCompanyId,
                note: { noteText: newNoteText.trim(), noteDate: new Date().toISOString() }
            });
            toast.success('Note added');
            setNewNoteText('');
            refetch();
        } catch {
            toast.error('Failed to add note');
        } finally {
            setIsAddingNote(false);
        }
    };

    const handleDeleteNote = async (noteId: string) => {
        if (!selectedCompanyId || !id) return;

        try {
            await deleteNote.mutateAsync({ jobId: id, noteId, companyId: selectedCompanyId });
            toast.success('Note deleted');
            refetch();
        } catch {
            toast.error('Failed to delete note');
        }
    };

    const handleDeleteAttachment = async (attachmentId: string) => {
        if (!selectedCompanyId || !id) return;

        try {
            await deleteAttachment.mutateAsync({ jobId: id, attachmentId, companyId: selectedCompanyId });
            toast.success('Attachment deleted');
            refetch();
        } catch {
            toast.error('Failed to delete attachment');
        }
    };

    const resetTimeEntryForm = () => {
        setTimeEntryDate(new Date().toISOString().split('T')[0]);
        setTimeEntryHours('');
        setTimeEntryMinutes('');
        setTimeEntryDescription('');
        setTimeEntryBillable(true);
        setTimeEntryHourlyRate('');
        setTimeEntryEmployeeName('');
        setEditingTimeEntryId(null);
    };

    const handleAddOrUpdateTimeEntry = async () => {
        if (!selectedCompanyId || !id) return;

        const hours = parseInt(timeEntryHours) || 0;
        const minutes = parseInt(timeEntryMinutes) || 0;
        const durationMinutes = hours * 60 + minutes;

        if (durationMinutes < 1) {
            toast.error('Duration must be at least 1 minute');
            return;
        }

        setIsAddingTimeEntry(true);
        try {
            const entry = {
                entryDate: timeEntryDate,
                durationMinutes,
                description: timeEntryDescription || undefined,
                billable: timeEntryBillable,
                hourlyRate: timeEntryHourlyRate ? parseFloat(timeEntryHourlyRate) : undefined,
                employeeName: timeEntryEmployeeName || undefined,
            };

            if (editingTimeEntryId) {
                await updateTimeEntry.mutateAsync({
                    jobId: id,
                    entryId: editingTimeEntryId,
                    companyId: selectedCompanyId,
                    entry,
                });
                toast.success('Time entry updated');
            } else {
                await addTimeEntry.mutateAsync({
                    jobId: id,
                    companyId: selectedCompanyId,
                    entry,
                });
                toast.success('Time entry added');
            }
            resetTimeEntryForm();
            refetch();
        } catch {
            toast.error('Failed to save time entry');
        } finally {
            setIsAddingTimeEntry(false);
        }
    };

    const handleEditTimeEntry = (entry: { id: string; entryDate: string; durationMinutes: number; description?: string; billable: boolean; hourlyRate?: number; employeeName?: string }) => {
        setEditingTimeEntryId(entry.id);
        setTimeEntryDate(entry.entryDate);
        const hours = Math.floor(entry.durationMinutes / 60);
        const minutes = entry.durationMinutes % 60;
        setTimeEntryHours(hours > 0 ? String(hours) : '');
        setTimeEntryMinutes(minutes > 0 ? String(minutes) : '');
        setTimeEntryDescription(entry.description || '');
        setTimeEntryBillable(entry.billable);
        setTimeEntryHourlyRate(entry.hourlyRate ? String(entry.hourlyRate) : '');
        setTimeEntryEmployeeName(entry.employeeName || '');
    };

    const handleDeleteTimeEntry = async (entryId: string) => {
        if (!selectedCompanyId || !id) return;

        try {
            await deleteTimeEntry.mutateAsync({ jobId: id, entryId, companyId: selectedCompanyId });
            toast.success('Time entry deleted');
            refetch();
        } catch {
            toast.error('Failed to delete time entry');
        }
    };

    const formatDuration = (minutes: number) => {
        const h = Math.floor(minutes / 60);
        const m = minutes % 60;
        if (h > 0 && m > 0) return `${h}h ${m}m`;
        if (h > 0) return `${h}h`;
        return `${m}m`;
    };

    const calculateTotalTime = () => {
        if (!job?.timeEntries) return { totalMinutes: 0, totalBillable: 0 };
        let totalMinutes = 0;
        let totalBillable = 0;
        for (const entry of job.timeEntries) {
            totalMinutes += entry.durationMinutes;
            if (entry.billable && entry.hourlyRate) {
                totalBillable += (entry.durationMinutes / 60) * entry.hourlyRate;
            }
        }
        return { totalMinutes, totalBillable };
    };

    if (!selectedCompanyId) {
        return (
            <div className="container mx-auto py-10 text-center">
                <p>Please select a company first.</p>
            </div>
        );
    }

    if (profileLoading) {
        return (
            <div className="container mx-auto py-10 flex justify-center">
                <Loader2 className="h-8 w-8 animate-spin" />
            </div>
        );
    }

    // Require Advanced plan
    if (!isAdvancedPlan) {
        return (
            <div className="container mx-auto py-10 max-w-2xl">
                <Card className="border-amber-200 bg-amber-50 dark:bg-amber-950/20 dark:border-amber-800">
                    <CardHeader className="text-center">
                        <div className="mx-auto mb-4 h-12 w-12 rounded-full bg-amber-100 dark:bg-amber-900 flex items-center justify-center">
                            <Lock className="h-6 w-6 text-amber-600" />
                        </div>
                        <CardTitle className="text-2xl text-amber-900 dark:text-amber-100">
                            Advanced Plan Required
                        </CardTitle>
                    </CardHeader>
                    <CardContent className="text-center space-y-4">
                        <p className="text-amber-700 dark:text-amber-300">
                            Job Management is available exclusively on the Advanced plan.
                            Upgrade to track jobs, add daily notes, and link to quotes and invoices.
                        </p>
                        <div className="flex justify-center gap-4 pt-4">
                            <Button variant="outline" onClick={() => navigate('/jobs')}>
                                <ArrowLeft className="mr-2 h-4 w-4" />
                                Back to Jobs
                            </Button>
                            <Button onClick={() => navigate('/settings/pricing')} className="bg-amber-600 hover:bg-amber-700">
                                <Sparkles className="mr-2 h-4 w-4" />
                                View Plans & Pricing
                            </Button>
                        </div>
                    </CardContent>
                </Card>
            </div>
        );
    }

    if (isLoading && !isNew) {
        return (
            <div className="container mx-auto py-10 flex justify-center">
                <Loader2 className="h-8 w-8 animate-spin" />
            </div>
        );
    }

    return (
        <div className="container mx-auto py-6 max-w-4xl space-y-8">
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                    <Button variant="ghost" size="icon" onClick={() => navigate('/jobs')}>
                        <ArrowLeft className="h-4 w-4" />
                    </Button>
                    <div>
                        <h1 className="text-3xl font-bold">{isNew ? 'New Job' : `Edit Job ${job?.jobNumber || ''}`}</h1>
                        {job?.status && (
                            <Badge variant="outline" className="mt-1">
                                {JOB_STATUS_LABELS[job.status as JobStatus]}
                            </Badge>
                        )}
                    </div>
                </div>
                <Button type="button" onClick={form.handleSubmit(onSubmit)} disabled={isSaving}>
                    {isSaving ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <Save className="mr-2 h-4 w-4" />}
                    Save
                </Button>
            </div>

            <Form {...form}>
                <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-8">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        {/* Job Details */}
                        <Card>
                            <CardHeader>
                                <CardTitle className="flex items-center gap-2">
                                    <FileText className="h-5 w-5" />
                                    Job Details
                                </CardTitle>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                <FormField
                                    control={form.control}
                                    name="title"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>
                                                Title <span className="text-destructive">*</span>
                                            </FormLabel>
                                            <FormControl>
                                                <Input placeholder="e.g., Kitchen Renovation" {...field} />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name="jobDescription"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Description</FormLabel>
                                            <FormControl>
                                                <Textarea
                                                    placeholder="Describe the work to be done..."
                                                    {...field}
                                                />
                                            </FormControl>
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name="location"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel className="flex items-center gap-1">
                                                <MapPin className="h-4 w-4" />
                                                Location
                                            </FormLabel>
                                            <FormControl>
                                                <Input placeholder="Job site address" {...field} />
                                            </FormControl>
                                        </FormItem>
                                    )}
                                />
                                <div className="space-y-2">
                                    <FormLabel className="flex items-center gap-1">
                                        <User className="h-4 w-4" />
                                        Customer
                                    </FormLabel>
                                    {selectedCustomerName && (
                                        <div className="flex items-center justify-between p-2 bg-muted rounded-md text-sm">
                                            <span>{selectedCustomerName}</span>
                                            <Button
                                                type="button"
                                                variant="ghost"
                                                size="sm"
                                                onClick={() => {
                                                    form.setValue('clientId', undefined);
                                                    setSelectedCustomerName(null);
                                                }}
                                            >
                                                <X className="h-4 w-4" />
                                            </Button>
                                        </div>
                                    )}
                                    <CustomerSearch
                                        onSelect={(client) => {
                                            form.setValue('clientId', String(client.id));
                                            setSelectedCustomerName(
                                                `${client.firstName} ${client.lastName}${client.entityName ? ` (${client.entityName})` : ''}`
                                            );
                                        }}
                                    />
                                </div>
                            </CardContent>
                        </Card>

                        {/* Status & Scheduling */}
                        <Card>
                            <CardHeader>
                                <CardTitle className="flex items-center gap-2">
                                    <Calendar className="h-5 w-5" />
                                    Status & Scheduling
                                </CardTitle>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                <FormField
                                    control={form.control}
                                    name="status"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Status</FormLabel>
                                            <Select onValueChange={field.onChange} value={field.value}>
                                                <FormControl>
                                                    <SelectTrigger>
                                                        <SelectValue placeholder="Select status" />
                                                    </SelectTrigger>
                                                </FormControl>
                                                <SelectContent>
                                                    {Object.entries(JOB_STATUS_LABELS).map(([key, label]) => (
                                                        <SelectItem key={key} value={key}>
                                                            {label}
                                                        </SelectItem>
                                                    ))}
                                                </SelectContent>
                                            </Select>
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name="scheduledDate"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Scheduled Date</FormLabel>
                                            <FormControl>
                                                <Input type="date" {...field} />
                                            </FormControl>
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name="completionDate"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Completion Date</FormLabel>
                                            <FormControl>
                                                <Input type="date" {...field} />
                                            </FormControl>
                                        </FormItem>
                                    )}
                                />
                            </CardContent>
                        </Card>
                    </div>

                    {/* Job Notes - Only show for existing jobs */}
                    {!isNew && (
                        <Card>
                            <CardHeader>
                                <CardTitle className="flex items-center gap-2">
                                    <FileText className="h-5 w-5" />
                                    Daily Notes
                                </CardTitle>
                                <CardDescription>
                                    Add timestamped notes to track progress on this job
                                </CardDescription>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                {/* Add Note Form */}
                                <div className="flex gap-2 items-end">
                                    <div className="flex-1">
                                        <Textarea
                                            placeholder="Add a progress note..."
                                            value={newNoteText}
                                            onChange={(e) => setNewNoteText(e.target.value)}
                                            rows={2}
                                        />
                                    </div>
                                    <Button
                                        type="button"
                                        onClick={handleAddNote}
                                        disabled={isAddingNote || !newNoteText.trim()}
                                    >
                                        {isAddingNote ? <Loader2 className="h-4 w-4 animate-spin" /> : <Plus className="h-4 w-4" />}
                                        <span className="ml-1">Add</span>
                                    </Button>
                                </div>

                                {/* Notes List */}
                                {job?.notes && job.notes.length > 0 && (
                                    <div className="space-y-2 mt-4 border-t pt-4">
                                        {job.notes.map((note) => (
                                            <div
                                                key={note.id}
                                                className="flex items-start justify-between p-3 border rounded-lg bg-muted/50"
                                            >
                                                <div className="flex-1">
                                                    <p className="text-sm">{note.noteText}</p>
                                                    <p className="text-xs text-muted-foreground mt-1">
                                                        {new Date(note.noteDate).toLocaleString()}
                                                    </p>
                                                </div>
                                                <Button
                                                    variant="ghost"
                                                    size="sm"
                                                    onClick={() => handleDeleteNote(note.id)}
                                                >
                                                    <X className="h-4 w-4 text-destructive" />
                                                </Button>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </CardContent>
                        </Card>
                    )}

                    {/* Time Entries - Only show for existing jobs */}
                    {!isNew && (
                        <Card>
                            <CardHeader>
                                <CardTitle className="flex items-center gap-2">
                                    <Clock className="h-5 w-5" />
                                    Time Entries
                                </CardTitle>
                                <CardDescription>
                                    Track time spent on this job
                                </CardDescription>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                {/* Summary */}
                                {job?.timeEntries && job.timeEntries.length > 0 && (
                                    <div className="flex gap-4 p-3 bg-muted rounded-lg text-sm">
                                        <div className="flex items-center gap-2">
                                            <Clock className="h-4 w-4 text-muted-foreground" />
                                            <span className="font-medium">Total: {formatDuration(calculateTotalTime().totalMinutes)}</span>
                                        </div>
                                        {calculateTotalTime().totalBillable > 0 && (
                                            <div className="flex items-center gap-2">
                                                <DollarSign className="h-4 w-4 text-muted-foreground" />
                                                <span className="font-medium">Billable: ${calculateTotalTime().totalBillable.toFixed(2)}</span>
                                            </div>
                                        )}
                                    </div>
                                )}

                                {/* Add/Edit Time Entry Form */}
                                <div className="space-y-3 p-4 border rounded-lg bg-muted/30">
                                    <div className="flex items-center justify-between">
                                        <span className="text-sm font-medium">
                                            {editingTimeEntryId ? 'Edit Time Entry' : 'Add Time Entry'}
                                        </span>
                                        {editingTimeEntryId && (
                                            <Button
                                                type="button"
                                                variant="ghost"
                                                size="sm"
                                                onClick={resetTimeEntryForm}
                                            >
                                                Cancel
                                            </Button>
                                        )}
                                    </div>
                                    <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                                        <div>
                                            <Label className="text-xs">Date</Label>
                                            <Input
                                                type="date"
                                                value={timeEntryDate}
                                                onChange={(e) => setTimeEntryDate(e.target.value)}
                                            />
                                        </div>
                                        <div>
                                            <Label className="text-xs">Hours</Label>
                                            <Input
                                                type="number"
                                                min="0"
                                                placeholder="0"
                                                value={timeEntryHours}
                                                onChange={(e) => setTimeEntryHours(e.target.value)}
                                            />
                                        </div>
                                        <div>
                                            <Label className="text-xs">Minutes</Label>
                                            <Input
                                                type="number"
                                                min="0"
                                                max="59"
                                                placeholder="0"
                                                value={timeEntryMinutes}
                                                onChange={(e) => setTimeEntryMinutes(e.target.value)}
                                            />
                                        </div>
                                        <div>
                                            <Label className="text-xs">Employee</Label>
                                            <Input
                                                placeholder="Who did the work?"
                                                value={timeEntryEmployeeName}
                                                onChange={(e) => setTimeEntryEmployeeName(e.target.value)}
                                            />
                                        </div>
                                    </div>
                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                                        <div>
                                            <Label className="text-xs">Description</Label>
                                            <Input
                                                placeholder="What was done?"
                                                value={timeEntryDescription}
                                                onChange={(e) => setTimeEntryDescription(e.target.value)}
                                            />
                                        </div>
                                        <div>
                                            <Label className="text-xs">Hourly Rate (optional)</Label>
                                            <Input
                                                type="number"
                                                min="0"
                                                step="0.01"
                                                placeholder="$0.00"
                                                value={timeEntryHourlyRate}
                                                onChange={(e) => setTimeEntryHourlyRate(e.target.value)}
                                            />
                                        </div>
                                    </div>
                                    <div className="flex items-center justify-between">
                                        <div className="flex items-center gap-2">
                                            <Switch
                                                id="billable"
                                                checked={timeEntryBillable}
                                                onCheckedChange={setTimeEntryBillable}
                                            />
                                            <Label htmlFor="billable" className="text-sm cursor-pointer">
                                                Billable
                                            </Label>
                                        </div>
                                        <Button
                                            type="button"
                                            onClick={handleAddOrUpdateTimeEntry}
                                            disabled={isAddingTimeEntry}
                                        >
                                            {isAddingTimeEntry ? (
                                                <Loader2 className="h-4 w-4 animate-spin mr-1" />
                                            ) : editingTimeEntryId ? (
                                                <Save className="h-4 w-4 mr-1" />
                                            ) : (
                                                <Plus className="h-4 w-4 mr-1" />
                                            )}
                                            {editingTimeEntryId ? 'Update' : 'Add'}
                                        </Button>
                                    </div>
                                </div>

                                {/* Time Entries List */}
                                {job?.timeEntries && job.timeEntries.length > 0 && (
                                    <div className="space-y-2 mt-4 border-t pt-4">
                                        {job.timeEntries.map((entry) => (
                                            <div
                                                key={entry.id}
                                                className="flex items-start justify-between p-3 border rounded-lg bg-muted/50"
                                            >
                                                <div className="flex-1 space-y-1">
                                                    <div className="flex items-center gap-2 flex-wrap">
                                                        <span className="font-medium">{formatDuration(entry.durationMinutes)}</span>
                                                        <span className="text-xs text-muted-foreground">
                                                            {new Date(entry.entryDate).toLocaleDateString()}
                                                        </span>
                                                        {entry.billable && (
                                                            <Badge variant="secondary" className="text-xs">Billable</Badge>
                                                        )}
                                                        {entry.hourlyRate && (
                                                            <span className="text-xs text-muted-foreground">
                                                                ${entry.hourlyRate}/hr
                                                            </span>
                                                        )}
                                                    </div>
                                                    {entry.employeeName && (
                                                        <p className="text-xs text-muted-foreground flex items-center gap-1">
                                                            <User className="h-3 w-3" /> {entry.employeeName}
                                                        </p>
                                                    )}
                                                    {entry.description && (
                                                        <p className="text-sm">{entry.description}</p>
                                                    )}
                                                </div>
                                                <div className="flex gap-1">
                                                    <Button
                                                        variant="ghost"
                                                        size="sm"
                                                        onClick={() => handleEditTimeEntry(entry)}
                                                    >
                                                        <Pencil className="h-4 w-4" />
                                                    </Button>
                                                    <Button
                                                        variant="ghost"
                                                        size="sm"
                                                        onClick={() => handleDeleteTimeEntry(entry.id)}
                                                    >
                                                        <X className="h-4 w-4 text-destructive" />
                                                    </Button>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </CardContent>
                        </Card>
                    )}

                    {/* Attachments - Only show for existing jobs */}
                    {!isNew && (
                        <Card>
                            <CardHeader>
                                <CardTitle className="flex items-center gap-2">
                                    <Paperclip className="h-5 w-5" />
                                    Attachments
                                </CardTitle>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                <FileUpload
                                    uploadUrl={`/api/companies/${selectedCompanyId}/jobs/${id}/attachments`}
                                    accept="image/*,application/pdf"
                                    maxSizeMB={10}
                                    uploadLabel="Upload Photo/Document"
                                    onUploadSuccess={() => {
                                        refetch();
                                        toast.success('Attachment uploaded');
                                    }}
                                />
                                {job?.attachments && job.attachments.length > 0 && (
                                    <div className="space-y-2 mt-4">
                                        <p className="text-sm font-medium">Uploaded Files:</p>
                                        {job.attachments.map((att) => (
                                            <div
                                                key={att.id}
                                                className="flex items-center justify-between p-3 border rounded-lg"
                                            >
                                                <span className="text-sm truncate flex-1">{att.fileName}</span>
                                                <div className="flex gap-2 ml-2">
                                                    <Button variant="ghost" size="sm" asChild>
                                                        <a
                                                            href={`/api/companies/${selectedCompanyId}/jobs/${id}/attachments/${att.id}`}
                                                            target="_blank"
                                                            rel="noopener noreferrer"
                                                        >
                                                            <Download className="h-4 w-4" />
                                                        </a>
                                                    </Button>
                                                    <Button
                                                        variant="ghost"
                                                        size="sm"
                                                        onClick={() => handleDeleteAttachment(att.id)}
                                                    >
                                                        <X className="h-4 w-4 text-destructive" />
                                                    </Button>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </CardContent>
                        </Card>
                    )}

                    {/* Linked Documents - Only show for existing jobs */}
                    {!isNew && (
                        <Card>
                            <CardHeader>
                                <CardTitle className="flex items-center gap-2">
                                    <LinkIcon className="h-5 w-5" />
                                    Linked Documents
                                </CardTitle>
                                <CardDescription>
                                    Quotes and invoices related to this job
                                </CardDescription>
                            </CardHeader>
                            <CardContent>
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                    {/* Linked Quotes */}
                                    <div className="space-y-3">
                                        <h4 className="text-sm font-medium">Linked Quotes</h4>
                                        <QuoteSearch
                                            onSelect={async (quote) => {
                                                if (!selectedCompanyId || !id) return;
                                                try {
                                                    await linkQuote.mutateAsync({ jobId: id, quoteId: String(quote.id), companyId: selectedCompanyId });
                                                    toast.success('Quote linked');
                                                    refetch();
                                                } catch {
                                                    toast.error('Failed to link quote');
                                                }
                                            }}
                                            excludeIds={job?.linkedQuotes?.map(q => String(q.id)) || []}
                                        />
                                        {job?.linkedQuotes && job.linkedQuotes.length > 0 ? (
                                            <div className="space-y-2">
                                                {job.linkedQuotes.map((quote) => (
                                                    <div
                                                        key={quote.id}
                                                        className="flex items-center justify-between p-2 border rounded"
                                                    >
                                                        <a
                                                            href={`/quotes/${quote.id}`}
                                                            className="text-sm hover:underline"
                                                        >
                                                            {quote.quoteNumber}
                                                        </a>
                                                        <div className="flex items-center gap-2">
                                                            <Badge variant="outline">{quote.status}</Badge>
                                                            <Button
                                                                type="button"
                                                                variant="ghost"
                                                                size="sm"
                                                                onClick={async () => {
                                                                    if (!selectedCompanyId || !id) return;
                                                                    try {
                                                                        await unlinkQuote.mutateAsync({ jobId: id, quoteId: quote.id, companyId: selectedCompanyId });
                                                                        toast.success('Quote unlinked');
                                                                        refetch();
                                                                    } catch {
                                                                        toast.error('Failed to unlink quote');
                                                                    }
                                                                }}
                                                            >
                                                                <X className="h-4 w-4 text-destructive" />
                                                            </Button>
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        ) : (
                                            <p className="text-sm text-muted-foreground">No linked quotes</p>
                                        )}
                                    </div>

                                    {/* Linked Invoices */}
                                    <div className="space-y-3">
                                        <h4 className="text-sm font-medium">Linked Invoices</h4>
                                        <InvoiceSearch
                                            onSelect={async (invoice) => {
                                                if (!selectedCompanyId || !id) return;
                                                try {
                                                    await linkInvoice.mutateAsync({ jobId: id, invoiceId: String(invoice.id), companyId: selectedCompanyId });
                                                    toast.success('Invoice linked');
                                                    refetch();
                                                } catch {
                                                    toast.error('Failed to link invoice');
                                                }
                                            }}
                                            excludeIds={job?.linkedInvoices?.map(i => String(i.id)) || []}
                                        />
                                        {job?.linkedInvoices && job.linkedInvoices.length > 0 ? (
                                            <div className="space-y-2">
                                                {job.linkedInvoices.map((invoice) => (
                                                    <div
                                                        key={invoice.id}
                                                        className="flex items-center justify-between p-2 border rounded"
                                                    >
                                                        <a
                                                            href={`/invoices/${invoice.id}`}
                                                            className="text-sm hover:underline"
                                                        >
                                                            {invoice.invoiceNumber}
                                                        </a>
                                                        <div className="flex items-center gap-2">
                                                            <Badge variant="outline">{invoice.status}</Badge>
                                                            <Button
                                                                type="button"
                                                                variant="ghost"
                                                                size="sm"
                                                                onClick={async () => {
                                                                    if (!selectedCompanyId || !id) return;
                                                                    try {
                                                                        await unlinkInvoice.mutateAsync({ jobId: id, invoiceId: invoice.id, companyId: selectedCompanyId });
                                                                        toast.success('Invoice unlinked');
                                                                        refetch();
                                                                    } catch {
                                                                        toast.error('Failed to unlink invoice');
                                                                    }
                                                                }}
                                                            >
                                                                <X className="h-4 w-4 text-destructive" />
                                                            </Button>
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        ) : (
                                            <p className="text-sm text-muted-foreground">No linked invoices</p>
                                        )}
                                    </div>
                                </div>
                            </CardContent>
                        </Card>
                    )}
                </form>
            </Form>

            {/* Delete Action */}
            {!isNew && (
                <div className="pt-8 border-t">
                    <Button variant="destructive" onClick={() => setIsDeleteDialogOpen(true)}>
                        <Trash className="mr-2 h-4 w-4" /> Delete Job
                    </Button>
                </div>
            )}

            <AlertDialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>Delete Job?</AlertDialogTitle>
                        <AlertDialogDescription>
                            This action cannot be undone. The job, all its notes, and attachments will be
                            permanently deleted.
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel>Cancel</AlertDialogCancel>
                        <AlertDialogAction onClick={handleDelete} className="bg-destructive hover:bg-destructive/90">
                            Delete
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>
        </div>
    );
}
