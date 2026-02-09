
import ReactMarkdown from 'react-markdown';
import { Card, CardContent } from '@/components/ui/card';
import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { ArrowLeft } from 'lucide-react';

const privacyContent = `
# Privacy Policy

**Effective Date:** January 1, 2026

### 1. Introduction
At AI Soft Labs Pty Ltd (trading as Ottero), we take your privacy seriously. This Privacy Policy outlines how we collect, use, and protect your personal information when you use our services.

### 2. Information Collection
We collect:
- **Account Information**: Your name, email, and company details provided during registration.
- **Business Data**: Information you input about your company, products, services, and customers (e.g., client names, addresses) for the purpose of generating quotes and invoices.
- **Usage Data**: Information on how you interact with our Service.

**Note**: We do **not** collect or store sensitive payment information (such as credit card numbers) belonging to your customers.

### 3. Use of Information
We use the collected information for the following purposes:
- **Service Provision**: To generate and manage your business documents (quotes, invoices) and maintain your account.
- **Communication**: To contact you regarding your account, updates, or support inquiries.
- **Improvement**: To analyze usage and improve our products and services.

### 4. Data Protection
We implement reasonable security measures to protect your personal information from unauthorized access, alteration, disclosure, or destruction. However, no method of transmission over the internet or distinct electronic storage is 100% secure.

### 5. Sharing of Information
We do not sell, trade, or otherwise transfer your personally identifiable information to outside parties without your consent, except as described below:
- **Service Providers**: We may share information with trusted third parties who assist us in operating our website, conducting our business, or serving you, so long as those parties agree to keep this information confidential.
- **Payment Processing**: We use Stripe for secure payment processing. We do not store your full credit card information on our servers.
- **Legal Requirements**: We may release your information when we believe release is appropriate to comply with the law, enforce our site policies, or protect ours or others' rights, property, or safety.

### 6. Your Rights
You have the right to:
- Access the personal information we hold about you.
- Request correction of any incorrect information.
- Request deletion of your account and personal data, subject to legal and contractual obligations.

### 7. Changes to This Policy
We reserve the right to modify this privacy policy at any time. Changes will be effective immediately upon posting on our website. Your continued use of the service constitutes your agreement to the new privacy policy.

### 8. Contact Us
If you have any questions about this Privacy Policy, please contact us at info@ottero.com.au.
`;

const PrivacyPage = () => {
    return (
        <div className="min-h-screen bg-background p-4 md:p-8">
            <div className="max-w-4xl mx-auto space-y-6">
                <Link to="/">
                    <Button variant="ghost" className="mb-4">
                        <ArrowLeft className="h-4 w-4 mr-2" /> Back to Home
                    </Button>
                </Link>

                <Card>
                    <CardContent className="pt-6 prose dark:prose-invert max-w-none">
                        <ReactMarkdown>{privacyContent}</ReactMarkdown>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
};

export default PrivacyPage;
