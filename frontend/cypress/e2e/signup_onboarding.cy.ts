
describe('Sign Up & Onboarding Flow', () => {
    beforeEach(() => {
        // Log in (simulating a new user session or existing one for test)
        cy.loginToAuth0();
        cy.visit('/dashboard');
    });

    it('should complete the new user onboarding flow', () => {
        // 1. Verify Plan Selection (Free Plan Step)
        cy.visit('/settings/pricing');
        cy.contains('Simple, Transparent Pricing').should('be.visible');

        // Find the Free plan card and verify interaction
        // Using "Free" title to locate the card
        cy.contains('Free').parent().parent().within(() => {
            // Verify description or features to ensure correct card
            cy.contains('Perfect for getting started').should('be.visible');
            // The button might be disabled (Current Plan) or enabled. 
            // We just verify it exists and is visible.
            cy.get('button').should('be.visible');
        });

        // 2. Go through company creation
        cy.visit('/companies/new');
        cy.contains('Create New Company').should('be.visible');

        const timestamp = Date.now();
        const companyName = `Startup ${timestamp}`;

        cy.get('input[name="name"]').type(companyName);
        cy.get('input[name="abn"]').type(`ABN${timestamp}`);
        cy.get('input[name="email"]').type(`contact@${timestamp}.com`);
        cy.get('input[name="phone"]').type('0400111222');
        cy.get('input[name="address"]').type('123 Innovation Blvd');

        // Save
        cy.contains('button', 'Create Company').click({ force: true });

        // Verify we are redirected to companies list
        cy.url().should('include', '/companies');
        cy.contains(companyName).should('be.visible');

        // Select the newly created company to proceed
        cy.contains('tr', companyName).contains('button', 'Select').click();

        // Verify Dashboard loads with selected company
        cy.url().should('include', '/dashboard');
        // Wait for navbar update
        cy.get('nav').should('contain', companyName);

        // 3. Test and add a test quote (interpreted as "Add a test record/quote")
        // We need a customer first
        cy.visit('/customers/new');
        cy.get('input[name="firstName"]').type('Onboarding');
        cy.get('input[name="lastName"]').type('User');
        cy.get('input[name="email"]').type(`user${timestamp}@test.com`);
        cy.get('input[name="phoneNumber"]').type('0400999888');
        cy.contains('button', 'Create Customer').click({ force: true });
        cy.contains('Customer created successfully').should('be.visible');

        // Create Quote
        cy.visit('/quotes/new');

        // Select Customer
        cy.contains('label', 'Search Existing Client').parent().find('button').click({ force: true });
        cy.get('[role="option"]').should('be.visible').contains('Onboarding User').click();

        // Add Item
        cy.contains('button', 'Add Item').click();
        cy.get('input[placeholder="Description"]').type('Onboarding Code Verification');
        cy.get('input[placeholder="0.00"]').first().clear().type('42.00'); // Test Code value?

        // Save
        cy.contains('button', 'Save').click();
        cy.contains('Quote created successfully').should('be.visible');

        // Verify Quote Number exists (The "code")
        cy.get('h1').should('contain', 'Quote #');
    });
});
