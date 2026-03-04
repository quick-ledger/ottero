# Ottero Monorepo

> **IMPORTANT**: Never commit or push changes without user review. Always wait for explicit approval before running git commit, git push, or creating PRs.

## Architecture

Ottero is a full-stack invoicing application in a monorepo structure:

| Directory | Type | Tech Stack |
|-----------|------|------------|
| `frontend/` | Web App | React 18, TypeScript, Vite, Radix UI, Tailwind, TanStack Query |
| `backend/` | API | Java 21, Spring Boot 3, JPA/Hibernate, MapStruct |
| `infra/` | Infrastructure | Kubernetes, Docker, GitHub Actions |

## How They Connect

- Frontend calls backend via REST API at `/api/...` endpoints
- Authentication: Auth0 (OAuth2 with JWT tokens)
- Production URL: https://ottero.com.au

## Features

### Core Invoicing
- **Invoices**: Create, send, track payments (DRAFT → SENT → PAID/CANCELLED)
- **Quotes**: Create estimates, convert to invoices
- **Recurring Invoices**: Automatic invoice generation on schedule
- **Payment Reminders**: Automated reminders for overdue invoices
- **Stripe Payments**: Online payment integration

### Inventory Management
- **Products**: Inventory items with optional stock tracking
- **Services**: Service items for billing
- **Stock Tracking**: quantityOnHand, reorderPoint, automatic deduction on invoice send
- **Stock Movements**: Full audit trail (SALE, PURCHASE, ADJUSTMENT)
- **Purchase Orders**: Procurement workflow (DRAFT → SENT → PARTIALLY_RECEIVED → RECEIVED)
- **Suppliers**: Vendor management for purchase orders

### Job Management
- **Jobs**: Track work with scheduling, status, and client assignment
- **Job Notes**: Add notes to jobs
- **Job Attachments**: File attachments with camera capture support
- **Job Time Tracking**: Log time entries with duration, employee, billable flag, hourly rate

### Other Features
- **Customers/Clients**: Customer information management
- **Expenses**: Track business expenses with attachments
- **Assets**: Company-owned assets (separate from saleable products)
- **Dashboard**: Overview of business metrics
- **Multi-tenant**: Each user can have multiple companies

### Mobile Features
- **Camera Capture**: All attachment uploads support mobile camera capture via HTML5 `capture="environment"`
- **Capacitor Support**: Native camera access on mobile apps (QuoteAttachments)

## Key Entities

- **Invoice**: DRAFT, SENT, PAID, CANCELLED statuses
- **Quote**: Can be converted to Invoice
- **Customer/Client**: Customer information
- **Company**: Multi-tenant - each user has companies
- **Product**: Inventory items with optional stock tracking (quantityOnHand, reorderPoint)
- **Service**: Service items for invoicing
- **Supplier**: Vendors for purchase orders
- **PurchaseOrder**: Procurement workflow (DRAFT, SENT, PARTIALLY_RECEIVED, RECEIVED)
- **StockMovement**: Audit trail for inventory changes (SALE, PURCHASE, ADJUSTMENT)
- **Asset**: Company-owned assets separate from saleable products
- **Job**: Work tracking with notes, attachments, time entries
- **JobTimeEntry**: Time tracking with duration, employee, billable status

## Frontend Patterns

- Pages in `frontend/src/pages/`
- Components in `frontend/src/components/` with Radix UI primitives
- Hooks in `frontend/src/hooks/` using TanStack Query
- State: Zustand for global state (`useAppStore`)
- Types in `frontend/src/types/index.ts`
- Schemas in `frontend/src/types/schemas.ts` (Zod validation)

### Line Items & ItemSearch

Invoice and Quote line items use `ItemSearch` component (`frontend/src/components/line-items/ItemSearch.tsx`):
- Autocomplete search for products and services
- Caches all products/services locally (5-minute TanStack Query cache)
- Uses React Portal to render dropdown outside table (avoids overflow clipping)
- Keyboard navigation: Arrow Up/Down, Enter to select, Escape to close
- Links `productItemId` or `serviceItemId` to line items
- Shows inventory status (in stock, low stock, out of stock) for tracked products

### Inventory Integration

When an invoice is SENT, `InventoryService.processInvoiceStockDeduction()` automatically:
- Decreases `quantityOnHand` for linked products with `trackInventory=true`
- Creates `StockMovement` records for audit trail

## Backend Patterns

- Controllers in `backend/src/main/java/io/quickledger/controllers/`
- Services in `backend/src/main/java/io/quickledger/services/`
- Repositories in `backend/src/main/java/io/quickledger/repositories/`
- DTOs in `backend/src/main/java/io/quickledger/dto/`
- MapStruct mappers in `backend/src/main/java/io/quickledger/mappers/`

### Field Naming

- Invoice date field: `invoiceDate` (not `issueDate`) - must match backend DTO
- IDs from backend are `Long` - frontend uses `z.coerce.string()` in Zod schemas to handle number-to-string conversion

### MapStruct Notes

When mapping entities with nullable FK references (e.g., `productItemId`, `serviceItemId`), use `@AfterMapping` to explicitly set null:
```java
@AfterMapping
default void afterMapping(Dto dto, @MappingTarget Entity entity) {
    if (dto.getProductItemId() == null) entity.setProductItem(null);
}
```

## Deployment

- K8s context: `ottero-prod`
- Images: `ghcr.io/ai-soft-labs/ottero-frontend`, `ghcr.io/ai-soft-labs/ottero-backend`
- CI/CD: GitHub Actions (frontend-ci.yml, backend-ci.yml, deploy.yml)
