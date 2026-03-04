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
    // Inventory fields
    quantityOnHand?: number;
    reorderPoint?: number;
    reorderQuantity?: number;
    trackInventory?: boolean;
    // Product attributes
    attributeValues?: ProductAttributeValue[];
}

// Product Attribute types (EVA pattern)
export type ProductAttributeDataType = 'STRING' | 'NUMBER' | 'DATE' | 'BOOLEAN';

export interface ProductAttributeDefinition {
    id: string;
    name: string;
    dataType: ProductAttributeDataType;
    unit?: string;
    companyId: string;
}

export interface ProductAttributeValue {
    id?: string;
    definitionId: string;
    definitionName?: string;
    dataType?: ProductAttributeDataType;
    unit?: string;
    value: string;
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
    invoiceDate: string;
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

// Job types
export type JobStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'ON_HOLD' | 'COMPLETED' | 'CANCELLED';

export const JOB_STATUS_LABELS: Record<JobStatus, string> = {
    SCHEDULED: 'Scheduled',
    IN_PROGRESS: 'In Progress',
    ON_HOLD: 'On Hold',
    COMPLETED: 'Completed',
    CANCELLED: 'Cancelled',
};

export interface JobNote {
    id: string;
    noteText: string;
    noteDate: string;
    createdDate: string;
}

export interface JobAttachment {
    id: string;
    fileName: string;
    contentType: string;
    size: number;
}

export interface JobTimeEntry {
    id: string;
    entryDate: string;
    durationMinutes: number;
    description?: string;
    billable: boolean;
    hourlyRate?: number;
    employeeName?: string;
    createdDate: string;
}

export interface LinkedQuote {
    id: string;
    quoteNumber: string;
    status: string;
    totalPrice: number;
}

export interface LinkedInvoice {
    id: string;
    invoiceNumber: string;
    status: string;
    totalPrice: number;
}

export interface Job {
    id: string;
    companyId: string;
    clientId?: string;
    clientName?: string;
    clientEmail?: string;
    clientPhone?: string;
    jobNumber: string;
    title: string;
    jobDescription?: string;
    location?: string;
    status: JobStatus;
    scheduledDate?: string;
    completionDate?: string;
    createdDate?: string;
    modifiedDate?: string;
    notes?: JobNote[];
    attachments?: JobAttachment[];
    timeEntries?: JobTimeEntry[];
    linkedQuotes?: LinkedQuote[];
    linkedInvoices?: LinkedInvoice[];
}

// Supplier types
export interface Supplier {
    id: string;
    companyId: string;
    name: string;
    contactName?: string;
    email?: string;
    phone?: string;
    address?: string;
    paymentTerms?: string;
    notes?: string;
    isActive: boolean;
}

// Purchase Order types
export type PurchaseOrderStatus = 'DRAFT' | 'SENT' | 'PARTIALLY_RECEIVED' | 'RECEIVED' | 'CANCELLED';

export const PURCHASE_ORDER_STATUS_LABELS: Record<PurchaseOrderStatus, string> = {
    DRAFT: 'Draft',
    SENT: 'Sent',
    PARTIALLY_RECEIVED: 'Partially Received',
    RECEIVED: 'Received',
    CANCELLED: 'Cancelled',
};

export interface PurchaseOrderItem {
    id?: string;
    itemOrder: number;
    productItemId: string;
    productName?: string;
    quantityOrdered: number;
    quantityReceived: number;
    unitPrice: number;
    gst: number;
    total: number;
}

export interface PurchaseOrder {
    id: string;
    companyId: string;
    supplierId: string;
    supplierName?: string;
    poNumber: string;
    orderDate: string;
    expectedDeliveryDate?: string;
    status: PurchaseOrderStatus;
    totalAmount: number;
    gst: number;
    notes?: string;
    items: PurchaseOrderItem[];
}

export interface ReceiveItemsRequest {
    purchaseOrderId: string;
    notes?: string;
    items: {
        purchaseOrderItemId: string;
        quantityReceived: number;
    }[];
}

// Inventory types
export type StockMovementType = 'SALE' | 'PURCHASE' | 'ADJUSTMENT' | 'TRANSFER' | 'RETURN';
export type StockReferenceType = 'INVOICE' | 'PURCHASE_ORDER' | 'MANUAL';

export const STOCK_MOVEMENT_TYPE_LABELS: Record<StockMovementType, string> = {
    SALE: 'Sale',
    PURCHASE: 'Purchase',
    ADJUSTMENT: 'Adjustment',
    TRANSFER: 'Transfer',
    RETURN: 'Return',
};

export interface StockMovement {
    id: string;
    companyId: string;
    productItemId: string;
    productName?: string;
    movementType: StockMovementType;
    quantityChange: number;
    quantityBefore: number;
    quantityAfter: number;
    referenceType: StockReferenceType;
    referenceId?: string;
    referenceNumber?: string;
    notes?: string;
    createdDate: string;
}

export interface LowStockAlert {
    productId: string;
    productName: string;
    productCode?: string;
    quantityOnHand: number;
    reorderPoint: number;
    reorderQuantity?: number;
}

export interface InventoryDashboard {
    totalProducts: number;
    lowStockCount: number;
    outOfStockCount: number;
    totalInventoryValue: number;
    lowStockAlerts: LowStockAlert[];
}

export interface StockAdjustmentRequest {
    newQuantity: number;
    reason: string;
}

// Asset types
export type AssetStatus = 'ACTIVE' | 'INACTIVE' | 'DISPOSED' | 'UNDER_MAINTENANCE';
export type DepreciationMethod = 'STRAIGHT_LINE' | 'DECLINING_BALANCE' | 'NONE';

export const ASSET_STATUS_LABELS: Record<AssetStatus, string> = {
    ACTIVE: 'Active',
    INACTIVE: 'Inactive',
    DISPOSED: 'Disposed',
    UNDER_MAINTENANCE: 'Under Maintenance',
};

export const DEPRECIATION_METHOD_LABELS: Record<DepreciationMethod, string> = {
    STRAIGHT_LINE: 'Straight Line',
    DECLINING_BALANCE: 'Declining Balance',
    NONE: 'None',
};

export interface AssetAttributeDefinition {
    id: string;
    name: string;
    dataType: ProductAttributeDataType;
    unit?: string;
    companyId: string;
}

export interface AssetAttributeValue {
    id?: string;
    definitionId: string;
    definitionName?: string;
    dataType?: ProductAttributeDataType;
    unit?: string;
    value: string;
}

export interface Asset {
    id: string;
    companyId: string;
    assetGroupId?: string;
    assetGroupName?: string;
    name: string;
    description?: string;
    code?: string;
    serialNumber?: string;
    location?: string;
    quantity?: number;
    status?: AssetStatus;
    // Financial tracking
    purchaseDate?: string;
    purchasePrice?: number;
    currentValue?: number;
    // Depreciation
    depreciationMethod?: DepreciationMethod;
    usefulLifeYears?: number;
    salvageValue?: number;
    // Attributes
    valueDTOs?: AssetAttributeValue[];
}

export interface AssetGroup {
    id: string;
    companyId: string;
    name: string;
    description?: string;
}
