ALTER TABLE uppgift ADD CONSTRAINT unique_uppgift_handlaggning_regel_erbjudande UNIQUE (handlaggning_id, regel, erbjudande_id);
