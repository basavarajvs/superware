# NexaWare WMS - Unified PostgreSQL Database Schema

This directory contains the unified and modularized PostgreSQL schema for the NexaWare Warehouse Management System. The schema has been refactored from multiple SQL dialects into a consistent, maintainable set of scripts organized by business domain.

## Unified Schema Structure (`unified_postgres_schema/`)

The new schema is designed for PostgreSQL and follows a logical, modular structure to improve clarity, maintainability, and scalability.

### Schema Files Overview

The SQL scripts are designed to be executed in numerical order to ensure all dependencies are met.

-   **`00_utils.sql`**: Contains shared utility functions, such as the `update_updated_at_column()` trigger function used across multiple tables to automatically manage `updated_at` timestamps.
-   **`01_system_auth.sql`**: Defines the core tables for system-wide authentication and multi-tenancy, including `tenants`, `users`, and `roles`.
-   **`02_geography.sql`**: Establishes tables for geographic data, such as `countries`, `states`, and `cities`.
-   **`03_clients_vendors.sql`**: Contains tables for managing business partners, including `clients` and `vendors`.
-   **`04_products.sql`**: Defines the product catalog, including `product_categories`, `products`, and `product_variants`.
-   **`05_facilities.sql`**: Manages the physical warehouse structure, including `facilities`, `storage_zones`, and `storage_locations`.
-   **`06_inventory.sql`**: Covers all aspects of inventory management, including `inventory_lots`, `onhand_inventory`, and `inventory_transactions`.
-   **`07_purchasing.sql`**: Defines the procurement process with tables for `purchase_orders`, `po_receipts`, and `vendor_returns`.
-   **`08_sales.sql`**: Manages the sales and fulfillment lifecycle, including `sales_orders`, `shipments`, and `customer_returns`.
-   **`09_warehouse_ops.sql`**: Contains schemas for internal warehouse operations, such as `warehouse_tasks` (for picking, put-away) and `work_orders` (for kitting/assembly).
-   **`10_accounting.sql`**: Provides a complete, multi-tenant double-entry accounting system, including a chart of accounts, journal entries, and invoicing.
-   **`11_billing.sql`**: Manages tenant subscriptions, licensing plans, and billing history.
-   **`12_asn.sql`**: Advanced Shipping Notice management with complete ASN lifecycle tracking.
-   **`13_quality_management.sql`**: Comprehensive quality management system with inspection plans and non-conformance tracking.
-   **`14_shipping_logistics.sql`**: Complete shipping and logistics management with carrier integration.
-   **`15_advanced_picking.sql`**: Sophisticated picking optimization and path planning.
-   **`16_packing_loading.sql`**: Complete packing and loading dock management.
-   **`99_foreign_keys.sql`**: A dedicated script for adding any deferred foreign key constraints. In the current design, all foreign keys are created inline, so this file serves as a placeholder for future needs.

## Key Design Principles

-   **PostgreSQL Native**: The schema is written exclusively for PostgreSQL, leveraging features like `SERIAL` primary keys, `TIMESTAMPTZ`, `ENUM` types, and `JSONB`.
-   **Modularity**: Each file corresponds to a specific business domain, making the schema easier to understand and manage.
-   **Multi-Tenancy**: A `tenant_id` is enforced across relevant tables to support a multi-tenant architecture.
-   **Data Integrity**: Foreign key constraints, `NOT NULL`, and `UNIQUE` constraints are used to ensure data consistency.
-   **Timestamp Automation**: A reusable trigger automatically updates the `updated_at` field on any row modification.
-   **Soft Deletes**: Most tables include an `is_deleted` boolean field to support soft deletion.
-   **Audit Trail**: An `audit_log` table tracks all data modifications for compliance and troubleshooting.
-   **Comprehensive Indexing**: Appropriate indexes are defined to optimize query performance.

## How to Set Up the Database

To initialize a new database, execute the `.sql` scripts from the `unified_postgres_schema/` directory in numerical order (from `00` to `99`).

```bash
# Example using psql command-line tool
DB_NAME="your_db_name"
DB_USER="your_user"

for f in unified_postgres_schema/*.sql; do
  echo "Executing $f..."
  psql -U $DB_USER -d $DB_NAME -a -f "$f"
done
```

This will create all the tables, types, and functions in the correct order.

## Schema Features

### Multi-Tenancy
- Full tenant isolation with tenant-specific data partitioning
- Tenant licensing and subscription management
- Usage tracking and billing

### Inventory Management
- Real-time stock tracking with lot/serial support
- Multiple storage locations and zones
- Cycle counting and inventory adjustments
- Expiration and FIFO/FEFO support

### Inbound Operations
- Purchase order management
- ASN (Advanced Shipping Notice) processing with complete lifecycle
- Receiving and put-away workflows
- Quality inspection integration

### Outbound Operations
- Sales order processing
- Picking optimization with zone and path planning
- Packing and shipping with material tracking
- Load planning and dock scheduling

### Quality Management
- Configurable inspection plans and checklists
- Quality alerts and non-conformance tracking
- Corrective and preventive actions
- Comprehensive quality metrics

### Financial Management
- Complete accounting system with chart of accounts
- Customer invoicing and vendor bills
- Payment processing and tracking
- Journal entries and financial reporting

### Advanced Features
- Role-based access control
- Comprehensive audit logging
- Workflow automation
- Reporting and analytics views
- API integration points

## Performance Considerations

- Optimized for both OLTP and reporting workloads
- Appropriate indexes for common query patterns
- Consideration for data retention policies
- Partitioning strategies for large tables (to be implemented)

## Integration Points

- REST API endpoints
- Webhook support for events
- Data import/export capabilities
- Reporting and analytics views

## Documentation

- Complete ERD diagram (to be generated)
- Data dictionary
- API documentation (to be created)
- Sample queries for common operations
- Migration scripts from existing schema