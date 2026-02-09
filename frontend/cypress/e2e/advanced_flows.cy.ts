
describe('Advanced Authenticated Flows', () => {
    beforeEach(() => {
        // This logs in explicitly and then restores the session for subsequent tests
        cy.loginToAuth0();

        // Visit dashboard to ensure we are ready
        cy.visit('/dashboard');
        // Wait for company selection (badge in navbar)
        cy.get('nav').find('.bg-secondary\\/50').should('be.visible');
    });

    describe('Company Management', () => {
        const timestamp = Date.now();
        const companyPhone = `04${timestamp.toString().substring(6)}`; // Fake phone like 04123456

        it('should modify company details and verify persistence', () => {
            // Navigate to companies list
            // Navigate to Company Details via Settings
            cy.contains('button', 'Settings').click();
            cy.wait(500);
            cy.contains('a', 'Company Details').click();

            // Wait for loading to finish
            cy.contains('Loading company details...').should('not.exist');
            cy.wait(1000); // Extra stability for form hydration

            // Verify we are on the edit page (Company Details)
            cy.contains('h1', 'Company Details').should('exist');
            // Or look for a known field
            cy.get('input[name="name"]').should('exist');

            // No need to click "Edit" in a table, we are already there.

            // Update phone number
            cy.get('input[name="phone"]').should('be.visible').clear({ force: true }).type(companyPhone, { force: true });

            // Save
            cy.contains('button', 'Save Changes').click({ force: true });

            // Expect success toast
            cy.contains('Company updated successfully').should('be.visible');

            // Reload/Come back and verify
            cy.visit('/companies');
            cy.wait(1000);
            cy.get('table tbody tr').first().find('a').contains('Edit').click();
            cy.get('input[name="phone"]').should('have.value', companyPhone);
        });
    });

    describe('Sequence Configuration & Usage', () => {
        const quotePrefix = `QT-${Date.now()}-`;
        const invoicePrefix = `INV-${Date.now()}-`;

        it('should update sequence format and generate new documents with it', () => {
            // 1. Go to Sequence Config
            cy.contains('button', 'Settings').click();
            cy.wait(500);
            cy.contains('a', 'Number Sequences').click();

            // 2. Update Quote Sequence
            // 2. Update Quote Sequence (index 0)
            cy.get('input[name="prefix"]').eq(0).should('be.visible').clear({ force: true }).type(quotePrefix, { force: true });
            cy.get('input[name="currentNumber"]').eq(0).clear({ force: true }).type('500', { force: true });
            cy.contains('button', 'Save Quote Config').click({ force: true });
            cy.contains('Quote sequence updated').should('be.visible');

            // 3. Update Invoice Sequence (index 1)
            cy.get('input[name="prefix"]').eq(1).should('be.visible').clear({ force: true }).type(invoicePrefix, { force: true });
            cy.get('input[name="currentNumber"]').eq(1).clear({ force: true }).type('900', { force: true });
            cy.contains('button', 'Save Invoice Config').click({ force: true });
            cy.contains('Invoice sequence updated').should('be.visible');


            // 4. Create a New Quote and verify Prefix
            cy.visit('/quotes/new');

            // Select a customer (required to save)
            // We need to type in the combobox/search
            // Assuming CustomerSearch component:
            // Select a customer
            // Assuming the search component uses a button trigger (common in shadcn/radix)
            cy.contains('label', 'Search Existing Client').parent().find('button').click({ force: true });

            // Allow popover to open
            cy.get('[role="dialog"], [role="listbox"]').should('be.visible');

            // Search and select
            cy.get('input[placeholder*="Search"]').type('Test');
            cy.wait(500); // debounce
            cy.get('[role="option"]').first().click();

            // Add a Line Item (Required)
            cy.contains('button', 'Add Item').click();
            cy.get('input[placeholder="Description"]').type('Test Service');
            cy.get('input[placeholder="0.00"]').first().clear().type('100'); // Price

            // Save
            cy.contains('button', 'Save').click();
            cy.contains('Quote created successfully').should('be.visible');

            // Verify the Quote Number matches our new prefix
            cy.get('h1').should('contain', quotePrefix);
            cy.get('h1').should('contain', '500'); // It should use the Next Number we set

            // 5. Convert to Invoice and verify Invoice Prefix
            // Click "Convert to Invoice"
            cy.contains('button', 'Convert to Invoice').click();
            cy.on('window:confirm', () => true); // Auto accept confirmation if any

            // Verify success and redirection
            cy.contains('Converted to Invoice').should('be.visible');

            // Check Invoice Number
            // Wait for navigation
            cy.url().should('include', '/invoices/');
            cy.get('h1').should('contain', invoicePrefix);
            cy.get('h1').should('contain', '900');
        });
    });

    describe('Quote & Invoice Modifications', () => {
        it('should edit an existing quote', () => {
            cy.visit('/quotes');
            // Click Edit on first quote
            cy.get('table tbody tr').first().find('a').contains('Edit').click();

            // Change notes
            const newNotes = `Updated Notes ${Date.now()}`;
            cy.get('textarea[name="notes"]').clear().type(newNotes);

            // Save
            cy.contains('button', 'Save').click();
            cy.contains('Quote updated successfully').should('be.visible');

            // Verify
            cy.reload();
            cy.get('textarea[name="notes"]').should('have.value', newNotes);
        });

        it('should edit an existing invoice', () => {
            cy.visit('/invoices');
            // Wait for table to load
            cy.get('table').should('exist');
            cy.wait(1000);

            // Click Edit
            cy.get('table tbody tr').first().find('a').contains('Edit').click();

            // Update Due Date
            // e.g. set to tomorrow
            // Simply getting the input and typing a date yyyy-mm-dd
            const targetDate = new Date();
            targetDate.setDate(targetDate.getDate() + 5);
            const dateStr = targetDate.toISOString().split('T')[0];

            cy.get('input[name="dueDate"]').type(dateStr, { force: true });

            cy.contains('button', 'Save').click();
            cy.contains('Invoice updated successfully').should('be.visible');

            // Verify
            cy.reload();
            cy.get('input[name="dueDate"]').should('have.value', dateStr);
        });
    });

    describe('Settings Pages', () => {
        it('should view profile page', () => {
            cy.visit('/settings/profile');
            cy.contains('h1', 'Profile & Account').should('be.visible');
            cy.contains('Personal Information').should('be.visible');
            // Should show email
            // We can check if email is present (from login env)
            const email = Cypress.env('test_username');
            if (email) {
                cy.contains(email).should('be.visible');
            }
        });

        it('should view pricing page', () => {
            cy.visit('/settings/pricing');
            cy.contains('h1', 'Simple, Transparent Pricing').should('be.visible');
            cy.contains('Free').should('be.visible');
            cy.contains('Basic').should('be.visible');
        });
    });

});
