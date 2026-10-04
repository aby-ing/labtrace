USE labtrace;

SET @sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.TABLE_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = DATABASE()
          AND TABLE_NAME = 'sample'
          AND CONSTRAINT_NAME = 'fk_sample_creator'
    ),
    'SELECT 1',
    'ALTER TABLE sample ADD CONSTRAINT fk_sample_creator FOREIGN KEY (creator_id) REFERENCES lab_user(id)'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.TABLE_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = DATABASE()
          AND TABLE_NAME = 'sample'
          AND CONSTRAINT_NAME = 'fk_sample_custodian'
    ),
    'SELECT 1',
    'ALTER TABLE sample ADD CONSTRAINT fk_sample_custodian FOREIGN KEY (custodian_id) REFERENCES lab_user(id)'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.TABLE_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = DATABASE()
          AND TABLE_NAME = 'sample_handover'
          AND CONSTRAINT_NAME = 'fk_handover_sample'
    ),
    'SELECT 1',
    'ALTER TABLE sample_handover ADD CONSTRAINT fk_handover_sample FOREIGN KEY (sample_id) REFERENCES sample(id) ON DELETE CASCADE'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.TABLE_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = DATABASE()
          AND TABLE_NAME = 'sample_handover'
          AND CONSTRAINT_NAME = 'fk_handover_from_user'
    ),
    'SELECT 1',
    'ALTER TABLE sample_handover ADD CONSTRAINT fk_handover_from_user FOREIGN KEY (from_user_id) REFERENCES lab_user(id)'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.TABLE_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = DATABASE()
          AND TABLE_NAME = 'sample_handover'
          AND CONSTRAINT_NAME = 'fk_handover_to_user'
    ),
    'SELECT 1',
    'ALTER TABLE sample_handover ADD CONSTRAINT fk_handover_to_user FOREIGN KEY (to_user_id) REFERENCES lab_user(id)'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
