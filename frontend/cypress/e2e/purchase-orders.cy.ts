describe('Purchase Order Workflows', () => {
    beforeEach(() => {
        cy.loginToAuth0();
        cy.visit('/dashboard');
        cy.get('nav').find('.bg-secondary\\/50').should('be.visible');
    });

    describe('List Purchase Orders', () => {
        it('should display PO list page', () => {
            // Navigate to purchase orders page via Inventory menu
            cy.contains('button', 'Inventory').click();
            cy.wait(500);
            cy.contains('a', 'Purchase Orders').click();
            cy.url().should('include', '/purchase-orders');
        });

        it('should show table with headers', () => {
            cy.visit('/purchase-orders');

            cy.get('table').should('exist');
            cy.get('table thead').should('contain', 'PO');
        });

        it('should have new PO link', () => {
            cy.visit('/purchase-orders');

            // Check for New Purchase Order link (Button with asChild renders as <a>)
            cy.contains('New Purchase Order').should('exist');
        });
    });

    describe('New Purchase Order', () => {
        it('should navigate to new PO form', () => {
            cy.visit('/purchase-orders');

            // Click New Purchase Order link
            cy.contains('New Purchase Order').click({ force: true });
            cy.url().should('include', '/purchase-orders/new');
        });
    });
});
