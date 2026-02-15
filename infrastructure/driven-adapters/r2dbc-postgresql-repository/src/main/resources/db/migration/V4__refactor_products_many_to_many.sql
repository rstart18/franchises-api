-- Migration V4: Refactor products table to support many-to-many relationship with branches
-- Remove branch_id FK and stock from products table, create junction table branch_products

-- Drop existing columns from products table
ALTER TABLE products DROP COLUMN IF EXISTS branch_id;
ALTER TABLE products DROP COLUMN IF EXISTS stock;

-- Add unique constraint on product name (globally unique products)
ALTER TABLE products ADD CONSTRAINT products_name_unique UNIQUE (name);

-- Create junction table for many-to-many relationship
CREATE TABLE IF NOT EXISTS branch_products (
    product_id  BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    branch_id   BIGINT NOT NULL REFERENCES branches(id) ON DELETE CASCADE,
    stock       INTEGER NOT NULL DEFAULT 0,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    deleted_at  TIMESTAMP,
    PRIMARY KEY (product_id, branch_id)
);

-- Create indexes for frequent queries
CREATE INDEX idx_branch_products_branch_id ON branch_products(branch_id);
CREATE INDEX idx_branch_products_product_id ON branch_products(product_id);
