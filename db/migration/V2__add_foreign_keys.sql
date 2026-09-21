USE labtrace;

ALTER TABLE sample
    ADD CONSTRAINT fk_sample_creator
        FOREIGN KEY (creator_id) REFERENCES lab_user(id),
    ADD CONSTRAINT fk_sample_custodian
        FOREIGN KEY (custodian_id) REFERENCES lab_user(id);

ALTER TABLE sample_handover
    ADD CONSTRAINT fk_handover_sample
        FOREIGN KEY (sample_id) REFERENCES sample(id)
        ON DELETE CASCADE,
    ADD CONSTRAINT fk_handover_from_user
        FOREIGN KEY (from_user_id) REFERENCES lab_user(id),
    ADD CONSTRAINT fk_handover_to_user
        FOREIGN KEY (to_user_id) REFERENCES lab_user(id);
