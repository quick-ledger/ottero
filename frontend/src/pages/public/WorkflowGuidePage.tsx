import {
    FileText,
    Send,
    CheckCircle,
    Edit,
    CreditCard,
    Lock,
    ShieldCheck,
    RefreshCw,
    Copy,
    ArrowRight,
    Search,
    Users,
    UserPlus,
    Settings,
    Hash,
    Palette
} from 'lucide-react';
import { Separator } from '@/components/ui/separator';

export default function WorkflowGuidePage() {
    return (
        <div className="container mx-auto py-12 px-4 max-w-4xl">
            <h1 className="text-3xl font-bold mb-2">Ottero Guide</h1>
            <p className="text-muted-foreground mb-12">
                Understanding the lifecycle of Quotes and Invoices.
            </p>

            <div className="space-y-12">

                <section>
                    <div className="flex items-center gap-3 mb-6">
                        <UserPlus className="h-6 w-6 text-primary" />
                        <h2 className="text-2xl font-semibold">Getting Started</h2>
                    </div>

                    <div className="space-y-6">
                        <div className="flex gap-4">
                            <div className="mt-1"><Users className="h-5 w-5 text-muted-foreground" /></div>
                            <div>
                                <h3 className="font-medium">1. Sign Up & Company Setup</h3>
                                <p className="text-sm text-muted-foreground">
                                    The first step is to sign up and add your company details.
                                    This information (Logo, Address, Contact) will appear on all your quotes and invoices.
                                </p>
                                <p className="text-sm text-muted-foreground mt-2 bg-muted/50 p-3 rounded-md border border-border/50">
                                    <strong>Note:</strong> Currently, each account is limited to managing <strong>1 Company</strong>.
                                </p>
                            </div>
                        </div>
                    </div>
                </section>

                <Separator />

                <section>
                    <div className="flex items-center gap-3 mb-6">
                        <Users className="h-6 w-6 text-primary" />
                        <h2 className="text-2xl font-semibold">Customer Management</h2>
                    </div>

                    <div className="space-y-6">
                        <div className="flex gap-4">
                            <div className="mt-1"><Search className="h-5 w-5 text-muted-foreground" /></div>
                            <div>
                                <h3 className="font-medium">1. Smart Assignment</h3>
                                <p className="text-sm text-muted-foreground">
                                    When creating a quote, simply type a name. We'll instantly find the customer from your database.
                                </p>
                            </div>
                        </div>

                        <div className="flex gap-4">
                            <div className="mt-1"><UserPlus className="h-5 w-5 text-muted-foreground" /></div>
                            <div>
                                <h3 className="font-medium">2. Quick Add</h3>
                                <p className="text-sm text-muted-foreground">
                                    New client? Add their details on the spot without leaving your quote.
                                </p>
                            </div>
                        </div>

                        <div className="flex gap-4">
                            <div className="mt-1"><Users className="h-5 w-5 text-muted-foreground" /></div>
                            <div>
                                <h3 className="font-medium">3. Central Contacts</h3>
                                <p className="text-sm text-muted-foreground">
                                    View and manage all your customer relationships in the <strong>Customers</strong> menu.
                                </p>
                            </div>
                        </div>
                    </div>
                </section>

                <Separator />

                <section>
                    <div className="flex items-center gap-3 mb-6">
                        <FileText className="h-6 w-6 text-primary" />
                        <h2 className="text-2xl font-semibold">Quote Workflow</h2>
                    </div>

                    <div className="space-y-6">
                        <div className="flex gap-4">
                            <div className="mt-1"><Edit className="h-5 w-5 text-muted-foreground" /></div>
                            <div>
                                <h3 className="font-medium">1. Draft Mode</h3>
                                <p className="text-sm text-muted-foreground">
                                    Create a new quote. You can edit every detail freely:
                                </p>
                                <ul className="list-disc list-inside space-y-1 text-sm text-muted-foreground mt-2 ml-1">
                                    <li><strong>Customer:</strong> Add an existing customer to the quote by searching or create a new customer.</li>
                                    <li><strong>Line Items:</strong> Add unlimited items with flexible pricing.</li>
                                    <li><strong>GST:</strong> Toggle GST (10%) on/off for each item.</li>
                                    <li><strong>Discounts:</strong> Apply a global discount ($ or %) to the total.</li>
                                </ul>
                            </div>
                        </div>

                        <div className="flex gap-4">
                            <div className="mt-1"><Send className="h-5 w-5 text-muted-foreground" /></div>
                            <div>
                                <h3 className="font-medium">2. Send</h3>
                                <p className="text-sm text-muted-foreground">
                                    Once done, send the quote to your customer.
                                    Sending a quote locks it to ensure consistency.
                                    It becomes <strong>Read-Only</strong> while the customer reviews it.
                                </p>
                            </div>
                        </div>

                        <div className="flex gap-4">
                            <div className="mt-1"><ShieldCheck className="h-5 w-5 text-muted-foreground" /></div>
                            <div>
                                <h3 className="font-medium">3. Customer Decision</h3>
                                <p className="text-sm text-muted-foreground">
                                    Customers can <span className="text-green-600">Accept</span> or <span className="text-red-600">Reject</span> the quote online.
                                </p>
                            </div>
                        </div>

                        <div className="flex gap-4">
                            <div className="mt-1"><RefreshCw className="h-5 w-5 text-muted-foreground" /></div>
                            <div>
                                <h3 className="font-medium">4. Revisions</h3>
                                <p className="text-sm text-muted-foreground">
                                    To change a sent quote, use <strong>"Add Revision"</strong>. This creates a new version (Rev 2) and keeps the history.
                                </p>
                            </div>
                        </div>

                        <div className="flex gap-4">
                            <div className="mt-1"><ArrowRight className="h-5 w-5 text-muted-foreground" /></div>
                            <div>
                                <h3 className="font-medium">5. Convert to Invoice</h3>
                                <p className="text-sm text-muted-foreground">
                                    Once a quote is <strong>Accepted</strong>, you can instantly convert it to a draft Invoice with one click.
                                </p>
                            </div>
                        </div>

                        <div className="flex gap-4">
                            <div className="mt-1"><Copy className="h-5 w-5 text-muted-foreground" /></div>
                            <div>
                                <h3 className="font-medium">6. Duplicate</h3>
                                <p className="text-sm text-muted-foreground">
                                    Use <strong>"Duplicate Quote"</strong> to clone an existing quote. Perfect for creating templates or reusing quotes for different clients.
                                </p>
                            </div>
                        </div>
                    </div>
                </section>

                <Separator />

                {/* 2. Invoices Workflow */}
                <section>
                    <div className="flex items-center gap-3 mb-6">
                        <CreditCard className="h-6 w-6 text-primary" />
                        <h2 className="text-2xl font-semibold">Invoice Workflow</h2>
                    </div>

                    <div className="space-y-6">
                        <div className="flex gap-4">
                            <div className="mt-1"><Edit className="h-5 w-5 text-muted-foreground" /></div>
                            <div>
                                <h3 className="font-medium">1. Draft</h3>
                                <p className="text-sm text-muted-foreground">
                                    Created from an accepted quote or from scratch. Fully editable.
                                </p>
                            </div>
                        </div>

                        <div className="flex gap-4">
                            <div className="mt-1"><Lock className="h-5 w-5 text-muted-foreground" /></div>
                            <div>
                                <h3 className="font-medium">2. Issued / Sent</h3>
                                <p className="text-sm text-muted-foreground">
                                    Send the invoice to your customer. Once sent, an invoice is a legal document and is <strong>Locked/Read-Only</strong>.
                                    <br />No revisions allowed. If incorrect, you must <strong>Void / Cancel</strong> it and create a new one.
                                </p>
                            </div>
                        </div>

                        <div className="flex gap-4">
                            <div className="mt-1"><CheckCircle className="h-5 w-5 text-muted-foreground" /></div>
                            <div>
                                <h3 className="font-medium">3. Paid</h3>
                                <p className="text-sm text-muted-foreground">
                                    Payment is recorded and the lifecycle is complete.
                                </p>
                            </div>
                        </div>
                    </div>
                </section>



                <Separator />



                <section>
                    <div className="flex items-center gap-3 mb-6">
                        <CreditCard className="h-6 w-6 text-primary" />
                        <h2 className="text-2xl font-semibold">Online Payments with Stripe</h2>
                    </div>

                    <div className="space-y-6">
                        <div className="flex gap-4">
                            <div className="mt-1"><ShieldCheck className="h-5 w-5 text-muted-foreground" /></div>
                            <div>
                                <h3 className="font-medium">1. Connect Your Stripe Account</h3>
                                <p className="text-sm text-muted-foreground">
                                    Link your Stripe account to start accepting online payments directly through your invoices.
                                    Setup takes just a few minutes and gives your customers a secure, professional payment experience.
                                </p>
                            </div>
                        </div>

                        <div className="flex gap-4">
                            <div className="mt-1"><CheckCircle className="h-5 w-5 text-muted-foreground" /></div>
                            <div>
                                <h3 className="font-medium">2. Get Paid Faster</h3>
                                <p className="text-sm text-muted-foreground">
                                    Enable one-click payments for your customers. They can pay invoices instantly with credit card, debit card, or other payment methods.
                                    Funds are deposited directly into your bank account.
                                </p>
                            </div>
                        </div>

                        <div className="flex gap-4">
                            <div className="mt-1"><CreditCard className="h-5 w-5 text-muted-foreground" /></div>
                            <div>
                                <h3 className="font-medium">3. Zero Cost to You</h3>
                                <p className="text-sm text-muted-foreground">
                                    Payment processing fees are automatically added to the invoice total, so your customers pay a small surcharge to cover the cost.
                                    You receive the full invoice amount with no deductions.
                                </p>
                                <div className="mt-3 bg-blue-50 border border-blue-200 rounded-lg p-3">
                                    <p className="text-sm text-blue-900">
                                        <strong>Example:</strong> For a $1,000 invoice, the customer pays approximately $1,018 (1.8% surcharge).
                                        You receive the full $1,000 in your account.
                                    </p>
                                </div>
                            </div>
                        </div>

                        <div className="flex gap-4">
                            <div className="mt-1"><Lock className="h-5 w-5 text-muted-foreground" /></div>
                            <div>
                                <h3 className="font-medium">4. Secure & Compliant</h3>
                                <p className="text-sm text-muted-foreground">
                                    Stripe handles all payment security and compliance (PCI-DSS). Your customers' payment information is never stored on our servers,
                                    ensuring maximum security and peace of mind.
                                </p>
                            </div>
                        </div>
                    </div>
                </section>

                <Separator />

                <section>
                    <div className="flex items-center gap-3 mb-6">
                        <Settings className="h-6 w-6 text-primary" />
                        <h2 className="text-2xl font-semibold">Configuration & Customization</h2>
                    </div>

                    <div className="space-y-6">
                        <div className="flex gap-4">
                            <div className="mt-1"><Hash className="h-5 w-5 text-muted-foreground" /></div>
                            <div>
                                <h3 className="font-medium">1. Smart Sequencing</h3>
                                <p className="text-sm text-muted-foreground">
                                    Customize your quote and invoice number generation. Configure custom Prefixes, Postfixes, and Padding for both Quotes and Invoices.
                                    <br />
                                    <span className="text-xs bg-muted px-1.5 py-0.5 rounded text-foreground/80 mt-1 inline-block">Example: Q-0042-2024</span>
                                </p>
                            </div>
                        </div>

                        <div className="flex gap-4">
                            <div className="mt-1"><Palette className="h-5 w-5 text-muted-foreground" /></div>
                            <div>
                                <h3 className="font-medium">2. PDF Templates & Branding</h3>
                                <p className="text-sm text-muted-foreground">
                                    Make your invoice and quote pdf files stand out:
                                </p>
                                <ul className="list-disc list-inside space-y-1 text-sm text-muted-foreground mt-2 ml-1">
                                    <li><strong>Logo:</strong> Upload your company logo and customize its size & position.</li>
                                    <li><strong>Payment Details:</strong> Add default Bank details (BSB/Account) to every invoice.</li>
                                    <li><strong>Notes:</strong> Define default payment terms or notes that appear automatically.</li>

                                </ul>
                            </div>
                        </div>
                    </div>
                </section>

                <Separator />

                <div className="pt-8 text-center">
                    <a href="/quotes" className="text-primary hover:underline text-sm font-medium">Create a new Quote &rarr;</a>
                </div>
            </div>
        </div >
    );
}
