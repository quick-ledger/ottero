describe('Supplier Workflows', () => {
    beforeEach(() => {
        cy.loginToAuth0();
        cy.visit('/dashboard');
        cy.get('nav').find('.bg-secondary\\/50').should('be.visible');
    });

    describe('Create Supplier', () => {
        it('should create a new supplier', () => {
            const timestamp = Date.now();
            const supplierName = `Test Supplier ${timestamp}`;

            // Navigate directly to suppliers page
            cy.visit('/suppliers');
            cy.url().should('include', '/suppliers');

            // Click Add Supplier (it's a link inside a button)
            cy.contains('Add Supplier').click({ force: true });
            cy.url().should('include', '/suppliers/new');

            // Fill form
            cy.get('input[name="name"]').type(supplierName);
            cy.get('input[name="contactName"]').type('John Contact');
            cy.get('input[name="email"]').type(`supplier${timestamp}@example.com`);
            cy.get('input[name="phone"]').type('0400000001');

            // Submit
            cy.contains('button', 'Save Supplier').click({ force: true });

            // Verify success
            cy.contains('Supplier created').should('be.visible');
        });

        it('should create supplier with all fields', () => {
            cy.visit('/suppliers/new');

            const fullTimestamp = Date.now();
            cy.get('input[name="name"]').type(`Full Supplier ${fullTimestamp}`);
            cy.get('input[name="contactName"]').type('Jane Doe');
            cy.get('input[name="email"]').type(`full${fullTimestamp}@supplier.com`);
            cy.get('input[name="phone"]').type('0412345678');

            // Fill optional fields
            cy.get('textarea[name="address"]').type('456 Supplier St, Melbourne VIC 3000');
            cy.get('textarea[name="notes"]').type('Important supplier notes');
            cy.get('input[name="paymentTerms"]').type('Net 30');

            cy.contains('button', 'Save Supplier').click({ force: true });
            cy.contains('Supplier created').should('be.visible');
        });
    });

    describe('List Suppliers', () => {
        it('should display supplier list', () => {
            cy.visit('/suppliers');

            cy.get('table').should('exist');
            cy.get('table thead').should('contain', 'Name');
        });

        it('should navigate to supplier edit on click', () => {
            // First create a supplier to ensure there's data
            cy.visit('/suppliers/new');
            const timestamp = Date.now();
            cy.get('input[name="name"]').type(`Nav Test ${timestamp}`);
            cy.get('input[name="email"]').type(`nav${timestamp}@supplier.com`);
            cy.contains('button', 'Save Supplier').click({ force: true });
            cy.contains('Supplier created').should('be.visible');

            cy.visit('/suppliers');
            cy.wait(500);

            cy.get('table tbody tr').first().find('a').first().click();
            cy.url().should('match', /\/suppliers\/\d+/);
        });
    });

    describe('Edit Supplier', () => {
        it('should edit supplier contact name', () => {
            // First create a supplier to edit
            cy.visit('/suppliers/new');
            const timestamp = Date.now();
            cy.get('input[name="name"]').type(`Edit Test ${timestamp}`);
            cy.get('input[name="email"]').type(`edit${timestamp}@supplier.com`);
            cy.contains('button', 'Save Supplier').click({ force: true });
            cy.contains('Supplier created').should('be.visible');

            // Now edit it
            cy.visit('/suppliers');
            cy.wait(500);
            cy.get('table tbody tr').first().find('a').first().click();

            // Update contact name
            const newContactName = `Updated Contact ${Date.now()}`;
            cy.get('input[name="contactName"]').clear().type(newContactName);

            // Save
            cy.contains('button', 'Save Supplier').click({ force: true });
            cy.contains('Supplier updated').should('be.visible');

            // Verify persistence
            cy.visit('/suppliers');
            cy.wait(500);
            cy.get('table tbody tr').first().find('a').first().click();
            cy.get('input[name="contactName"]').should('have.value', newContactName);
        });

        it('should edit supplier email', () => {
            // First create a supplier
            cy.visit('/suppliers/new');
            const timestamp = Date.now();
            cy.get('input[name="name"]').type(`Email Edit ${timestamp}`);
            cy.get('input[name="email"]').type(`emailedit${timestamp}@supplier.com`);
            cy.contains('button', 'Save Supplier').click({ force: true });
            cy.contains('Supplier created').should('be.visible');

            cy.visit('/suppliers');
            cy.wait(500);
            cy.get('table tbody tr').first().find('a').first().click();

            const newEmail = `updated${Date.now()}@supplier.com`;
            cy.get('input[name="email"]').clear().type(newEmail);

            cy.contains('button', 'Save Supplier').click({ force: true });
            cy.contains('Supplier updated').should('be.visible');

            cy.visit('/suppliers');
            cy.wait(500);
            cy.get('table tbody tr').first().find('a').first().click();
            cy.get('input[name="email"]').should('have.value', newEmail);
        });
    });

    describe('Delete Supplier', () => {
        it('should delete a supplier', () => {
            // First create a supplier to delete
            cy.visit('/suppliers/new');

            const timestamp = Date.now();
            const deleteName = `ToDelete Supplier ${timestamp}`;
            cy.get('input[name="name"]').type(deleteName);
            cy.get('input[name="email"]').type(`delete${timestamp}@supplier.com`);

            cy.contains('button', 'Save Supplier').click({ force: true });
            cy.contains('Supplier created').should('be.visible');

            // Now go back to list and delete it
            cy.visit('/suppliers');
            cy.wait(500);

            // Find the delete button for the supplier we just created
            cy.contains('tr', deleteName).find('button').filter(':has(svg.lucide-trash-2)').click();

            // Verify deletion toast
            cy.contains('Supplier deleted').should('be.visible');

            // Supplier is soft-deleted (marked as Inactive), verify status changed
            cy.contains('tr', deleteName).should('contain', 'Inactive');
        });
    });
});
