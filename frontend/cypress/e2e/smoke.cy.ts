
describe('Smoke Test', () => {
    it('should load the home page and check title', () => {
        cy.visit('/');
        cy.title().should('include', 'Ottero');
        cy.get('#root').should('exist');
    });
});
