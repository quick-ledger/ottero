describe('Invoice Workflows', () => {
    beforeEach(() => {
        cy.loginToAuth0();
        cy.visit('/dashboard');
        cy.get('nav').find('.bg-secondary\\/50').should('be.visible');
    });

    describe('Create Invoice', () => {
        it('should create an invoice with customer and line items', () => {
            cy.visit('/invoices/new');
            cy.contains('New Invoice').should('be.visible');

            // Select a customer using the search input
            cy.get('input[placeholder*="Search client"]').type('Test');
            cy.wait(1000);
            cy.get('[role="option"]').first().click({ force: true });

            // Add line item
            cy.contains('button', 'Add Item').click();
            cy.get('input[placeholder="Description"]').first().type('Electrical work');
            cy.get('table tbody tr').first().find('input[type="number"]').eq(0).clear().type('4');
            cy.get('table tbody tr').first().find('input[type="number"]').eq(1).clear().type('75');

            // Save
            cy.contains('button', 'Save').click();
            cy.contains('Invoice created successfully').should('be.visible');

            // Verify redirect to edit page
            cy.url().should('match', /\/invoices\/\d+/);
            cy.get('h1').should('contain', 'Invoice #');
        });

        it('should create an invoice with multiple line items', () => {
            cy.visit('/invoices/new');

            // Select a customer using the search input
            cy.get('input[placeholder*="Search client"]').type('Test');
            cy.wait(1000);
            cy.get('[role="option"]').first().click({ force: true });

            // Add first line item
            cy.contains('button', 'Add Item').click();
            cy.get('input[placeholder="Description"]').eq(0).type('Consultation');
            cy.get('table tbody tr').eq(0).find('input[type="number"]').eq(0).clear().type('1');
            cy.get('table tbody tr').eq(0).find('input[type="number"]').eq(1).clear().type('120');

            // Add second line item
            cy.contains('button', 'Add Item').click();
            cy.get('input[placeholder="Description"]').eq(1).type('Materials');
            cy.get('table tbody tr').eq(1).find('input[type="number"]').eq(0).clear().type('5');
            cy.get('table tbody tr').eq(1).find('input[type="number"]').eq(1).clear().type('25');

            // Verify 2 rows
            cy.get('table tbody tr').should('have.length', 2);

            // Save
            cy.contains('button', 'Save').click();
            cy.contains('Invoice created successfully').should('be.visible');
        });
    });

    describe('Line Item Management', () => {
        it('should remove a line item from invoice', () => {
            cy.visit('/invoices/new');

            // Select a customer using the search input
            cy.get('input[placeholder*="Search client"]').type('Test');
            cy.wait(1000);
            cy.get('[role="option"]').first().click({ force: true });

            // Add two items
            cy.contains('button', 'Add Item').click();
            cy.get('input[placeholder="Description"]').eq(0).type('Keep this');
            cy.get('table tbody tr').eq(0).find('input[type="number"]').eq(1).clear().type('100');

            cy.contains('button', 'Add Item').click();
            cy.get('input[placeholder="Description"]').eq(1).type('Delete this');
            cy.get('table tbody tr').eq(1).find('input[type="number"]').eq(1).clear().type('50');

            // Verify 2 rows
            cy.get('table tbody tr').should('have.length', 2);

            // Remove second item
            cy.get('table tbody tr').eq(1).find('button').filter(':has(svg.lucide-trash-2)').click();

            // Verify 1 row remains
            cy.get('table tbody tr').should('have.length', 1);
            cy.contains('Keep this').should('be.visible');
            cy.contains('Delete this').should('not.exist');
        });
    });

    describe('Edit Invoice', () => {
        it('should edit invoice due date', () => {
            cy.visit('/invoices');
            cy.get('table').should('exist');
            cy.wait(500);

            // Click first invoice
            cy.get('table tbody tr').first().click();

            // Set due date to 7 days from now
            const dueDate = new Date();
            dueDate.setDate(dueDate.getDate() + 7);
            const dateStr = dueDate.toISOString().split('T')[0];

            cy.get('input[name="dueDate"]').clear().type(dateStr, { force: true });

            cy.contains('button', 'Save').click();
            cy.contains('Invoice updated successfully').should('be.visible');

            // Verify persistence
            cy.reload();
            cy.get('input[name="dueDate"]').should('have.value', dateStr);
        });

        it('should edit invoice notes', () => {
            cy.visit('/invoices');
            cy.get('table tbody tr').first().click();

            const newNotes = `Invoice notes updated ${Date.now()}`;
            cy.get('textarea[name="notes"]').clear().type(newNotes);

            cy.contains('button', 'Save').click();
            cy.contains('Invoice updated successfully').should('be.visible');

            cy.reload();
            cy.get('textarea[name="notes"]').should('have.value', newNotes);
        });
    });

    describe('Invoice Actions', () => {
        it('should download invoice PDF', () => {
            cy.visit('/invoices');
            cy.get('table tbody tr').first().click();

            cy.contains('button', 'Download PDF').click();

            // Verify no error
            cy.contains('Failed to download').should('not.exist');
        });

        it('should copy public link', () => {
            cy.visit('/invoices');
            cy.get('table tbody tr').first().click();

            cy.contains('button', 'Copy Public Link').click();
            cy.contains('Generate & Copy Link').click();

            cy.contains('Public link copied').should('be.visible');
        });

        it('should send invoice to customer', () => {
            cy.visit('/invoices');
            cy.get('table tbody tr').first().click();

            // Check if Send button exists (depends on status)
            cy.get('body').then(($body) => {
                if ($body.find('button:contains("Send to Customer")').length > 0) {
                    cy.contains('button', 'Send to Customer').click();
                    // Should show success or handle appropriately
                    cy.contains('sent').should('be.visible');
                }
            });
        });
    });

    describe('Invoice Status Changes', () => {
        it('should cancel an invoice', () => {
            // Create a fresh invoice to cancel
            cy.visit('/invoices/new');

            cy.get('input[placeholder*="Search client"]').type('Test');
            cy.wait(1000);
            cy.get('[role="option"]').first().click({ force: true });

            cy.contains('button', 'Add Item').click();
            cy.get('input[placeholder="Description"]').first().type('To be cancelled');
            cy.get('table tbody tr').first().find('input[type="number"]').eq(1).clear().type('100');

            cy.contains('button', 'Save').click();
            cy.contains('Invoice created successfully').should('be.visible');

            // Cancel it
            cy.get('body').then(($body) => {
                if ($body.find('button:contains("Cancel Invoice")').length > 0) {
                    cy.contains('button', 'Cancel Invoice').click();
                    cy.contains('Yes, Cancel it').click();
                    cy.contains('cancelled').should('be.visible');
                }
            });
        });
    });

    describe('GST Calculation', () => {
        it('should calculate GST correctly', () => {
            cy.visit('/invoices/new');

            cy.get('input[placeholder*="Search client"]').type('Test');
            cy.wait(1000);
            cy.get('[role="option"]').first().click({ force: true });

            // Add item: qty=2, price=$50, GST=10%
            cy.contains('button', 'Add Item').click();
            cy.get('input[placeholder="Description"]').first().type('GST test');
            cy.get('table tbody tr').first().find('input[type="number"]').eq(0).clear().type('2');
            cy.get('table tbody tr').first().find('input[type="number"]').eq(1).clear().type('50');

            // Subtotal = $100, GST = $10, Total = $110
            cy.contains('GST').parent().should('contain', '$10.00');
            cy.get('.font-bold').contains('Total').parent().should('contain', '$110.00');
        });
    });

    describe('Discounts', () => {
        it('should apply dollar discount', () => {
            cy.visit('/invoices/new');

            cy.get('input[placeholder*="Search client"]').type('Test');
            cy.wait(1000);
            cy.get('[role="option"]').first().click({ force: true });

            cy.contains('button', 'Add Item').click();
            cy.get('input[placeholder="Description"]').first().type('Discount test');
            cy.get('table tbody tr').first().find('input[type="number"]').eq(0).clear().type('1');
            cy.get('table tbody tr').first().find('input[type="number"]').eq(1).clear().type('100');

            // $100 + 10% GST = $110, minus $20 discount = $90
            cy.get('input[name="discountValue"]').clear().type('20');

            cy.get('.font-bold').contains('Total').parent().should('contain', '$90.00');
        });

        it('should apply percentage discount', () => {
            cy.visit('/invoices/new');

            cy.get('input[placeholder*="Search client"]').type('Test');
            cy.wait(1000);
            cy.get('[role="option"]').first().click({ force: true });

            cy.contains('button', 'Add Item').click();
            cy.get('input[placeholder="Description"]').first().type('Percent discount test');
            cy.get('table tbody tr').first().find('input[type="number"]').eq(0).clear().type('1');
            cy.get('table tbody tr').first().find('input[type="number"]').eq(1).clear().type('200');

            // Change to percentage discount
            cy.get('button[role="combobox"]').last().click();
            cy.get('[role="option"]').contains('Discount (%)').click();

            // Apply 15% discount: $220 - 15% = $187
            cy.get('input[name="discountValue"]').clear().type('15');

            cy.get('.font-bold').contains('Total').parent().should('contain', '$187.00');
        });
    });

    describe('Navigation', () => {
        it('should navigate to invoices list', () => {
            cy.contains('a', 'Invoices').click();
            cy.url().should('include', '/invoices');
            cy.contains('button', 'New Invoice').should('exist');
        });

        it('should have New Invoice button on list page', () => {
            cy.visit('/invoices');
            cy.contains('button', 'New Invoice').should('be.visible');
            cy.contains('button', 'New Invoice').click();
            cy.url().should('include', '/invoices/new');
        });
    });
});
