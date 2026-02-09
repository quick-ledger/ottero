import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { useApi } from '@/hooks/useApi';
import { toast } from 'sonner';
import { Upload, Trash, Image as ImageIcon } from 'lucide-react';

interface FileUploadProps {
    /**
     * Upload endpoint URL
     */
    uploadUrl: string;

    /**
     * Download/preview URL (optional)
     */
    downloadUrl?: string;

    /**
     * Accepted file types (e.g., "image/*", "application/pdf")
     */
    accept?: string;

    /**
     * Maximum file size in MB
     */
    maxSizeMB?: number;

    /**
     * Show image preview for image files
     */
    showPreview?: boolean;

    /**
     * Upload button label
     */
    uploadLabel?: string;

    /**
     * Callback when file is uploaded successfully
     */
    onUploadSuccess?: () => void;

    /**
     * Callback when file is deleted successfully
     */
    onDeleteSuccess?: () => void;

    /**
     * Optional delete URL (if different from upload URL)
     */
    deleteUrl?: string;

    /**
     * Alternative button variant
     */
    variant?: 'default' | 'outline' | 'secondary' | 'ghost';

    /**
     * Button size
     */
    size?: 'default' | 'sm' | 'lg' | 'icon';

    /**
     * Whether to show delete button
     */
    showDelete?: boolean;

    /**
     * Custom class for the container
     */
    className?: string;
}

export function FileUpload({
    uploadUrl,
    downloadUrl,
    accept = '*/*',
    maxSizeMB = 5,
    showPreview = false,
    uploadLabel = 'Upload',
    onUploadSuccess,
    onDeleteSuccess,
    deleteUrl,
    variant = 'outline',
    size = 'sm',
    showDelete = false,
    className = '',
}: FileUploadProps) {
    const [isUploading, setIsUploading] = useState(false);
    const [preview, setPreview] = useState<string | null>(null);
    const [imageError, setImageError] = useState(false);
    const [imageDataUrl, setImageDataUrl] = useState<string | null>(null);
    const api = useApi();

    // Fetch image data through API when downloadUrl changes
    useEffect(() => {
        const fetchImage = async () => {
            if (!downloadUrl) {
                setImageDataUrl(null);
                setPreview(null);
                setImageError(false);
                return;
            }

            try {
                const response = await api.get(downloadUrl, { responseType: 'blob' });
                const blob = response.data;
                const dataUrl = URL.createObjectURL(blob);
                setImageDataUrl(dataUrl);
                setPreview(dataUrl);
                setImageError(false);
            } catch (error) {
                console.error('Failed to load image:', error);
                setImageError(true);
                setImageDataUrl(null);
                setPreview(null);
            }
        };

        fetchImage();

        // Cleanup function to revoke object URL
        return () => {
            if (imageDataUrl) {
                URL.revokeObjectURL(imageDataUrl);
            }
        };
    }, [downloadUrl]);

    const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
        if (!e.target.files || e.target.files.length === 0) return;

        const file = e.target.files[0];

        // Validate file size
        const fileSizeMB = file.size / (1024 * 1024);
        if (fileSizeMB > maxSizeMB) {
            toast.error(`File size must not exceed ${maxSizeMB}MB`);
            e.target.value = '';
            return;
        }

        // Validate file type if accept is specified
        if (accept !== '*/*' && !matchesAccept(file.type, accept)) {
            toast.error(`Invalid file type. Accepted: ${accept}`);
            e.target.value = '';
            return;
        }

        await uploadFile(file);

        // Reset input to allow selecting same file again
        e.target.value = '';
    };

    const matchesAccept = (fileType: string, acceptString: string): boolean => {
        const acceptPatterns = acceptString.split(',').map(s => s.trim());
        return acceptPatterns.some(pattern => {
            if (pattern === '*/*') return true;
            if (pattern.endsWith('/*')) {
                const prefix = pattern.slice(0, -2);
                return fileType.startsWith(prefix + '/');
            }
            return fileType === pattern;
        });
    };

    const uploadFile = async (file: File) => {
        const formData = new FormData();
        formData.append('file', file);

        try {
            setIsUploading(true);
            await api.post(uploadUrl, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });

            toast.success('File uploaded successfully');

            // Create preview for images
            if (showPreview && file.type.startsWith('image/')) {
                const previewUrl = URL.createObjectURL(file);
                setPreview(previewUrl);
            }

            onUploadSuccess?.();
        } catch (error: any) {
            console.error('Upload error:', error);
            const errorMessage = error.response?.data || error.message || 'Failed to upload file';
            toast.error(errorMessage);
        } finally {
            setIsUploading(false);
        }
    };

    const handleDelete = async () => {
        if (!confirm('Are you sure you want to delete this file?')) return;

        const urlToDelete = deleteUrl || uploadUrl;

        try {
            setIsUploading(true);
            await api.delete(urlToDelete);
            toast.success('File deleted successfully');
            setPreview(null);
            onDeleteSuccess?.();
        } catch (error: any) {
            console.error('Delete error:', error);
            const errorMessage = error.response?.data || error.message || 'Failed to delete file';
            toast.error(errorMessage);
        } finally {
            setIsUploading(false);
        }
    };

    const uploadId = `file-upload-${Math.random().toString(36).substr(2, 9)}`;
    const isImage = accept.includes('image');
    const hasFile = !!(preview && !imageError);

    return (
        <div className={`flex items-center gap-4 ${className}`}>
            {showPreview && isImage && (
                <div className="relative">
                    {hasFile ? (
                        <>
                            <img
                                src={preview || ''}
                                alt="Preview"
                                className="w-24 h-24 object-contain border-2 border-dashed border-gray-300 rounded-lg p-2 bg-gray-50"
                                onError={() => setImageError(true)}
                            />
                            {showDelete && (
                                <Button
                                    type="button"
                                    variant="destructive"
                                    size="icon"
                                    className="absolute -top-2 -right-2 h-6 w-6 rounded-full"
                                    onClick={handleDelete}
                                    disabled={isUploading}
                                >
                                    <Trash className="h-3 w-3" />
                                </Button>
                            )}
                        </>
                    ) : (
                        <div className="w-24 h-24 border-2 border-dashed border-gray-300 rounded-lg p-2 bg-gray-50 flex items-center justify-center">
                            <ImageIcon className="h-8 w-8 text-gray-400" />
                        </div>
                    )}
                </div>
            )}

            {!showPreview && showDelete && hasFile && (
                <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    className="text-destructive hover:text-destructive hover:bg-destructive/10"
                    onClick={handleDelete}
                    disabled={isUploading}
                >
                    <Trash className="h-4 w-4" />
                </Button>
            )}

            <div>
                <Button
                    type="button"
                    variant={variant}
                    size={size}
                    onClick={() => document.getElementById(uploadId)?.click()}
                    disabled={isUploading}
                >
                    {isUploading ? (
                        <>Uploading...</>
                    ) : (
                        <>
                            <Upload className="h-4 w-4 mr-2" />
                            {hasFile && showPreview ? 'Change' : uploadLabel}
                        </>
                    )}
                </Button>
                <input
                    id={uploadId}
                    type="file"
                    className="hidden"
                    onChange={handleFileUpload}
                    accept={accept}
                    disabled={isUploading}
                />
                <p className="text-xs text-muted-foreground mt-1">
                    {accept.replace('image/*', 'Images')} (max {maxSizeMB}MB)
                </p>
            </div>
        </div>
    );
}
