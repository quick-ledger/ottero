import { useState, useRef } from 'react';
import { Button } from '@/components/ui/button';
import { Mic, MicOff, Loader2 } from 'lucide-react';
import { useApi } from '@/hooks/useApi';
import { toast } from 'sonner';

interface VoiceRecorderProps {
    companyId: string | number;
    documentType: 'quote' | 'invoice';
    onDraftCreated: (draft: any) => void;
}

export function VoiceRecorder({ companyId, documentType, onDraftCreated }: VoiceRecorderProps) {
    const [isRecording, setIsRecording] = useState(false);
    const [isProcessing, setIsProcessing] = useState(false);
    const mediaRecorderRef = useRef<MediaRecorder | null>(null);
    const chunksRef = useRef<Blob[]>([]);
    const api = useApi();

    const startRecording = async () => {
        try {
            const stream = await navigator.mediaDevices.getUserMedia({ audio: true });

            // Use webm for better browser support, backend can handle it
            const mediaRecorder = new MediaRecorder(stream, {
                mimeType: 'audio/webm;codecs=opus'
            });

            mediaRecorderRef.current = mediaRecorder;
            chunksRef.current = [];

            mediaRecorder.ondataavailable = (e) => {
                if (e.data.size > 0) {
                    chunksRef.current.push(e.data);
                }
            };

            mediaRecorder.onstop = async () => {
                // Stop all tracks
                stream.getTracks().forEach(track => track.stop());

                const audioBlob = new Blob(chunksRef.current, { type: 'audio/webm' });
                await sendAudioToBackend(audioBlob);
            };

            mediaRecorder.start();
            setIsRecording(true);
            toast.info('Recording... Speak your quote details');
        } catch (error) {
            console.error('Failed to start recording:', error);
            toast.error('Could not access microphone. Please check permissions.');
        }
    };

    const stopRecording = () => {
        if (mediaRecorderRef.current && isRecording) {
            mediaRecorderRef.current.stop();
            setIsRecording(false);
        }
    };

    const sendAudioToBackend = async (audioBlob: Blob) => {
        setIsProcessing(true);

        try {
            const formData = new FormData();
            formData.append('audio', audioBlob, 'recording.webm');
            formData.append('documentType', documentType);
            formData.append('languageCode', 'en-AU');
            formData.append('encoding', 'WEBM_OPUS');

            const response = await api.post(
                `/api/companies/${companyId}/voice/draft`,
                formData,
                {
                    headers: {
                        'Content-Type': 'multipart/form-data',
                    },
                }
            );

            const { quote, invoice, transcript, warnings } = response.data;

            if (warnings && warnings.length > 0) {
                warnings.forEach((w: string) => toast.warning(w));
            }

            toast.success(`Draft created from voice: "${transcript?.substring(0, 50)}..."`);

            onDraftCreated(documentType === 'quote' ? quote : invoice);
        } catch (error: any) {
            console.error('Voice draft failed:', error);
            const message = error?.response?.data?.message || error?.response?.data || 'Failed to process voice input';
            toast.error(message);
        } finally {
            setIsProcessing(false);
        }
    };

    const handleClick = () => {
        if (isRecording) {
            stopRecording();
        } else {
            startRecording();
        }
    };

    if (isProcessing) {
        return (
            <Button variant="outline" disabled>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Processing...
            </Button>
        );
    }

    return (
        <Button
            variant={isRecording ? "destructive" : "outline"}
            onClick={handleClick}
            type="button"
            title={isRecording ? "Stop recording" : "Create quote by voice"}
        >
            {isRecording ? (
                <>
                    <MicOff className="mr-2 h-4 w-4" />
                    Stop Recording
                </>
            ) : (
                <>
                    <Mic className="mr-2 h-4 w-4" />
                    Voice
                </>
            )}
        </Button>
    );
}
