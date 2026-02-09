export const QUOTE_STATUS_LABELS: Record<string, string> = {
    PENDING: 'Pending',
    SENT: 'Sent',
    ACCEPTED: 'Accepted',
    REJECTED: 'Rejected',
    CANCELLED: 'Cancelled',
};

export const QUOTE_STATUS_COLORS: Record<string, "default" | "secondary" | "destructive" | "outline" | "success" | "warning" | "info"> = {
    PENDING: 'warning',
    SENT: 'info',
    ACCEPTED: 'success',
    REJECTED: 'destructive',
    CANCELLED: 'secondary',
};

export const getQuoteStatusLabel = (status: string) => {
    return QUOTE_STATUS_LABELS[status] || status;
};

export const getQuoteStatusColor = (status: string) => {
    return QUOTE_STATUS_COLORS[status] || 'outline';
};
