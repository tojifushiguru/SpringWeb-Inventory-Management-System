-- Add user tracking columns to categories table
ALTER TABLE categories 
ADD COLUMN created_by VARCHAR(50),
ADD COLUMN last_modified_at TIMESTAMP,
ADD COLUMN last_modified_by VARCHAR(50);

-- Update existing records to have a default created_by value
UPDATE categories 
SET created_by = 'SYSTEM'
WHERE created_by IS NULL;

-- Add indexes for better performance
CREATE INDEX idx_categories_created_by ON categories(created_by);
CREATE INDEX idx_categories_last_modified_by ON categories(last_modified_by);
CREATE INDEX idx_categories_last_modified_at ON categories(last_modified_at);