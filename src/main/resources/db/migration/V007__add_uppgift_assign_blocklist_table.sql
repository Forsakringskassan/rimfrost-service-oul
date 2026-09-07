CREATE TABLE uppgift_assign_blocklist(
    uppgift_id                   UUID NOT NULL,
    handlaggare_id_typ_id         VARCHAR(255) NOT NULL,
    handlaggare_id_varde          VARCHAR(255) NOT NULL,
    PRIMARY KEY(uppgift_id, handlaggare_id_typ_id, handlaggare_id_varde)
);

ALTER TABLE uppgift_assign_blocklist ADD CONSTRAINT fk_uppgift_assign_blocklist_uppgift FOREIGN KEY (uppgift_id) REFERENCES uppgift(id) ON UPDATE CASCADE ON DELETE CASCADE;