#!/bin/bash

# Ottero E2E Test Runner
# Usage:
#   ./run-tests.sh              # Run all tests against local dev
#   ./run-tests.sh --prod       # Run all tests against production
#   ./run-tests.sh quotes       # Run quote tests only (local dev)
#   ./run-tests.sh quotes --prod # Run quote tests against production
#   ./run-tests.sh --open       # Open Cypress UI (interactive mode)
#   ./run-tests.sh quotes --open --prod # Open Cypress UI for quotes against prod
#
# Test suites:
#   quotes, invoices, customers, settings, smoke, landing, signup, all
#
# Environment flags:
#   --prod, -p    Run against production (https://dashboard.ottero.com.au)
#   --dev, -d     Run against local dev (http://localhost:5173) [default]
#
# Mode flags:
#   --open, -o    Open Cypress UI instead of headless run

set -e

cd "$(dirname "$0")/frontend"

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo "Installing dependencies..."
    npm install
fi

# Environment URLs
DEV_URL="http://localhost:5173"
PROD_URL="https://ottero.com.au"

# Defaults
SPEC=""
MODE="run"
ENV="dev"
BASE_URL="$DEV_URL"

# Parse arguments
for arg in "$@"; do
    case $arg in
        --open|-o)
            MODE="open"
            ;;
        --prod|-p)
            ENV="prod"
            BASE_URL="$PROD_URL"
            ;;
        --dev|-d)
            ENV="dev"
            BASE_URL="$DEV_URL"
            ;;
        quotes)
            SPEC="cypress/e2e/quotes.cy.ts"
            ;;
        invoices)
            SPEC="cypress/e2e/invoices.cy.ts"
            ;;
        customers)
            SPEC="cypress/e2e/customers.cy.ts"
            ;;
        settings)
            SPEC="cypress/e2e/settings.cy.ts"
            ;;
        smoke)
            SPEC="cypress/e2e/smoke.cy.ts"
            ;;
        landing)
            SPEC="cypress/e2e/landing_page.cy.ts"
            ;;
        signup)
            SPEC="cypress/e2e/signup_onboarding.cy.ts"
            ;;
        all)
            SPEC=""
            ;;
        *)
            # Check if it's a file path
            if [ -f "cypress/e2e/$arg.cy.ts" ]; then
                SPEC="cypress/e2e/$arg.cy.ts"
            elif [ -f "$arg" ]; then
                SPEC="$arg"
            fi
            ;;
    esac
done

# Export for Cypress
export CYPRESS_BASE_URL="$BASE_URL"

echo "============================================"
echo "Ottero E2E Tests"
echo "============================================"
echo "Environment: $ENV ($BASE_URL)"
echo "Mode: $MODE"
if [ -n "$SPEC" ]; then
    echo "Suite: $SPEC"
else
    echo "Suite: all tests"
fi
echo "============================================"
echo ""

# Build command
if [ "$MODE" = "open" ]; then
    if [ -n "$SPEC" ]; then
        npx cypress open --e2e --spec "$SPEC"
    else
        npx cypress open --e2e
    fi
else
    if [ -n "$SPEC" ]; then
        npx cypress run --spec "$SPEC"
    else
        npx cypress run
    fi
fi
