CREATE TABLE sorteringsordning (
    id          UUID        NOT NULL PRIMARY KEY,
    version     BIGINT      NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    entries     TEXT        NOT NULL
);

CREATE TABLE default_sorteringsordning (
    lock                 BOOLEAN NOT NULL DEFAULT TRUE PRIMARY KEY,
    sorteringsordning_id UUID    NOT NULL REFERENCES sorteringsordning(id),
    CONSTRAINT one_row CHECK (lock = TRUE)
);
