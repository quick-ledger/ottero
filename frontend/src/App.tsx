import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { Suspense, lazy } from 'react';
import { useAuth0 } from '@auth0/auth0-react';

const queryClient = new QueryClient();

import AppLayout, { ProtectedRoute } from '@/components/layout/AppLayout';
import LandingPage from '@/pages/LandingPage';
import CompanyListPage from '@/pages/companies/CompanyListPage';
import CompanyEditPage from '@/pages/companies/CompanyEditPage';
import QuoteListPage from '@/pages/quotes/QuoteListPage';
import QuoteEditPage from '@/pages/quotes/QuoteEditPage';
import InvoiceListPage from '@/pages/invoices/InvoiceListPage';
import InvoiceEditPage from '@/pages/invoices/InvoiceEditPage';
import CallbackPage from '@/pages/CallbackPage';

// Lazy-loaded components
const Dashboard = lazy(() => import('@/pages/DashboardPage'));
const CustomerListPage = lazy(() => import('@/pages/customers/CustomerListPage'));
const CustomerEditPage = lazy(() => import('@/pages/customers/CustomerEditPage'));

const SequenceConfigPage = lazy(() => import('@/pages/settings/SequenceConfigPage'));
const TemplateConfigPage = lazy(() => import('@/pages/settings/TemplateConfigPage'));
const PricingPage = lazy(() => import('@/pages/settings/PricingPage'));
const ProfilePage = lazy(() => import('@/pages/settings/ProfilePage'));
const ReferralPage = lazy(() => import('@/pages/settings/ReferralPage'));

const QuoteViewer = lazy(() => import('@/pages/public/QuoteViewer'));
const InvoiceViewer = lazy(() => import('@/pages/public/InvoiceViewer'));
const ContactPage = lazy(() => import('@/pages/ContactPage'));
const TermsPage = lazy(() => import('@/pages/public/TermsPage'));
const PrivacyPage = lazy(() => import('@/pages/public/PrivacyPage'));
const WorkflowGuidePage = lazy(() => import('@/pages/public/WorkflowGuidePage'));

import { Toaster } from '@/components/ui/sonner';

import { App as CapApp } from '@capacitor/app';
import { useEffect } from 'react';

function App() {
  const { isLoading } = useAuth0();

  useEffect(() => {
    CapApp.addListener('appUrlOpen', ({ url }) => {
      if (url.includes('code') && url.includes('state')) {
        // This is an Auth0 callback
        try {
          // Capacitor opens "https://localhost/...", strictly speaking
          // inside the WebView we are already at "https://localhost".
          // We just need to ensure the router/window sees the params.
          const openUrl = new URL(url);
          if (openUrl.pathname === '/' && openUrl.search) {
            // Force the window to process the query params
            window.location.href = url;
          }
        } catch (e) {
          console.error('Error handling callback URL', e);
        }
      }
    });
  }, []);

  if (isLoading) {
    return <div className="flex items-center justify-center h-screen">Loading...</div>;
  }

  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
        <Suspense fallback={<div>Loading...</div>}> {/* Add Suspense for lazy loading */}
          <Routes>
            {/* Public Viewer Routes - No Layout/Navbar */}
            <Route path="/public/quotes/:id" element={<QuoteViewer />} />
            <Route path="/public/invoices/view" element={<InvoiceViewer />} />
            <Route path="/terms" element={<TermsPage />} />
            <Route path="/privacy" element={<PrivacyPage />} />

            <Route path="/" element={<AppLayout />}>
              <Route index element={<LandingPage />} />
              <Route path="/callback" element={<CallbackPage />} />
              <Route path="/guide" element={<WorkflowGuidePage />} />
              <Route path="contact" element={<ContactPage />} />

              <Route element={<ProtectedRoute />}>
                <Route path="dashboard" element={<Dashboard />} />
                <Route path="companies" element={<CompanyListPage />} />
                <Route path="companies/new" element={<CompanyEditPage />} />
                <Route path="companies/:id" element={<CompanyEditPage />} />
                <Route path="settings/sequences" element={<SequenceConfigPage />} />
                <Route path="settings/template-config" element={<TemplateConfigPage />} />
                <Route path="settings/pricing" element={<PricingPage />} />
                <Route path="settings/profile" element={<ProfilePage />} />
                <Route path="settings/referrals" element={<ReferralPage />} />


                <Route path="quotes" element={<QuoteListPage />} />
                <Route path="quotes/:id" element={<QuoteEditPage />} />
                <Route path="invoices" element={<InvoiceListPage />} />
                <Route path="invoices/:id" element={<InvoiceEditPage />} />
                <Route path="customers" element={<CustomerListPage />} />
                <Route path="customers/new" element={<CustomerEditPage />} />
                <Route path="customers/:id" element={<CustomerEditPage />} />



              </Route>
            </Route>
          </Routes>
        </Suspense>
        <Toaster />
      </BrowserRouter>
    </QueryClientProvider>
  );
}

export default App;
