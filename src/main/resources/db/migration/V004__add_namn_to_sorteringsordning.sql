ALTER TABLE sorteringsordning ADD COLUMN namn VARCHAR NOT NULL;
UPDATE sorteringsordning SET namn = id::varchar WHERE namn = '';