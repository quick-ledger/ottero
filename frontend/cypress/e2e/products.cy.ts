describe('Product Workflows', () => {
    beforeEach(() => {
        cy.loginToAuth0();
        cy.visit('/dashboard');
        cy.get('nav').find('.bg-secondary\\/50').should('be.visible');
    });

    describe('Create Product', () => {
        it('should create a new product', () => {
            const timestamp = Date.now();
            const productName = `Test Product ${timestamp}`;

            // Navigate to products page via Inventory menu
            cy.contains('button', 'Inventory').click();
            cy.wait(500);
            cy.contains('a', 'Products').click();
            cy.url().should('include', '/products');

            // Click New Product
            cy.contains('button', 'New Product').click({ force: true });
            cy.url().should('include', '/products/new');

            // Fill form
            cy.get('input[name="name"]').type(productName);
            cy.get('input[name="price"]').clear().type('99.99');
            cy.get('input[name="description"]').type('A test product description');

            // Submit
            cy.contains('button', 'Create Product').click({ force: true });

            // Verify success
            cy.contains('Product created').should('be.visible');
        });

        it('should create product with inventory tracking', () => {
            cy.visit('/products/new');

            const fullTimestamp = Date.now();
            cy.get('input[name="name"]').type(`Tracked Product ${fullTimestamp}`);
            cy.get('input[name="price"]').clear().type('49.99');

            // Enable inventory tracking via Switch component
            cy.get('button[role="switch"]').click();

            // Fill inventory fields that appear after toggle
            cy.get('input[name="quantityOnHand"]').clear().type('100');
            cy.get('input[name="reorderPoint"]').clear().type('10');
            cy.get('input[name="reorderQuantity"]').clear().type('50');

            cy.contains('button', 'Create Product').click({ force: true });
            cy.contains('Product created').should('be.visible');
        });
    });

    describe('List Products', () => {
        it('should display product list', () => {
            cy.visit('/products');

            cy.get('table').should('exist');
            cy.get('table thead').should('contain', 'Name');
            cy.get('table thead').should('contain', 'Price');
        });

        it('should have search functionality', () => {
            cy.visit('/products');

            // Verify search input exists
            cy.get('input[placeholder*="Search"]').should('exist');
        });

        it('should navigate to new product form', () => {
            cy.visit('/products');

            cy.contains('button', 'New Product').click({ force: true });
            cy.url().should('include', '/products/new');
        });
    });
});
