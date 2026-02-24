describe('Customer Workflows', () => {
    beforeEach(() => {
        cy.loginToAuth0();
        cy.visit('/dashboard');
        cy.get('nav').find('.bg-secondary\\/50').should('be.visible');
    });

    describe('Create Customer', () => {
        const timestamp = Date.now();
        const customerFirstName = 'Test';
        const customerLastName = `Customer${timestamp}`;
        const customerEmail = `test${timestamp}@example.com`;

        it('should create a new customer', () => {
            // Navigate to customers page
            cy.contains('button', 'Settings').click();
            cy.wait(500);
            cy.contains('a', 'Customers').click();
            cy.url().should('include', '/customers');

            // Click New Customer
            cy.contains('button', 'New Customer').click({ force: true });

            // Fill form
            cy.get('input[name="firstName"]').type(customerFirstName);
            cy.get('input[name="lastName"]').type(customerLastName);
            cy.get('input[name="email"]').type(customerEmail);
            cy.get('input[name="phoneNumber"]').type('0400000001');

            // Submit
            cy.contains('button', 'Create Customer').click({ force: true });

            // Verify success
            cy.contains('Customer created').should('be.visible');
            cy.contains(`${customerFirstName} ${customerLastName}`).should('be.visible');
        });

        it('should create customer with all fields', () => {
            cy.contains('button', 'Settings').click();
            cy.wait(500);
            cy.contains('a', 'Customers').click();

            cy.contains('button', 'New Customer').click({ force: true });

            const fullTimestamp = Date.now();
            cy.get('input[name="firstName"]').type('John');
            cy.get('input[name="lastName"]').type(`Doe${fullTimestamp}`);
            cy.get('input[name="email"]').type(`john${fullTimestamp}@example.com`);
            cy.get('input[name="phoneNumber"]').type('0412345678');

            // Fill optional fields if they exist
            cy.get('body').then(($body) => {
                if ($body.find('input[name="entityName"]').length > 0) {
                    cy.get('input[name="entityName"]').type('Doe Plumbing Pty Ltd');
                }
                if ($body.find('input[name="address"]').length > 0) {
                    cy.get('input[name="address"]').type('123 Main St, Sydney NSW 2000');
                }
            });

            cy.contains('button', 'Create Customer').click({ force: true });
            cy.contains('Customer created').should('be.visible');
        });
    });

    describe('Search Customer', () => {
        it('should search for existing customer', () => {
            cy.contains('button', 'Settings').click();
            cy.wait(500);
            cy.contains('a', 'Customers').click();

            // Search for "Test" - should find test customers
            cy.get('input[placeholder*="Search"]').type('Test');
            cy.wait(1000); // debounce

            // Should show results
            cy.get('table tbody tr').should('have.length.at.least', 1);
            cy.contains('Test').should('be.visible');
        });

        it('should show no results for non-existent customer', () => {
            cy.contains('button', 'Settings').click();
            cy.wait(500);
            cy.contains('a', 'Customers').click();

            cy.get('input[placeholder*="Search"]').type('ZZZZNONEXISTENT12345');
            cy.wait(1000);

            // Should show empty state or no rows
            cy.get('table tbody tr').should('have.length', 0);
        });
    });

    describe('Edit Customer', () => {
        it('should edit customer phone number', () => {
            cy.contains('button', 'Settings').click();
            cy.wait(500);
            cy.contains('a', 'Customers').click();

            // Click Edit on first customer
            cy.get('table tbody tr').first().find('a').contains('Edit').click();

            // Update phone
            const newPhone = `04${Date.now().toString().substring(5)}`;
            cy.get('input[name="phoneNumber"]').clear().type(newPhone);

            // Save
            cy.contains('button', 'Save').click({ force: true });
            cy.contains('Customer updated').should('be.visible');

            // Verify persistence
            cy.reload();
            cy.get('input[name="phoneNumber"]').should('have.value', newPhone);
        });

        it('should edit customer email', () => {
            cy.contains('button', 'Settings').click();
            cy.wait(500);
            cy.contains('a', 'Customers').click();

            cy.get('table tbody tr').first().find('a').contains('Edit').click();

            const newEmail = `updated${Date.now()}@example.com`;
            cy.get('input[name="email"]').clear().type(newEmail);

            cy.contains('button', 'Save').click({ force: true });
            cy.contains('Customer updated').should('be.visible');

            cy.reload();
            cy.get('input[name="email"]').should('have.value', newEmail);
        });
    });

    describe('Delete Customer', () => {
        it('should delete a customer', () => {
            // First create a customer to delete
            cy.contains('button', 'Settings').click();
            cy.wait(500);
            cy.contains('a', 'Customers').click();

            cy.contains('button', 'New Customer').click({ force: true });

            const timestamp = Date.now();
            const deleteName = `ToDelete${timestamp}`;
            cy.get('input[name="firstName"]').type('Delete');
            cy.get('input[name="lastName"]').type(deleteName);
            cy.get('input[name="email"]').type(`delete${timestamp}@example.com`);
            cy.get('input[name="phoneNumber"]').type('0400000000');

            cy.contains('button', 'Create Customer').click({ force: true });
            cy.contains('Customer created').should('be.visible');

            // Now find and delete it
            cy.contains('button', 'Settings').click();
            cy.wait(500);
            cy.contains('a', 'Customers').click();

            // Search for it
            cy.get('input[placeholder*="Search"]').type(deleteName);
            cy.wait(1000);

            // Click Edit
            cy.get('table tbody tr').first().find('a').contains('Edit').click();

            // Find Delete button
            cy.get('body').then(($body) => {
                if ($body.find('button:contains("Delete")').length > 0) {
                    cy.contains('button', 'Delete').click();

                    // Confirm
                    cy.get('body').then(($confirmBody) => {
                        if ($confirmBody.find('button:contains("Confirm")').length > 0) {
                            cy.contains('button', 'Confirm').click();
                        } else if ($confirmBody.find('[role="alertdialog"]').length > 0) {
                            cy.get('[role="alertdialog"]').find('button').last().click();
                        }
                    });

                    cy.contains('deleted').should('be.visible');
                }
            });
        });
    });

    describe('Customer List', () => {
        it('should display customer list', () => {
            cy.contains('button', 'Settings').click();
            cy.wait(500);
            cy.contains('a', 'Customers').click();

            cy.get('table').should('exist');
            cy.get('table thead').should('contain', 'Name');
            cy.get('table thead').should('contain', 'Email');
        });

        it('should navigate from dashboard quick action', () => {
            cy.contains('Add Customer').click();
            cy.url().should('include', '/customers');
        });
    });
});
