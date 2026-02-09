import { useAuth0 } from "@auth0/auth0-react";

export const CallbackPage = () => {
    const { error } = useAuth0();

    if (error) {
        return (
            <div className="flex h-screen w-full items-center justify-center">
                <div className="text-center">
                    <h1 className="text-2xl font-bold text-destructive">Error</h1>
                    <p>{error.message}</p>
                </div>
            </div>
        );
    }

    return (
        <div className="flex h-screen w-full items-center justify-center">
            <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-primary"></div>
        </div>
    );
};

export default CallbackPage;
