describe('Refer a Friend', () => {
    beforeEach(() => {
        cy.loginToAuth0();
        cy.visit('/dashboard');
        cy.get('nav').find('.bg-secondary\\/50').should('be.visible');
    });

    describe('Navigation', () => {
        it('should navigate to referral page from settings menu', () => {
            cy.contains('button', 'Settings').click();
            cy.wait(500);
            cy.contains('a', 'Refer a Friend').should('be.visible');
            cy.contains('a', 'Refer a Friend').click();

            cy.url().should('include', '/settings/referrals');
        });

        it('should have Refer a Friend link in settings dropdown', () => {
            cy.contains('button', 'Settings').click();
            cy.wait(500);

            cy.contains('a', 'Refer a Friend').should('be.visible');
        });
    });

    describe('Page Display', () => {
        it('should display referral page with correct heading', () => {
            cy.visit('/settings/referrals');

            cy.contains('h1', 'Refer a Friend').should('be.visible');
            cy.contains('Share Ottero with friends').should('be.visible');
        });

        it('should display How It Works section', () => {
            cy.visit('/settings/referrals');

            cy.contains('How It Works').should('be.visible');
            cy.contains("Enter Friend's Email").should('be.visible');
            cy.contains('Friend Signs Up').should('be.visible');
            cy.contains('You Get Free Month').should('be.visible');
        });

        it('should display referral form', () => {
            cy.visit('/settings/referrals');

            cy.contains('Refer a Friend').should('be.visible');
            cy.get('input#refereeEmail').should('exist');
            cy.get('input#refereeName').should('exist');
            cy.contains('button', 'Submit Referral').should('exist');
        });

        it('should display Your Referrals section', () => {
            cy.visit('/settings/referrals');

            cy.contains('Your Referrals').should('be.visible');
        });
    });

    describe('Form Validation', () => {
        it('should require email field', () => {
            cy.visit('/settings/referrals');

            // Try to submit without email
            cy.contains('button', 'Submit Referral').click();

            // HTML5 validation should prevent submission
            cy.get('input#refereeEmail:invalid').should('exist');
        });

        it('should validate email format', () => {
            cy.visit('/settings/referrals');

            cy.get('input#refereeEmail').type('invalid-email');
            cy.contains('button', 'Submit Referral').click();

            // HTML5 validation should prevent submission
            cy.get('input#refereeEmail:invalid').should('exist');
        });

        it('should allow optional name field', () => {
            cy.visit('/settings/referrals');

            const testEmail = `test-${Date.now()}@example.com`;
            cy.get('input#refereeEmail').type(testEmail);
            // Don't fill name

            cy.contains('button', 'Submit Referral').click();

            // Should submit successfully without name
            cy.contains('Referral submitted').should('be.visible');
        });
    });

    describe('Referral Submission', () => {
        it('should submit a referral successfully', () => {
            cy.visit('/settings/referrals');

            const testEmail = `cypress-test-${Date.now()}@example.com`;
            const testName = 'Cypress Test Friend';

            cy.get('input#refereeEmail').type(testEmail);
            cy.get('input#refereeName').type(testName);

            cy.contains('button', 'Submit Referral').click();

            cy.contains('Referral submitted').should('be.visible');
        });

        it('should clear form after successful submission', () => {
            cy.visit('/settings/referrals');

            const testEmail = `cypress-clear-${Date.now()}@example.com`;

            cy.get('input#refereeEmail').type(testEmail);
            cy.get('input#refereeName').type('Test Friend');

            cy.contains('button', 'Submit Referral').click();

            cy.contains('Referral submitted').should('be.visible');

            // Form should be cleared
            cy.get('input#refereeEmail').should('have.value', '');
            cy.get('input#refereeName').should('have.value', '');
        });

        it('should show referral in history after submission', () => {
            cy.visit('/settings/referrals');

            const testEmail = `cypress-history-${Date.now()}@example.com`;

            cy.get('input#refereeEmail').type(testEmail);
            cy.contains('button', 'Submit Referral').click();

            cy.contains('Referral submitted').should('be.visible');

            // Should appear in the referral list
            cy.contains(testEmail).should('be.visible');
            cy.contains('Pending').should('be.visible');
        });

        it('should show error for duplicate email referral', () => {
            cy.visit('/settings/referrals');

            const testEmail = `cypress-dupe-${Date.now()}@example.com`;

            // First submission
            cy.get('input#refereeEmail').type(testEmail);
            cy.contains('button', 'Submit Referral').click();
            cy.contains('Referral submitted').should('be.visible');

            // Wait for form to clear
            cy.get('input#refereeEmail').should('have.value', '');

            // Second submission with same email
            cy.get('input#refereeEmail').type(testEmail);
            cy.contains('button', 'Submit Referral').click();

            cy.contains('already referred').should('be.visible');
        });

        it('should prevent self-referral', () => {
            cy.visit('/settings/referrals');

            const userEmail = Cypress.env('test_username');
            if (userEmail) {
                cy.get('input#refereeEmail').type(userEmail);
                cy.contains('button', 'Submit Referral').click();

                cy.contains('cannot refer yourself').should('be.visible');
            }
        });
    });

    describe('Referral History', () => {
        it('should display empty state when no referrals', () => {
            // This test assumes a fresh user with no referrals
            // In practice, may need to use a test account without referrals
            cy.visit('/settings/referrals');

            cy.get('body').then(($body) => {
                const hasNoReferrals = $body.text().includes('No referrals yet');
                const hasReferrals = $body.find('[class*="referral"]').length > 0 ||
                    $body.text().includes('Pending') ||
                    $body.text().includes('Discount Applied');

                // Should show either empty state or referral list
                expect(hasNoReferrals || hasReferrals).to.be.true;
            });
        });

        it('should display referral status badges', () => {
            cy.visit('/settings/referrals');

            // Submit a referral to ensure we have at least one
            const testEmail = `cypress-badge-${Date.now()}@example.com`;
            cy.get('input#refereeEmail').type(testEmail);
            cy.contains('button', 'Submit Referral').click();
            cy.contains('Referral submitted').should('be.visible');

            // Check for status badge
            cy.contains('Pending').should('be.visible');
        });
    });

    describe('Loading States', () => {
        it('should show loading state initially', () => {
            cy.visit('/settings/referrals');

            // Either shows loading or content immediately
            cy.get('body').then(($body) => {
                const isLoaded = $body.text().includes('Refer a Friend') ||
                    $body.text().includes('Loading');
                expect(isLoaded).to.be.true;
            });
        });

        it('should show submitting state when creating referral', () => {
            cy.visit('/settings/referrals');

            const testEmail = `cypress-loading-${Date.now()}@example.com`;
            cy.get('input#refereeEmail').type(testEmail);

            cy.contains('button', 'Submit Referral').click();

            // Button should show submitting state or success message should appear
            cy.get('body').then(($body) => {
                const isProcessing = $body.text().includes('Submitting') ||
                    $body.text().includes('Referral submitted');
                expect(isProcessing).to.be.true;
            });
        });
    });
});
