
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Mail, MessageCircle, MessageSquare } from 'lucide-react';
import useTawk from '@/hooks/useTawk';

const ContactPage = () => {
    useTawk();

    return (
        <div className="container mx-auto py-10 max-w-4xl space-y-8">
            <div className="space-y-2">
                <h1 className="text-3xl font-bold tracking-tight">Contact Us</h1>
                <p className="text-muted-foreground">
                    Get in touch with us through any of the channels below.
                </p>
            </div>

            <div className="grid gap-6 md:grid-cols-2">
                <Card>
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                            <Mail className="h-5 w-5" /> Email
                        </CardTitle>
                        <CardDescription>Send us an email anytime</CardDescription>
                    </CardHeader>
                    <CardContent>
                        <a href="mailto:info@ottero.com.au" className="text-sm font-medium hover:underline text-primary">
                            info@ottero.com.au
                        </a>

                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                            <MessageCircle className="h-5 w-5" /> WhatsApp
                        </CardTitle>
                        <CardDescription>Chat with us on WhatsApp</CardDescription>
                    </CardHeader>
                    <CardContent>
                        <p className="text-sm text-muted-foreground">Coming soon...</p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                            <MessageSquare className="h-5 w-5" /> Discord
                        </CardTitle>
                        <CardDescription>Join our community</CardDescription>
                    </CardHeader>
                    <CardContent>
                        <p className="text-sm text-muted-foreground">Coming soon...</p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                            Live Chat
                        </CardTitle>
                        <CardDescription>Talk to support instantly</CardDescription>
                    </CardHeader>
                    <CardContent>
                        <p className="text-sm text-muted-foreground">
                            Check the bottom right corner of your screen to start a chat with us!
                        </p>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
};

export default ContactPage;
