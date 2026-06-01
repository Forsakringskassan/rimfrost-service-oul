CREATE TABLE uppgift (
    id                           UUID NOT NULL PRIMARY KEY,
    handlaggning_id              UUID NOT NULL,
    handlaggar_id_typ_id         VARCHAR(255),
    handlaggar_id_varde          VARCHAR(255),
    skapad                       DATE NOT NULL,
    planerad_till                DATE,
    utford                       DATE,
    status                       VARCHAR(255) NOT NULL,
    regel                        VARCHAR(255) NOT NULL,
    beskrivning                  VARCHAR(255) NOT NULL,
    verksamhetslogik             VARCHAR(255) NOT NULL,
    roll                         VARCHAR(255) NOT NULL,
    url                          VARCHAR(255) NOT NULL,
    sub_topic                    VARCHAR(255) NOT NULL,
    erbjudande_id                VARCHAR(255) NOT NULL,
    erbjudande_namn              VARCHAR(255) NOT NULL,
    reason                       VARCHAR(255),
    version                      BIGINT      NOT NULL DEFAULT 0,
    created_at                   TIMESTAMPTZ NOT NULL,
    updated_at                   TIMESTAMPTZ NOT NULL
);

CREATE TABLE uppgift_individ (
    uppgift_id                   UUID NOT NULL,
    typ_id                       VARCHAR(255) NOT NULL,
    varde                        VARCHAR(255) NOT NULL,
    PRIMARY KEY(uppgift_id, typ_id, varde)
);

CREATE TABLE uppgift_cloud_event_attribute (
    uppgift_id                   UUID NOT NULL,
    cloud_event_attribute_key    VARCHAR(255) NOT NULL,
    cloud_event_attribute_value  VARCHAR(255),
    PRIMARY KEY(uppgift_id, cloud_event_attribute_key)
);

ALTER TABLE uppgift_individ ADD CONSTRAINT fk_uppgift_individ_uppgift FOREIGN KEY (uppgift_id) REFERENCES uppgift(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE uppgift_cloud_event_attribute ADD CONSTRAINT fk_uppgift_cloud_event_attribute_uppgift FOREIGN KEY (uppgift_id) REFERENCES uppgift(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE uppgift_cloud_event_attribute ADD CONSTRAINT unique_uppgift_id_cloud_event_attribute_key UNIQUE (uppgift_id, cloud_event_attribute_key);