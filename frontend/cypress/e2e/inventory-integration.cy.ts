/**
 * Inventory Integration Tests
 *
 * These tests verify basic inventory integration features exist and are accessible.
 */
describe('Inventory Integration', () => {
    beforeEach(() => {
        cy.loginToAuth0();
        cy.visit('/dashboard');
        cy.get('nav').find('.bg-secondary\\/50').should('be.visible');
    });

    describe('Inventory Dashboard Access', () => {
        it('should access inventory dashboard from menu', () => {
            cy.contains('button', 'Inventory').click();
            cy.wait(500);
            cy.contains('a', 'Inventory Dashboard').click();
            cy.url().should('include', '/inventory');
        });

        it('should show dashboard with key metrics', () => {
            cy.visit('/inventory');
            cy.contains('Total Products').should('be.visible');
            cy.contains('Inventory Value').should('be.visible');
        });
    });

    describe('Stock Movements Access', () => {
        it('should access stock movements page', () => {
            cy.visit('/inventory/movements');
            cy.contains('Stock Movements').should('be.visible');
            cy.get('table').should('exist');
        });
    });

    describe('Product Inventory Tracking', () => {
        it('should have inventory tracking option in product form', () => {
            cy.visit('/products/new');

            // Check for inventory tracking toggle
            cy.contains('Track Inventory').should('be.visible');
            cy.get('button[role="switch"]').should('exist');
        });

        it('should show inventory fields when tracking is enabled', () => {
            cy.visit('/products/new');

            // Enable tracking
            cy.get('button[role="switch"]').click();

            // Inventory fields should appear
            cy.contains('Quantity on Hand').should('be.visible');
            cy.contains('Reorder Point').should('be.visible');
        });
    });

    describe('Purchase Order Integration', () => {
        it('should access purchase orders from inventory menu', () => {
            cy.contains('button', 'Inventory').click();
            cy.wait(500);
            cy.contains('a', 'Purchase Orders').click();
            cy.url().should('include', '/purchase-orders');
        });
    });

    describe('Supplier Integration', () => {
        it('should have suppliers page accessible', () => {
            cy.visit('/suppliers');
            cy.get('table').should('exist');
        });
    });
});
