describe('Service Workflows', () => {
    beforeEach(() => {
        cy.loginToAuth0();
        cy.visit('/dashboard');
        cy.get('nav').find('.bg-secondary\\/50').should('be.visible');
    });

    describe('Create Service', () => {
        it('should create a new service', () => {
            const timestamp = Date.now();
            const serviceName = `Test Service ${timestamp}`;

            // Navigate to services page via Settings menu
            cy.contains('button', 'Settings').click();
            cy.wait(500);
            cy.contains('a', 'Services').click();
            cy.url().should('include', '/services');

            // Click New Service
            cy.contains('button', 'New Service').click({ force: true });
            cy.url().should('include', '/services/new');

            // Fill form
            cy.get('input[name="name"]').type(serviceName);
            cy.get('input[name="price"]').clear().type('150.00');
            cy.get('input[name="description"]').type('A test service description');

            // Submit
            cy.contains('button', 'Create Service').click({ force: true });

            // Verify success
            cy.contains('Service created').should('be.visible');
        });

        it('should create service with description', () => {
            cy.visit('/services/new');

            const fullTimestamp = Date.now();
            cy.get('input[name="name"]').type(`Hourly Service ${fullTimestamp}`);
            cy.get('input[name="price"]').clear().type('85.00');
            cy.get('input[name="description"]').type('Hourly consulting rate');

            cy.contains('button', 'Create Service').click({ force: true });
            cy.contains('Service created').should('be.visible');
        });
    });

    describe('List Services', () => {
        it('should display service list', () => {
            cy.visit('/services');

            cy.get('table').should('exist');
            cy.get('table thead').should('contain', 'Name');
            cy.get('table thead').should('contain', 'Price');
        });

        it('should have search functionality', () => {
            cy.visit('/services');

            // Verify search input exists
            cy.get('input[placeholder*="Search"]').should('exist');
        });

        it('should navigate to new service form', () => {
            cy.visit('/services');

            cy.contains('button', 'New Service').click({ force: true });
            cy.url().should('include', '/services/new');
        });
    });
});
