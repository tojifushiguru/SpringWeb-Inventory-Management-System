-- Modify reorder_level column to have a default value and allow NULL
ALTER TABLE products 
MODIFY COLUMN reorder_level INT DEFAULT 0;

-- Update any existing NULL values to use the default
UPDATE products 
SET reorder_level = 0 
WHERE reorder_level IS NULL;