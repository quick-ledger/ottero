package io.quickledger.entities.expense;

public enum ExpenseCategory {
    ADVERTISING("Advertising & Marketing"),
    BANK_FEES("Bank Fees & Charges"),
    CAR_EXPENSES("Motor Vehicle Expenses"),
    COMMUNICATION("Telephone & Internet"),
    DEPRECIATION("Depreciation"),
    ELECTRICITY("Electricity & Gas"),
    ENTERTAINMENT("Entertainment (Non-deductible)"),
    FREIGHT("Freight & Courier"),
    INSURANCE("Insurance"),
    INTEREST("Interest Expense"),
    LEGAL_FEES("Legal & Professional Fees"),
    MEALS_TRAVEL("Meals (Travel)"),
    OFFICE_SUPPLIES("Office Supplies"),
    PROFESSIONAL_DEVELOPMENT("Training & Education"),
    RENT("Rent & Lease Payments"),
    REPAIRS_MAINTENANCE("Repairs & Maintenance"),
    SOFTWARE_SUBSCRIPTIONS("Software & Subscriptions"),
    SUBCONTRACTORS("Subcontractor Payments"),
    SUPER_CONTRIBUTIONS("Superannuation Contributions"),
    TRAVEL("Travel Expenses"),
    WAGES("Wages & Salaries"),
    OTHER("Other Expenses");

    private final String displayName;

    ExpenseCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
