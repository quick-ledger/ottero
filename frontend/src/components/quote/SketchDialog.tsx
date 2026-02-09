import { useState, useRef } from 'react';
import { ReactSketchCanvas } from 'react-sketch-canvas';

// Fix for type issue where ReactSketchCanvasRef is not exported or found
type CanvasRef = any;
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogFooter,
    DialogTrigger,
} from "@/components/ui/dialog";
import { Button } from '@/components/ui/button';
import { Pen, Undo, Redo, Trash, Save, Eraser } from 'lucide-react';
import { Loader2 } from 'lucide-react';

interface SketchDialogProps {
    onSave: (blob: Blob) => Promise<void>;
    trigger?: React.ReactNode;
    open?: boolean;
    onOpenChange?: (open: boolean) => void;
}

export function SketchDialog({ onSave, trigger, open, onOpenChange }: SketchDialogProps) {
    const canvasRef = useRef<ReactSketchCanvasRef>(null);
    const [isSaving, setIsSaving] = useState(false);
    const [strokeColor, setStrokeColor] = useState('#000000');
    const [eraseMode, setEraseMode] = useState(false);
    const [internalOpen, setInternalOpen] = useState(false);

    // Handle controlled or uncontrolled open state
    const isOpen = open !== undefined ? open : internalOpen;
    const setIsOpen = onOpenChange || setInternalOpen;

    const handleSave = async () => {
        if (!canvasRef.current) return;

        try {
            setIsSaving(true);
            const dataUrl = await canvasRef.current.exportImage("png");
            const res = await fetch(dataUrl);
            const blob = await res.blob();
            await onSave(blob);
            setIsOpen(false);
            // Reset canvas for next time
            // canvasRef.current.clearCanvas(); 
        } catch (error) {
            console.error("Failed to save sketch", error);
        } finally {
            setIsSaving(false);
        }
    };

    const handleClear = () => {
        canvasRef.current?.clearCanvas();
    };

    const handleUndo = () => {
        canvasRef.current?.undo();
    };

    const handleRedo = () => {
        canvasRef.current?.redo();
    };

    return (
        <Dialog open={isOpen} onOpenChange={setIsOpen}>
            {trigger && <DialogTrigger asChild>{trigger}</DialogTrigger>}
            <DialogContent className="sm:max-w-[800px] h-[80vh] flex flex-col p-0 gap-0">
                <DialogHeader className="p-4 border-b">
                    <DialogTitle>Draw Sketch</DialogTitle>
                </DialogHeader>

                <div className="flex-1 bg-white relative overflow-hidden touch-none">
                    <ReactSketchCanvas
                        ref={canvasRef}
                        strokeWidth={4}
                        strokeColor={strokeColor}
                        eraserWidth={20}
                        style={{ border: 'none' }}
                        canvasColor="transparent"
                    />
                </div>

                <div className="p-4 border-t bg-muted/20 flex flex-col md:flex-row gap-4 justify-between items-center">
                    <div className="flex items-center gap-2">
                        <Button
                            variant={!eraseMode && strokeColor === '#000000' ? "secondary" : "ghost"}
                            size="icon"
                            onClick={() => { setEraseMode(false); setStrokeColor('#000000'); canvasRef.current?.eraseMode(false); }}
                            title="Black Pen"
                        >
                            <div className="w-4 h-4 rounded-full bg-black" />
                        </Button>
                        <Button
                            variant={!eraseMode && strokeColor === '#ef4444' ? "secondary" : "ghost"}
                            size="icon"
                            onClick={() => { setEraseMode(false); setStrokeColor('#ef4444'); canvasRef.current?.eraseMode(false); }}
                            title="Red Pen"
                        >
                            <div className="w-4 h-4 rounded-full bg-red-500" />
                        </Button>
                        <Button
                            variant={!eraseMode && strokeColor === '#3b82f6' ? "secondary" : "ghost"}
                            size="icon"
                            onClick={() => { setEraseMode(false); setStrokeColor('#3b82f6'); canvasRef.current?.eraseMode(false); }}
                            title="Blue Pen"
                        >
                            <div className="w-4 h-4 rounded-full bg-blue-500" />
                        </Button>

                        <div className="w-px h-6 bg-border mx-2" />

                        <Button
                            variant={eraseMode ? "secondary" : "ghost"}
                            size="icon"
                            onClick={() => { setEraseMode(true); canvasRef.current?.eraseMode(true); }}
                            title="Eraser"
                        >
                            <Eraser className="h-4 w-4" />
                        </Button>
                        <Button variant="ghost" size="icon" onClick={handleClear} title="Clear All">
                            <Trash className="h-4 w-4" />
                        </Button>
                    </div>

                    <div className="flex items-center gap-2">
                        <Button variant="ghost" size="icon" onClick={handleUndo} title="Undo">
                            <Undo className="h-4 w-4" />
                        </Button>
                        <Button variant="ghost" size="icon" onClick={handleRedo} title="Redo">
                            <Redo className="h-4 w-4" />
                        </Button>
                        <div className="w-px h-6 bg-border mx-2" />
                        <Button onClick={handleSave} disabled={isSaving}>
                            {isSaving ? <Loader2 className="h-4 w-4 mr-2 animate-spin" /> : <Save className="h-4 w-4 mr-2" />}
                            Save Sketch
                        </Button>
                    </div>
                </div>
            </DialogContent>
        </Dialog>
    );
}
