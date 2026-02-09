
import { useEffect } from 'react';

const useTawk = () => {
    useEffect(() => {
        const script = document.createElement("script");
        script.async = true;
        script.src = 'https://embed.tawk.to/695dee0540fac11981e53b86/1jebejhnq';
        script.charset = 'UTF-8';
        script.setAttribute('crossorigin', '*');

        const firstScript = document.getElementsByTagName("script")[0];
        if (firstScript && firstScript.parentNode) {
            firstScript.parentNode.insertBefore(script, firstScript);
        } else {
            document.head.appendChild(script);
        }

        // Optional: Cleanup if you want to remove the widget on unmount
        // But usually chat widgets persist. 
        // For a SPA, you might want to hide/show it.
        // Tawk API might allow Tawk_API.hideWidget() / showWidget()

        return () => {
            // Cleanup if needed, though often tricky with external widgets
        };
    }, []);
};

export default useTawk;
