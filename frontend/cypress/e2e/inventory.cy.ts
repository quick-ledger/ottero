describe('Inventory Workflows', () => {
    beforeEach(() => {
        cy.loginToAuth0();
        cy.visit('/dashboard');
        cy.get('nav').find('.bg-secondary\\/50').should('be.visible');
    });

    describe('Inventory Dashboard', () => {
        it('should display inventory dashboard', () => {
            // Navigate to inventory dashboard via Inventory menu
            cy.contains('button', 'Inventory').click();
            cy.wait(500);
            cy.contains('a', 'Inventory Dashboard').click();
            cy.url().should('include', '/inventory');

            // Should show dashboard stats
            cy.contains('Total Products').should('be.visible');
        });

        it('should show low stock alerts', () => {
            cy.visit('/inventory');

            // Should have low stock section
            cy.contains('Low Stock').should('be.visible');
        });

        it('should show inventory value', () => {
            cy.visit('/inventory');

            // Should display inventory value
            cy.contains('Inventory Value').should('be.visible');
        });

        it('should have stock movements link', () => {
            cy.visit('/inventory');

            // Dashboard should have View Stock Movements link
            cy.contains('View Stock Movements').should('be.visible');
        });
    });

    describe('Stock Movements', () => {
        it('should display stock movements page', () => {
            cy.visit('/inventory/movements');

            cy.contains('Stock Movements').should('be.visible');
            cy.get('table').should('exist');
        });

        it('should show movement type column', () => {
            cy.visit('/inventory/movements');

            cy.get('table thead').should('contain', 'Type');
        });

        it('should show quantity change column', () => {
            cy.visit('/inventory/movements');

            cy.get('table thead').should('contain', 'Change');
        });

        it('should show reference column', () => {
            cy.visit('/inventory/movements');

            cy.get('table thead').should('contain', 'Reference');
        });

        it('should not have add button (read-only audit trail)', () => {
            cy.visit('/inventory/movements');

            // Stock movements is an audit trail - no add button
            cy.contains('button', 'Add').should('not.exist');
        });
    });

    describe('Low Stock Alerts', () => {
        it('should show out of stock count', () => {
            cy.visit('/inventory');

            cy.contains('Out of Stock').should('be.visible');
        });
    });
});
