
/// <reference types="cypress" />

declare namespace Cypress {
    interface Chainable {
        loginToAuth0(): Chainable<void>;
    }
}

Cypress.Commands.add('loginToAuth0', () => {
    const username = Cypress.env('test_username');
    const password = Cypress.env('test_password');

    // Programmatic login via Auth0 API would be better, but without client_secret
    // we have to use the UI.
    // 
    // However, Auth0 Universal Login Page is a different domain.
    // We need to use cy.origin() to interact with it.

    cy.session(
        `auth0-${username}`,
        () => {
            cy.visit('/');
            // Click the login button (it says "Get Started" or "Go to Dashboard" if already logged in)
            // We expect to not be logged in initially for the session creation
            cy.wait(1000); // Ensure hydration
            cy.get('button').contains(/Get Started|Login/).click({ force: true });

            // This will redirect to 'ottero.au.auth0.com'
            cy.origin('https://ottero.au.auth0.com', { args: { username, password } }, ({ username, password }) => {
                // Wait for the login form load
                cy.wait(2000);
                cy.get('input[name="username"]').should('be.visible').type(username);
                cy.get('input[name="password"]').should('be.visible').type(password, { log: false });
                cy.get('button[type="submit"][name="action"]').click();
            });

            // After login, we should be redirected back to the dashboard
            cy.url().should('include', '/dashboard');
            cy.contains('h1', 'Dashboard').should('be.visible'); // Assuming there's an h1 Dashboard
        },
        {
            validate: () => {
                // Validate the session is still active by visiting dashboard
                cy.visit('/dashboard');
                // If we are redirected to home or login, session is invalid
                cy.url().should('include', '/dashboard');
            },
            cacheAcrossSpecs: true
        }
    );
});
