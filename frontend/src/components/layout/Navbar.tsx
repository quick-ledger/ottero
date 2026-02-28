import { Link } from 'react-router-dom';
import { useAppStore } from '@/store/useAppStore';
import { useAuth0 } from '@auth0/auth0-react';
import { Button } from '@/components/ui/button';
import { LogOut, Building2, ChevronDown, Menu } from 'lucide-react';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"

const Navbar = () => {
    const { selectedCompanyName, selectedCompanyId, actions } = useAppStore();
    const { logout, user, isAuthenticated, loginWithRedirect, isLoading, error } = useAuth0();

    if (isLoading) {
        return <nav className="border-b bg-background h-16" />; // Skeleton or empty nav while loading
    }

    if (error) {
        return (
            <nav className="border-b bg-background p-4 text-red-500">
                Auth Error: {error.message}
                <Button variant="outline" className="ml-4" onClick={() => loginWithRedirect()}>Try Login Again</Button>
            </nav>
        );
    }


    const handleLogout = () => {
        actions.clearSelectedCompany();
        logout({ logoutParams: { returnTo: window.location.origin } });
    };

    const NavbarLinks = () => (
        <>
            {isAuthenticated && (
                <>
                    <Link to="/dashboard" className="transition-colors hover:text-foreground/80 text-foreground/60 w-full md:w-auto">Dashboard</Link>
                    <Link to="/quotes" className="transition-colors hover:text-foreground/80 text-foreground/60 w-full md:w-auto">Quotes</Link>
                    <Link to="/invoices" className="transition-colors hover:text-foreground/80 text-foreground/60 w-full md:w-auto">Invoices</Link>
                    <Link to="/expenses" className="transition-colors hover:text-foreground/80 text-foreground/60 w-full md:w-auto">Expenses</Link>
                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <Button variant="ghost" className="text-foreground/60 hover:text-foreground/80 px-2 justify-start md:justify-center w-full md:w-auto">
                                Settings <ChevronDown className="ml-1 h-4 w-4" />
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="start">
                            <DropdownMenuLabel>Account</DropdownMenuLabel>
                            <DropdownMenuItem asChild>
                                <Link to="/settings/profile" className="cursor-pointer">Profile</Link>
                            </DropdownMenuItem>
                            <DropdownMenuItem asChild>
                                <Link to="/settings/pricing" className="cursor-pointer">Plans & Pricing</Link>
                            </DropdownMenuItem>
                            <DropdownMenuItem asChild>
                                <Link to="/settings/referrals" className="cursor-pointer">Refer a Friend</Link>
                            </DropdownMenuItem>

                            <DropdownMenuSeparator />
                            <DropdownMenuLabel>Your Data</DropdownMenuLabel>
                            {selectedCompanyId && (
                                <DropdownMenuItem asChild>
                                    <Link to={`/companies/${selectedCompanyId}`} className="cursor-pointer">Company Details</Link>
                                </DropdownMenuItem>
                            )}
                            <DropdownMenuItem asChild>
                                <Link to="/customers" className="cursor-pointer">Customers</Link>
                            </DropdownMenuItem>
                            <DropdownMenuSeparator />
                            <DropdownMenuLabel>Quote and Invoice Settings</DropdownMenuLabel>
                            <DropdownMenuItem asChild>
                                <Link to="/settings/template-config" className="cursor-pointer">PDF Template</Link>
                            </DropdownMenuItem>
                            <DropdownMenuItem asChild>
                                <Link to="/settings/sequences" className="cursor-pointer">Number Sequences</Link>
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                    <Link to="/guide" className="transition-colors hover:text-foreground/80 text-foreground/60 w-full md:w-auto">Guide</Link>
                    <Link to="/contact" className="transition-colors hover:text-foreground/80 text-foreground/60 w-full md:w-auto">Contact Us</Link>
                </>
            )}


        </>
    );

    return (
        <nav className="border-b bg-background">
            <div className="flex h-16 items-center px-4 md:px-8 justify-between">
                <Link to="/" className="text-lg font-bold flex items-center gap-2">
                    <img src="/logo-icon.png" alt="Ottero" className="h-8 md:h-10 w-auto" />
                    <span className="hidden xs:inline">Ottero</span>
                </Link>

                {/* Desktop Menu */}
                <div className="hidden md:flex items-center space-x-6 text-sm font-medium">
                    <NavbarLinks />
                </div>

                <div className="flex items-center space-x-2 md:space-x-4">
                    {/* Mobile Menu */}
                    <div className="md:hidden">
                        <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                                <Button variant="ghost" size="icon">
                                    <Menu className="h-6 w-6" />
                                </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end" className="w-56">
                                <div className="flex flex-col space-y-2 p-2">
                                    <NavbarLinks />
                                </div>
                            </DropdownMenuContent>
                        </DropdownMenu>
                    </div>

                    {isAuthenticated ? (
                        <>
                            {selectedCompanyName && (
                                <div className="hidden md:flex items-center text-sm text-foreground/80 bg-secondary/50 px-3 py-1 rounded-full">
                                    <Building2 className="mr-2 h-3 w-3" />
                                    {selectedCompanyName}
                                </div>
                            )}
                            <div className="hidden md:block text-sm font-medium text-muted-foreground">
                                {user?.email}
                            </div>
                            <Button variant="ghost" size="sm" onClick={handleLogout}>
                                <LogOut className="h-4 w-4 md:mr-2" />
                                <span className="hidden md:inline">Logout</span>
                            </Button>
                        </>
                    ) : (
                        <Button onClick={() => loginWithRedirect({ appState: { returnTo: '/dashboard' } })}>Login</Button>
                    )}
                </div>
            </div>
        </nav>
    );
};

export default Navbar;
