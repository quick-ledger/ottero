#!/bin/bash

# Seed Test Database for Cypress E2E Tests
# Usage: ./seed-test-db.sh [options]
#
# Options:
#   --local     Use local MySQL (default)
#   --docker    Use Docker MySQL
#   --help      Show this help

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SQL_FILE="$SCRIPT_DIR/../../backend/src/main/resources/db/seed-test-data.sql"

# Default database connection (from application-dev.properties)
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-quickledger}"
DB_USER="${DB_USER:-QuickLedger}"
DB_PASS="${DB_PASS:-Password123!}"

show_help() {
    echo "Seed Test Database for Cypress E2E Tests"
    echo ""
    echo "Usage: ./seed-test-db.sh [options]"
    echo ""
    echo "Options:"
    echo "  --local     Use local MySQL (default)"
    echo "  --docker    Use Docker MySQL container named 'ottero-mysql'"
    echo "  --help      Show this help"
    echo ""
    echo "Environment variables:"
    echo "  DB_HOST     Database host (default: localhost)"
    echo "  DB_PORT     Database port (default: 3306)"
    echo "  DB_NAME     Database name (default: quickledger)"
    echo "  DB_USER     Database user (default: QuickLedger)"
    echo "  DB_PASS     Database password (default: Password123!)"
}

run_local() {
    echo "Running seed script against local MySQL..."
    echo "  Host: $DB_HOST:$DB_PORT"
    echo "  Database: $DB_NAME"
    echo "  User: $DB_USER"
    echo ""

    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$SQL_FILE"
}

run_docker() {
    CONTAINER_NAME="${DOCKER_CONTAINER:-ottero-mysql}"
    echo "Running seed script in Docker container: $CONTAINER_NAME"

    docker exec -i "$CONTAINER_NAME" mysql -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$SQL_FILE"
}

# Parse arguments
MODE="local"
for arg in "$@"; do
    case $arg in
        --local)
            MODE="local"
            ;;
        --docker)
            MODE="docker"
            ;;
        --help|-h)
            show_help
            exit 0
            ;;
        *)
            echo "Unknown option: $arg"
            show_help
            exit 1
            ;;
    esac
done

# Check SQL file exists
if [ ! -f "$SQL_FILE" ]; then
    echo "Error: SQL file not found at $SQL_FILE"
    exit 1
fi

echo "============================================"
echo "Seeding Test Database"
echo "============================================"

if [ "$MODE" = "docker" ]; then
    run_docker
else
    run_local
fi

echo ""
echo "============================================"
echo "Done! Test data has been seeded."
echo ""
echo "Test user: cypress_test@gmail.com"
echo "Test company ID: 999"
echo "============================================"
