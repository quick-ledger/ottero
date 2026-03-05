import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { X, Download, Share } from 'lucide-react';

interface BeforeInstallPromptEvent extends Event {
    prompt: () => Promise<void>;
    userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>;
}

declare global {
    interface WindowEventMap {
        beforeinstallprompt: BeforeInstallPromptEvent;
    }
}

const DISMISSED_KEY = 'pwa-install-dismissed';
const DISMISS_DURATION = 7 * 24 * 60 * 60 * 1000; // 7 days

// Check if running on iOS
const isIOS = () => {
    return /iPad|iPhone|iPod/.test(navigator.userAgent) && !(window as { MSStream?: unknown }).MSStream;
};

// Check if already installed as standalone PWA
const isStandalone = () => {
    return window.matchMedia('(display-mode: standalone)').matches ||
        (navigator as { standalone?: boolean }).standalone === true;
};

export default function InstallPWA() {
    const [deferredPrompt, setDeferredPrompt] = useState<BeforeInstallPromptEvent | null>(null);
    const [showBanner, setShowBanner] = useState(false);
    const [isIOSDevice, setIsIOSDevice] = useState(false);

    useEffect(() => {
        // Don't show if already installed
        if (isStandalone()) return;

        // Check if already dismissed recently
        const dismissedAt = localStorage.getItem(DISMISSED_KEY);
        if (dismissedAt && Date.now() - parseInt(dismissedAt) < DISMISS_DURATION) {
            return;
        }

        // Check for iOS
        if (isIOS()) {
            setIsIOSDevice(true);
            setShowBanner(true);
            return;
        }

        // For Android/Chrome, listen for install prompt
        const handleBeforeInstallPrompt = (e: BeforeInstallPromptEvent) => {
            e.preventDefault();
            setDeferredPrompt(e);
            setShowBanner(true);
        };

        window.addEventListener('beforeinstallprompt', handleBeforeInstallPrompt);

        return () => {
            window.removeEventListener('beforeinstallprompt', handleBeforeInstallPrompt);
        };
    }, []);

    const handleInstall = async () => {
        if (!deferredPrompt) return;

        await deferredPrompt.prompt();
        const { outcome } = await deferredPrompt.userChoice;

        if (outcome === 'accepted') {
            setShowBanner(false);
            setDeferredPrompt(null);
        }
    };

    const handleDismiss = () => {
        setShowBanner(false);
        localStorage.setItem(DISMISSED_KEY, Date.now().toString());
    };

    if (!showBanner) return null;

    return (
        <div className="fixed bottom-0 left-0 right-0 z-50 p-4 bg-background border-t shadow-lg">
            <div className="container mx-auto flex items-center justify-between gap-4">
                <div className="flex items-center gap-3">
                    <img src="/logo-icon.png" alt="Ottero" className="h-10 w-auto" />
                    <div>
                        <p className="font-medium">Install Ottero</p>
                        <p className="text-sm text-muted-foreground">
                            {isIOSDevice
                                ? <>Tap <Share className="inline h-4 w-4" /> then "Add to Home Screen"</>
                                : 'Add to your home screen for quick access'
                            }
                        </p>
                    </div>
                </div>
                <div className="flex items-center gap-2">
                    {!isIOSDevice && (
                        <Button onClick={handleInstall} size="sm">
                            <Download className="h-4 w-4 mr-2" />
                            Install
                        </Button>
                    )}
                    <Button variant="ghost" size="icon" onClick={handleDismiss}>
                        <X className="h-4 w-4" />
                    </Button>
                </div>
            </div>
        </div>
    );
}
