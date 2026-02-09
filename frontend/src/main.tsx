import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { Auth0Provider } from '@auth0/auth0-react';
import './index.css';
import App from './App.tsx';

const root = createRoot(document.getElementById('root')!);

import { getEnv } from '@/utils/env';

const domain = getEnv('AUTH0_DOMAIN');
const clientId = getEnv('AUTH0_CLIENT_ID');
const audience = getEnv('AUTH0_AUDIENCE');

if (!domain || !clientId) {
  throw new Error('Missing required Auth0 environment variables. Please check your .env file.');
}

root.render(
  <Auth0Provider
    domain={domain}
    clientId={clientId}
    authorizationParams={{
      redirect_uri: window.location.origin,
      scope: "openid profile email",
      ...(audience && { audience })
    }}
    cacheLocation="localstorage"
    onRedirectCallback={(appState) => {
      window.history.replaceState(
        {},
        document.title,
        appState?.returnTo || window.location.pathname
      );
    }}
  >
    <App />
  </Auth0Provider>
);
