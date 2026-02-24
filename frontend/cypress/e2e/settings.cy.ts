describe('Settings Workflows', () => {
    beforeEach(() => {
        cy.loginToAuth0();
        cy.visit('/dashboard');
        cy.get('nav').find('.bg-secondary\\/50').should('be.visible');
    });

    describe('Company Details', () => {
        it('should view company details', () => {
            cy.contains('button', 'Settings').click();
            cy.wait(500);
            cy.contains('a', 'Company Details').click();

            cy.contains('h1', 'Company Details').should('exist');
            cy.get('input[name="name"]').should('exist');
        });

        it('should update company phone number', () => {
            cy.contains('button', 'Settings').click();
            cy.wait(500);
            cy.contains('a', 'Company Details').click();

            cy.contains('Loading company details...').should('not.exist');
            cy.wait(500);

            const newPhone = `04${Date.now().toString().substring(5)}`;
            cy.get('input[name="phone"]').clear({ force: true }).type(newPhone, { force: true });

            cy.contains('button', 'Save Changes').click({ force: true });
            cy.contains('Company updated successfully').should('be.visible');

            // Verify persistence
            cy.reload();
            cy.get('input[name="phone"]').should('have.value', newPhone);
        });

        it('should update company address', () => {
            cy.contains('button', 'Settings').click();
            cy.wait(500);
            cy.contains('a', 'Company Details').click();

            cy.contains('Loading company details...').should('not.exist');
            cy.wait(500);

            const newAddress = `${Date.now()} Test Street, Sydney NSW 2000`;
            cy.get('input[name="address"]').clear({ force: true }).type(newAddress, { force: true });

            cy.contains('button', 'Save Changes').click({ force: true });
            cy.contains('Company updated successfully').should('be.visible');
        });
    });

    describe('Number Sequences', () => {
        it('should view sequence configuration', () => {
            cy.contains('button', 'Settings').click();
            cy.wait(500);
            cy.contains('a', 'Number Sequences').click();

            cy.url().should('include', '/settings/sequences');
            cy.contains('Quote').should('be.visible');
            cy.contains('Invoice').should('be.visible');
        });

        it('should update quote sequence prefix', () => {
            cy.contains('button', 'Settings').click();
            cy.wait(500);
            cy.contains('a', 'Number Sequences').click();

            const newPrefix = `QT-${Date.now().toString().substring(8)}-`;

            // Quote sequence is typically the first one
            cy.get('input[name="prefix"]').eq(0).clear({ force: true }).type(newPrefix, { force: true });

            cy.contains('button', 'Save Quote Config').click({ force: true });
            cy.contains('Quote sequence updated').should('be.visible');
        });

        it('should update invoice sequence prefix', () => {
            cy.contains('button', 'Settings').click();
            cy.wait(500);
            cy.contains('a', 'Number Sequences').click();

            const newPrefix = `INV-${Date.now().toString().substring(8)}-`;

            // Invoice sequence is typically the second one
            cy.get('input[name="prefix"]').eq(1).clear({ force: true }).type(newPrefix, { force: true });

            cy.contains('button', 'Save Invoice Config').click({ force: true });
            cy.contains('Invoice sequence updated').should('be.visible');
        });

        it('should update starting number', () => {
            cy.contains('button', 'Settings').click();
            cy.wait(500);
            cy.contains('a', 'Number Sequences').click();

            const newNumber = Math.floor(Math.random() * 9000) + 1000; // Random 4-digit number

            cy.get('input[name="currentNumber"]').eq(0).clear({ force: true }).type(String(newNumber), { force: true });

            cy.contains('button', 'Save Quote Config').click({ force: true });
            cy.contains('Quote sequence updated').should('be.visible');
        });
    });

    describe('PDF Template', () => {
        it('should view PDF template configuration', () => {
            cy.contains('button', 'Settings').click();
            cy.wait(500);
            cy.contains('a', 'PDF Template').click();

            cy.url().should('include', '/settings/template-config');
        });

        it('should have template preview or configuration options', () => {
            cy.visit('/settings/template-config');

            // Check for common template config elements
            cy.get('body').then(($body) => {
                // Could have color pickers, logo upload, preview, etc.
                const hasConfig = $body.find('input').length > 0 ||
                    $body.find('button:contains("Save")').length > 0 ||
                    $body.find('[class*="preview"]').length > 0;

                expect(hasConfig).to.be.true;
            });
        });
    });

    describe('Profile & Account', () => {
        it('should view profile page', () => {
            cy.visit('/settings/profile');

            cy.contains('h1', 'Profile').should('be.visible');
            cy.contains('Personal Information').should('be.visible');
        });

        it('should display user email', () => {
            cy.visit('/settings/profile');

            const email = Cypress.env('test_username');
            if (email) {
                cy.contains(email).should('be.visible');
            }
        });

        it('should show subscription status', () => {
            cy.visit('/settings/profile');

            // Should show some subscription info
            cy.get('body').then(($body) => {
                const hasSubscriptionInfo = $body.text().includes('Free') ||
                    $body.text().includes('Basic') ||
                    $body.text().includes('Subscription') ||
                    $body.text().includes('Plan');

                expect(hasSubscriptionInfo).to.be.true;
            });
        });
    });

    describe('Pricing Page', () => {
        it('should view pricing page', () => {
            cy.visit('/settings/pricing');

            cy.contains('h1', 'Pricing').should('be.visible');
        });

        it('should display Free plan', () => {
            cy.visit('/settings/pricing');

            cy.contains('Free').should('be.visible');
            cy.contains('$0').should('be.visible');
        });

        it('should display Basic plan', () => {
            cy.visit('/settings/pricing');

            cy.contains('Basic').should('be.visible');
            cy.contains('$5').should('be.visible');
        });

        it('should have plan features listed', () => {
            cy.visit('/settings/pricing');

            // Check for feature mentions
            cy.contains('Quotes').should('be.visible');
            cy.contains('Invoices').should('be.visible');
        });
    });

    describe('Settings Navigation', () => {
        it('should navigate to all settings pages via menu', () => {
            // PDF Template
            cy.contains('button', 'Settings').click();
            cy.wait(500);
            cy.contains('a', 'PDF Template').should('be.visible');

            // Number Sequences
            cy.contains('a', 'Number Sequences').should('be.visible');

            // Company Details
            cy.contains('a', 'Company Details').should('be.visible');

            // Customers
            cy.contains('a', 'Customers').should('be.visible');
        });

        it('should have working links', () => {
            cy.contains('button', 'Settings').click();
            cy.wait(500);

            cy.contains('a', 'PDF Template').click();
            cy.url().should('include', '/settings/template-config');

            cy.contains('button', 'Settings').click();
            cy.wait(500);
            cy.contains('a', 'Number Sequences').click();
            cy.url().should('include', '/settings/sequences');

            cy.contains('button', 'Settings').click();
            cy.wait(500);
            cy.contains('a', 'Company Details').click();
            cy.url().should('include', '/companies/');
        });
    });

    describe('Dashboard', () => {
        it('should display dashboard correctly', () => {
            cy.visit('/dashboard');

            cy.contains('Dashboard').should('be.visible');
            cy.get('nav').should('exist');
            cy.contains('Quotes').should('exist');
            cy.contains('Invoices').should('exist');
            cy.contains('Customers').should('exist');
        });

        it('should show statistics widgets', () => {
            cy.visit('/dashboard');

            cy.contains('Total Invoices').should('be.visible');
            cy.contains('Total Quotes').should('be.visible');
            cy.contains('Total Customers').should('be.visible');
        });

        it('should have quick action buttons', () => {
            cy.visit('/dashboard');

            cy.contains('Create Quote').should('be.visible');
            cy.contains('Create Invoice').should('be.visible');
            cy.contains('Add Customer').should('be.visible');
        });

        it('should navigate from quick actions', () => {
            cy.visit('/dashboard');

            cy.contains('Create Quote').click();
            cy.url().should('include', '/quotes');

            cy.visit('/dashboard');
            cy.contains('Create Invoice').click();
            cy.url().should('include', '/invoices');
        });
    });
});
