
describe('Landing Page', () => {
    beforeEach(() => {
        cy.visit('/');
    });

    it('should verify ABN ACN is present in the footer', () => {
        // Check for "ACN 681823030" as seen in the code
        cy.contains('ACN 681823030').should('be.visible');
        cy.contains('AI Soft Labs Pty Ltd').should('be.visible');
    });

    it('should verify the Guide page link works and page loads', () => {
        // Click the "Learn More" button which navigates to /guide
        cy.contains('Learn More').click();

        // Verify URL
        cy.url().should('include', '/guide');

        // Verify content on the guide page
        cy.get('h1').should('contain', 'Ottero Guide');
        cy.contains('Understanding the lifecycle of Quotes and Invoices').should('be.visible');
    });

    it('should verify all footer links work', () => {
        // Guides Link
        cy.get('footer a[href="/guide"]').should('have.attr', 'href', '/guide');
        // Terms Link
        cy.get('footer a[href="/terms"]').should('have.attr', 'href', '/terms');
        // Privacy Link
        cy.get('footer a[href="/privacy"]').should('have.attr', 'href', '/privacy');
        // Contact Link
        cy.get('footer a[href="/contact"]').should('have.attr', 'href', '/contact');

        // We can click them to verify they load (except maybe external ones if any, but these look internal)

        // Test Terms
        cy.get('footer a[href="/terms"]').click();
        cy.url().should('include', '/terms');
        cy.go('back');

        // Test Privacy
        cy.get('footer a[href="/privacy"]').click();
        cy.url().should('include', '/privacy');
        cy.go('back');

        // Test Contact
        cy.get('footer a[href="/contact"]').click();
        cy.url().should('include', '/contact');
    });

    it('should display all pricing plans', () => {
        // Check for "Free" plan
        cy.contains('h3', 'Free').should('be.visible');

        // Check for "Basic" plan
        cy.contains('h3', 'Basic').should('be.visible');

        // Check for "Advanced" plan
        cy.contains('h3', 'Advanced').should('be.visible');

        // Verify features for Basic plan (example)
        cy.contains('For growing small businesses').should('be.visible');
        cy.contains('Start 1 Month Free Trial').should('be.visible');
    });
});
