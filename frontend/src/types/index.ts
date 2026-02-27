export interface Company {
    id: string;
    name: string;
    email: string;
    abn: string;
    phone?: string;
    address?: string;
    logoUrl?: string;

    // Banking details
    bank_bsb?: string;
    bank_account?: string;
    website?: string;
    stripeConnectedAccountId?: string;
    stripeChargesEnabled?: boolean;
}

export interface Customer {
    id: string;
    firstName: string;
    lastName: string;
    email: string;
    phone?: string;
    phoneNumber?: string;
    companyId: string;
    clientEntityName?: string;
}

export interface Product {
    id: string;
    name: string;
    description: string;
    price: number;
    companyId: string;
}

export interface Service {
    id: string;
    name: string;
    description: string;
    price: number;
    companyId: string;
}

export interface QuoteItem {
    id?: string;
    itemOrder: number;
    itemDescription: string;
    quantity: number;
    price: number;
    total: number; // calculated
    gst: 0 | 10;
    serviceItemId?: string | null;
    productItemId?: string | null;
}

export interface Quote {
    id: string;
    quoteNumber: string;
    quoteDate: string;
    expiryDate: string;
    status: 'PENDING' | 'SENT' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED';

    // discount
    discountType: 'DOLLAR' | 'PERCENT';
    discountValue: number;

    // totals
    totalPrice: number;
    gst: number;

    // client info snapshot (denormalized)
    clientId: string;
    clientFirstname: string;
    clientLastname: string;
    clientEntityName: string;
    clientPhone: string;
    clientEmail: string;

    notes?: string;
    quoteRevision: number;

    quoteItems: QuoteItem[];
    companyId: string;
    relatedInvoices?: { id: string; invoiceNumber: string }[];
}

export interface InvoiceItem extends QuoteItem { }

export type RecurringFrequency = 'WEEKLY' | 'BIWEEKLY' | 'MONTHLY' | 'QUARTERLY' | 'ANNUALLY';

export interface Invoice {
    id: string;
    invoiceNumber: string;
    quoteId?: string;
    quoteNumber?: string;
    issueDate: string;
    dueDate: string;
    status: 'DRAFT' | 'SENT' | 'PAID' | 'CANCELLED';
    totalPrice: number;
    gst: number;
    discountType: 'DOLLAR' | 'PERCENT';
    discountValue: number;
    clientId: string;
    clientFirstname?: string;
    clientLastname?: string;
    clientEntityName?: string;
    clientEmail?: string;
    clientPhone?: string;
    invoiceItems: InvoiceItem[];
    companyId: string;
    notes?: string;
    paymentLink?: string;

    // Recurring invoice fields
    isRecurring?: boolean;
    recurringFrequency?: RecurringFrequency;
    recurringEndDate?: string;
    recurringAutoSend?: boolean;
    nextRecurringDate?: string;
    parentInvoiceId?: string;
}

// Helper types for API responses
export interface PaginatedResponse<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
}

// Referral types
export type ReferralStatus = 'PENDING' | 'SIGNED_UP' | 'DISCOUNT_APPLIED';

export interface Referral {
    refereeEmail: string;
    refereeName: string | null;
    status: ReferralStatus;
    referralCode: string;
    createdDate: string;
}
