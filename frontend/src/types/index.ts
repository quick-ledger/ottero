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

// Expense types
export type ExpenseCategory =
    | 'ADVERTISING'
    | 'BANK_FEES'
    | 'CAR_EXPENSES'
    | 'COMMUNICATION'
    | 'DEPRECIATION'
    | 'ELECTRICITY'
    | 'ENTERTAINMENT'
    | 'FREIGHT'
    | 'INSURANCE'
    | 'INTEREST'
    | 'LEGAL_FEES'
    | 'MEALS_TRAVEL'
    | 'OFFICE_SUPPLIES'
    | 'PROFESSIONAL_DEVELOPMENT'
    | 'RENT'
    | 'REPAIRS_MAINTENANCE'
    | 'SOFTWARE_SUBSCRIPTIONS'
    | 'SUBCONTRACTORS'
    | 'SUPER_CONTRIBUTIONS'
    | 'TRAVEL'
    | 'WAGES'
    | 'OTHER';

export const EXPENSE_CATEGORY_LABELS: Record<ExpenseCategory, string> = {
    ADVERTISING: 'Advertising & Marketing',
    BANK_FEES: 'Bank Fees & Charges',
    CAR_EXPENSES: 'Motor Vehicle Expenses',
    COMMUNICATION: 'Telephone & Internet',
    DEPRECIATION: 'Depreciation',
    ELECTRICITY: 'Electricity & Gas',
    ENTERTAINMENT: 'Entertainment (Non-deductible)',
    FREIGHT: 'Freight & Courier',
    INSURANCE: 'Insurance',
    INTEREST: 'Interest Expense',
    LEGAL_FEES: 'Legal & Professional Fees',
    MEALS_TRAVEL: 'Meals (Travel)',
    OFFICE_SUPPLIES: 'Office Supplies',
    PROFESSIONAL_DEVELOPMENT: 'Training & Education',
    RENT: 'Rent & Lease Payments',
    REPAIRS_MAINTENANCE: 'Repairs & Maintenance',
    SOFTWARE_SUBSCRIPTIONS: 'Software & Subscriptions',
    SUBCONTRACTORS: 'Subcontractor Payments',
    SUPER_CONTRIBUTIONS: 'Superannuation Contributions',
    TRAVEL: 'Travel Expenses',
    WAGES: 'Wages & Salaries',
    OTHER: 'Other Expenses',
};

export type ExpenseStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'ARCHIVED';

export interface ExpenseAttachment {
    id: string;
    fileName: string;
    contentType: string;
    size: number;
}

export interface Expense {
    id: string;
    companyId: string;
    expenseDate: string;
    amount: number;
    gstAmount: number;
    netAmount: number;
    category: ExpenseCategory;
    vendor: string;
    expenseDescription: string;
    status: ExpenseStatus;
    taxDeductible: boolean;
    gstClaimable: boolean;
    referenceNumber?: string;
    paymentMethod?: string;
    notes?: string;
    financialYear: string;
    createdDate?: string;
    modifiedDate?: string;
    attachments?: ExpenseAttachment[];
}

export interface ExpenseSummary {
    totalExpenses: number;
    totalGstClaimable: number;
    totalTaxDeductible: number;
    expenseCount: number;
    byCategory: {
        category: string;
        categoryDisplayName: string;
        amount: number;
        gstAmount: number;
        count: number;
    }[];
    byMonth: {
        month: string;
        amount: number;
        gstAmount: number;
        count: number;
    }[];
}
