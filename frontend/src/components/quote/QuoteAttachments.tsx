import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { useApi } from '@/hooks/useApi';
import { Capacitor } from '@capacitor/core';
import { Camera, CameraResultType, CameraSource } from '@capacitor/camera';
import { toast } from 'sonner';
import { Loader2, Paperclip, Trash, Camera as CameraIcon, Upload, Paintbrush } from 'lucide-react';
import { SketchDialog } from './SketchDialog';

interface Attachment {
    id: number;
    fileName: string;
    contentType: string;
    size: number;
}

interface QuoteAttachmentsProps {
    companyId: string;
    quoteId: string;
}

export function QuoteAttachments({ companyId, quoteId }: QuoteAttachmentsProps) {
    const [attachments, setAttachments] = useState<Attachment[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [isUploading, setIsUploading] = useState(false);
    const api = useApi();

    useEffect(() => {
        loadAttachments();
    }, [quoteId, companyId]);

    const loadAttachments = async () => {
        if (!quoteId || quoteId === 'new') return;
        try {
            setIsLoading(true);
            const { data } = await api.get(`/api/companies/${companyId}/quotes/${quoteId}`);
            setAttachments(data.attachments || []);
        } catch (error) {
            console.error(error);
            toast.error("Failed to load attachments");
        } finally {
            setIsLoading(false);
        }
    };

    const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
        if (!e.target.files || e.target.files.length === 0) return;
        const file = e.target.files[0];
        await uploadFile(file);
        // Reset input value to allow selecting same file again
        e.target.value = '';
    };

    const uploadFile = async (file: File | Blob, fileName?: string) => {
        const formData = new FormData();
        formData.append('file', file, fileName || (file as File).name);

        try {
            setIsUploading(true);
            await api.post(`/api/companies/${companyId}/quotes/${quoteId}/attachments`, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });
            toast.success("Attachment uploaded");
            loadAttachments();
        } catch (error) {
            console.error(error);
            toast.error("Failed to upload attachment");
        } finally {
            setIsUploading(false);
        }
    };

    const handleSketchSave = async (blob: Blob) => {
        const fileName = `sketch_${new Date().getTime()}.png`;
        await uploadFile(blob, fileName);
    };

    const takePhoto = async () => {
        try {
            const image = await Camera.getPhoto({
                quality: 90,
                allowEditing: false,
                resultType: CameraResultType.Uri,
                source: CameraSource.Camera
            });

            if (!image.webPath) return;

            // Read the file into a blob
            const response = await fetch(image.webPath);
            const blob = await response.blob();
            // Default filename with timestamp
            const fileName = `photo_${new Date().getTime()}.jpeg`;

            await uploadFile(blob, fileName);

        } catch (error) {
            console.error("Camera error:", error);
            // Ignore cancelled
        }
    };

    const deleteAttachment = async (id: number) => {
        if (!confirm("Delete this attachment?")) return;
        try {
            await api.delete(`/api/companies/${companyId}/quotes/${quoteId}/attachments/${id}`);
            toast.success("Attachment deleted");
            setAttachments(prev => prev.filter(a => a.id !== id));
        } catch (error) {
            console.error(error);
            toast.error("Failed to delete attachment");
        }
    };

    const downloadAttachment = async (attachment: Attachment) => {
        try {
            const response = await api.get(`/api/companies/${companyId}/quotes/${quoteId}/attachments/${attachment.id}`, {
                responseType: 'blob',
            });

            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', attachment.fileName);
            document.body.appendChild(link);
            link.click();
            link.remove();
        } catch (error) {
            console.error(error);
            toast.error("Failed to download attachment");
        }
    };

    // if (quoteId === 'new') return null; // Can't attach to unsaved quote
    const isNew = !quoteId || quoteId === 'new';

    return (
        <Card>
            <CardHeader className="flex flex-row items-center justify-between py-4">
                <CardTitle className="text-lg">Attachments</CardTitle>
                <div className="flex gap-2">
                    <SketchDialog
                        onSave={handleSketchSave}
                        trigger={
                            <Button
                                size="sm"
                                disabled={isUploading || isNew}
                                title={isNew ? "Save quote first" : "Add Sketch"}
                                className="bg-teal-600 hover:bg-teal-700 text-white border-teal-600 hover:border-teal-700 shadow-sm"
                            >
                                <Paintbrush className="h-4 w-4 mr-2" />
                                Sketch
                            </Button>
                        }
                    />
                    <Button variant="outline" size="sm" onClick={() => document.getElementById('file-upload')?.click()} disabled={isUploading || isNew} title={isNew ? "Save quote first" : "Upload file"}>
                        <Upload className="h-4 w-4 mr-2" />
                        Upload
                    </Button>
                    <input
                        id="file-upload"
                        type="file"
                        className="hidden"
                        onChange={handleFileUpload}
                        accept="image/*,application/pdf"
                        disabled={isNew}
                    />
                    {Capacitor.getPlatform() !== 'web' && (
                        <Button variant="outline" size="sm" onClick={takePhoto} disabled={isUploading || isNew} title={isNew ? "Save quote first" : "Take photo"}>
                            <CameraIcon className="h-4 w-4 mr-2" />
                            Camera
                        </Button>
                    )}
                </div>
            </CardHeader>
            <CardContent>
                {isNew ? (
                    <div className="text-sm text-muted-foreground text-center py-4">Please save the quote to add attachments</div>
                ) : isLoading ? (
                    <div className="flex justify-center p-4"><Loader2 className="animate-spin" /></div>
                ) : attachments.length === 0 ? (
                    <div className="text-sm text-muted-foreground text-center py-4">No attachments</div>
                ) : (
                    <div className="space-y-2">
                        {attachments.map(att => (
                            <div key={att.id} className="flex items-center justify-between p-2 border rounded-md hover:bg-muted/50">
                                <div className="flex items-center gap-3 overflow-hidden">
                                    <Paperclip className="h-4 w-4 flex-shrink-0 text-muted-foreground" />
                                    <span
                                        className="text-sm truncate cursor-pointer hover:underline font-medium"
                                        onClick={() => downloadAttachment(att)}
                                    >
                                        {att.fileName}
                                    </span>
                                    <span className="text-xs text-muted-foreground">
                                        ({Math.round(att.size / 1024)} KB)
                                    </span>
                                </div>
                                <Button variant="ghost" size="icon" className="h-8 w-8 text-destructive hover:text-destructive hover:bg-destructive/10" onClick={() => deleteAttachment(att.id)}>
                                    <Trash className="h-4 w-4" />
                                </Button>
                            </div>
                        ))}
                    </div>
                )}
            </CardContent>
        </Card>
    );
}
