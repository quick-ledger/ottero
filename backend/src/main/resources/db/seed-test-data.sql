-- Seed Test Data for Cypress E2E Tests
-- Run this script against your dev database before running tests
--
-- Test User: cypress_test@gmail.com
-- Password: Set in Auth0 (create this user in Auth0 first)

-- ============================================
-- 1. Create Test User (if not exists)
-- ============================================
INSERT INTO users (id, name, surname, email, external_id, subscription_plan, stripe_customer_id, age, gender, cancel_at_period_end, trial_reminder_sent, created_date, modified_date)
SELECT
    999,
    'Cypress',
    'Tester',
    'cypress_test@gmail.com',
    'auth0|699cbcdaa50d6c4edf6333e7',
    'Professional',  -- Professional plan for unlimited quotes
    'cus_test_cypress',
    0,               -- age is required (primitive int)
    'X',             -- gender is required (primitive char)
    0,               -- cancel_at_period_end (primitive bit)
    0,               -- trial_reminder_sent (primitive bit)
    NOW(),
    NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'cypress_test@gmail.com');

-- Update existing user to have Basic plan if they exist
UPDATE users
SET subscription_plan = 'Basic',
    stripe_customer_id = COALESCE(stripe_customer_id, 'cus_test_cypress'),
    name = COALESCE(name, 'Cypress'),
    surname = COALESCE(surname, 'Tester'),
    age = COALESCE(age, 0),
    modified_date = NOW()
WHERE email = 'cypress_test@gmail.com';

-- ============================================
-- 2. Create Test Company (if not exists)
-- ============================================
INSERT INTO companies (id, name, email, phone, abn, address, created_date, modified_date)
SELECT
    999,
    'Cypress Test Company',
    'cypress_test@gmail.com',
    '0400000000',
    '12345678901',
    '123 Test Street, Sydney NSW 2000',
    NOW(),
    NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM companies WHERE id = 999);

-- ============================================
-- 3. Link User to Company
-- ============================================
INSERT INTO user_companies (user_id, company_id, role)
SELECT
    (SELECT id FROM users WHERE email = 'cypress_test@gmail.com'),
    999,
    'OWNER'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM user_companies
    WHERE user_id = (SELECT id FROM users WHERE email = 'cypress_test@gmail.com')
    AND company_id = 999
);

-- Update default company for the user
UPDATE users
SET default_company_id = 999
WHERE email = 'cypress_test@gmail.com' AND default_company_id IS NULL;

-- ============================================
-- 4. Create Sequence Configs for the Company
-- ============================================
INSERT INTO sequence_config (company_id, entity_type, prefix, current_number, number_padding)
SELECT 999, 'QUOTE', 'QT-', 1, 4 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sequence_config WHERE company_id = 999 AND entity_type = 'QUOTE');

INSERT INTO sequence_config (company_id, entity_type, prefix, current_number, number_padding)
SELECT 999, 'INVOICE', 'INV-', 1, 4 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sequence_config WHERE company_id = 999 AND entity_type = 'INVOICE');

-- ============================================
-- 5. Create Sample Customers (clients table)
-- ============================================
INSERT INTO clients (id, company_id, contact_name, contact_surname, email, phone, created_date, modified_date)
SELECT 999, 999, 'Test', 'Customer', 'testcustomer@example.com', '0400111111', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM clients WHERE company_id = 999 AND email = 'testcustomer@example.com');

INSERT INTO clients (id, company_id, contact_name, contact_surname, email, phone, created_date, modified_date)
SELECT 998, 999, 'John', 'Smith', 'john.smith@example.com', '0400222222', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM clients WHERE company_id = 999 AND email = 'john.smith@example.com');

INSERT INTO clients (id, company_id, contact_name, contact_surname, email, phone, created_date, modified_date)
SELECT 997, 999, 'Jane', 'Doe', 'jane.doe@example.com', '0400333333', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM clients WHERE company_id = 999 AND email = 'jane.doe@example.com');

-- ============================================
-- 5. Reset monthly quote count (delete old test quotes if needed)
-- ============================================
-- Uncomment if you want to clear test quotes each run:
-- DELETE FROM quote_items WHERE quote_id IN (SELECT id FROM quotes WHERE company_id = 999);
-- DELETE FROM quotes WHERE company_id = 999;

-- ============================================
-- Verification Queries (run these to verify)
-- ============================================
-- SELECT * FROM users WHERE email = 'cypress_test@gmail.com';
-- SELECT * FROM companies WHERE id = 999;
-- SELECT * FROM user_companies WHERE company_id = 999;
-- SELECT * FROM clients WHERE company_id = 999;

SELECT 'Test data seeded successfully!' AS status;
