export const INVOICE_STATUS_LABELS: Record<string, string> = {
    DRAFT: 'Draft',
    SENT: 'Sent to Customer',
    PAID: 'Paid',
    CANCELLED: 'Cancelled',
};

export const INVOICE_STATUS_COLORS: Record<string, "default" | "secondary" | "destructive" | "outline" | "success" | "warning" | "info"> = {
    DRAFT: 'secondary',
    SENT: 'info',
    PAID: 'success',
    CANCELLED: 'secondary', // or destructive?
};

export const getInvoiceStatusLabel = (status: string) => {
    return INVOICE_STATUS_LABELS[status] || status;
};

export const getInvoiceStatusColor = (status: string) => {
    return INVOICE_STATUS_COLORS[status] || 'outline';
};
