import { useAuth0 } from '@auth0/auth0-react';
import axios, { type AxiosInstance } from 'axios';
import { useMemo } from 'react';

export const useApi = (): AxiosInstance => {
    const { getAccessTokenSilently } = useAuth0();

    const api = useMemo(() => {
        const instance = axios.create({
            baseURL: import.meta.env.VITE_API_URL || '/', // Use env var for mobile/prod, fallback to proxy for dev
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json',
            },
        });

        instance.interceptors.request.use(async (config) => {
            try {
                const token = await getAccessTokenSilently();
                config.headers.Authorization = `Bearer ${token}`;
            } catch (error) {
                // If getting the token fails (e.g. not logged in),
                // we generally just proceed without the token and let
                // the backend return 401, which we handle elsewhere.
            }
            return config;
        });

        return instance;
    }, [getAccessTokenSilently]);

    return api;
};
