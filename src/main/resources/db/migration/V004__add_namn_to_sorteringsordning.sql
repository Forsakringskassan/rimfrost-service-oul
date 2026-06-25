ALTER TABLE sorteringsordning ADD COLUMN namn VARCHAR NOT NULL DEFAULT '';
UPDATE sorteringsordning SET namn = id::varchar WHERE namn = '';
ALTER TABLE sorteringsordning ALTER COLUMN namn DROP DEFAULT;