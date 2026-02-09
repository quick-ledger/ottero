import { Outlet, Navigate, useLocation } from 'react-router-dom';
import { withAuthenticationRequired } from '@auth0/auth0-react';
import { useEffect } from 'react';
import Navbar from './Navbar';
import { useSelectedCompanyId, useAppActions } from '@/store/useAppStore';
import { useCompanies } from '@/hooks/useCompanies';

const AppLayout = () => {
    return (
        <div className="flex min-h-screen flex-col bg-background">
            <Navbar />
            <main className="flex-1">
                <Outlet />
            </main>
        </div>
    );
};

// Component to handle route protection and company redirection
export const ProtectedLayout = ({ children }: { children?: React.ReactNode }) => {
    const selectedCompanyId = useSelectedCompanyId();
    const { setSelectedCompany } = useAppActions();
    const location = useLocation();
    const { data: companies, isLoading } = useCompanies();

    useEffect(() => {
        if (!isLoading && companies && companies.length === 1 && !selectedCompanyId) {
            setSelectedCompany(companies[0].id, companies[0].name);
        }
    }, [companies, isLoading, selectedCompanyId, setSelectedCompany]);

    if (isLoading) {
        return <div className="flex h-screen items-center justify-center">Loading...</div>;
    }

    // If no company is selected and we are not on the company list page, redirect to company list
    if (!selectedCompanyId && !location.pathname.startsWith('/companies')) {
        // If we have 1 company, we are auto-selecting, so don't redirect yet.
        if (companies && companies.length === 1) {
            return <div className="flex h-screen items-center justify-center">Selecting default company...</div>;
        }
        return <Navigate to="/companies" replace />;
    }

    return children ? <>{children}</> : <Outlet />;
};

// Wrap the protection component with authentication requirement
export const ProtectedRoute = withAuthenticationRequired(ProtectedLayout, {
    onRedirecting: () => (
        <div className="flex min-h-screen items-center justify-center">
            <div className="text-center">
                <div className="mb-4 text-lg">Loading...</div>
                <div className="text-sm text-muted-foreground">Authenticating</div>
            </div>
        </div>
    ),
});

export default AppLayout;
