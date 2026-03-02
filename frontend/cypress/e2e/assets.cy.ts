describe('Asset Workflows', () => {
    beforeEach(() => {
        cy.loginToAuth0();
        cy.visit('/dashboard');
        cy.get('nav').find('.bg-secondary\\/50').should('be.visible');
    });

    describe('List Assets', () => {
        it('should display asset list', () => {
            cy.visit('/assets');

            cy.get('table').should('exist');
            cy.get('table thead').should('contain', 'Name');
            cy.get('table thead').should('contain', 'Code');
        });

        it('should show asset description text', () => {
            cy.visit('/assets');

            // Verify the description explaining assets vs products
            cy.contains('Company-owned equipment').should('be.visible');
            cy.contains("don't appear on invoices").should('be.visible');
        });

        it('should navigate to new asset page', () => {
            cy.visit('/assets');

            // Click Add Asset (it's a link inside a button)
            cy.contains('Add Asset').click({ force: true });
            cy.url().should('include', '/assets/new');

            // Verify form elements exist
            cy.get('input[name="name"]').should('exist');
            cy.get('input[name="code"]').should('exist');
        });
    });

    describe('Asset Form', () => {
        it('should load new asset form with correct fields', () => {
            cy.visit('/assets/new');

            // Verify all expected fields are present
            cy.get('input[name="name"]').should('exist');
            cy.get('input[name="code"]').should('exist');
            cy.get('input[name="serialNumber"]').should('exist');
            cy.get('input[name="location"]').should('exist');
            cy.get('input[name="purchasePrice"]').should('exist');
            cy.get('input[name="currentValue"]').should('exist');
            cy.contains('button', 'Save Asset').should('exist');
        });
    });
});
