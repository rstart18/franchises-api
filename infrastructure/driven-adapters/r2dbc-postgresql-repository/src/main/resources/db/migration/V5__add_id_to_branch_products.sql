-- Migration V5: Add auto-incremental id column to branch_products
-- Spring Data R2DBC requires a single @Id column for save() to distinguish INSERT vs UPDATE

-- Drop composite primary key
ALTER TABLE branch_products DROP CONSTRAINT branch_products_pkey;

-- Add auto-incremental id as new primary key
ALTER TABLE branch_products ADD COLUMN id BIGSERIAL PRIMARY KEY;

-- Keep uniqueness on (product_id, branch_id) as a unique constraint
ALTER TABLE branch_products ADD CONSTRAINT uq_branch_product UNIQUE (product_id, branch_id);
