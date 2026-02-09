/**
 * Utility to get environment variables with runtime fallback.
 * Checks window.env (injected by K8s/Docker) first, then Vite's build-time env.
 */
declare global {
    interface Window {
        env?: Record<string, string>;
    }
}

export const getEnv = (key: string): string => {
    // 1. Check runtime config (window.env) - standardized keys (e.g. AUTH0_DOMAIN)
    if (window.env && window.env[key]) {
        return window.env[key];
    }

    // 2. Check build-time config (Vite) - requires VITE_ prefix
    const viteKey = `VITE_${key}`;
    return import.meta.env[viteKey] || '';
};
