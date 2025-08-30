-- Make fields NOT NULL
ALTER TABLE users ALTER COLUMN name SET NOT NULL;
ALTER TABLE users ALTER COLUMN username SET NOT NULL;
ALTER TABLE users ALTER COLUMN email SET NOT NULL;

-- Add unique constraint/index on email
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email ON users(email);

-- Ensure ID is auto-increment/identity (H2 syntax)
-- Depending on H2 version, both AUTO_INCREMENT and IDENTITY syntax are acceptable.
-- Try to set identity; if already identity, this may be a no-op.
ALTER TABLE users ALTER COLUMN id BIGINT AUTO_INCREMENT;