import { useAuth0 } from '@auth0/auth0-react';
import axios, { type AxiosInstance } from 'axios';
import { useMemo } from 'react';

// Helper to add timeout to a promise
const withTimeout = <T,>(promise: Promise<T>, ms: number, errorMessage: string): Promise<T> => {
    return Promise.race([
        promise,
        new Promise<T>((_, reject) =>
            setTimeout(() => reject(new Error(errorMessage)), ms)
        ),
    ]);
};

export const useApi = (): AxiosInstance => {
    const { getAccessTokenSilently, loginWithRedirect } = useAuth0();

    const api = useMemo(() => {
        const instance = axios.create({
            baseURL: import.meta.env.VITE_API_URL || '/', // Use env var for mobile/prod, fallback to proxy for dev
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json',
            },
        });

        instance.interceptors.request.use(async (config) => {
            const startTime = performance.now();
            try {
                // Add 5 second timeout to prevent hanging
                const token = await withTimeout(
                    getAccessTokenSilently(),
                    5000,
                    'Token fetch timeout - Auth0 not responding'
                );
                const elapsed = performance.now() - startTime;
                if (elapsed > 100) {
                    console.warn(`[useApi] getAccessTokenSilently took ${elapsed.toFixed(0)}ms for ${config.url}`);
                }
                config.headers.Authorization = `Bearer ${token}`;
            } catch (error: unknown) {
                const elapsed = performance.now() - startTime;
                console.error(`[useApi] getAccessTokenSilently failed after ${elapsed.toFixed(0)}ms:`, error);

                // Check if this is a login_required, consent_required, or timeout error
                const errorMessage = error instanceof Error ? error.message : String(error);
                if (errorMessage.includes('login_required') ||
                    errorMessage.includes('Login required') ||
                    errorMessage.includes('Consent required') ||
                    errorMessage.includes('consent_required') ||
                    errorMessage.includes('timeout')) {
                    console.warn('[useApi] Auth issue, redirecting to login...', errorMessage);
                    loginWithRedirect();
                    // Throw to prevent the request from proceeding
                    throw new Error('Auth required - redirecting to login');
                }
                // For other errors (e.g. network issues), proceed without token
                // and let the backend return 401
                console.warn('[useApi] Proceeding without token due to error:', errorMessage);
            }
            return config;
        });

        return instance;
    }, [getAccessTokenSilently, loginWithRedirect]);

    return api;
};
