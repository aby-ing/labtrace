USE labtrace;

SET @column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'outbox_message'
      AND column_name = 'sending_started_at'
);

SET @add_column_sql = IF(
    @column_exists = 0,
    'ALTER TABLE outbox_message ADD COLUMN sending_started_at DATETIME NULL COMMENT ''开始发送时间'' AFTER retry_count',
    'SELECT 1'
);

PREPARE add_column_statement FROM @add_column_sql;
EXECUTE add_column_statement;
DEALLOCATE PREPARE add_column_statement;

UPDATE outbox_message
SET status = 'FAILED',
    sending_started_at = NULL,
    next_retry_at = NOW(),
    last_error = COALESCE(
        last_error,
        '服务重启后恢复超时的发送任务'
    )
WHERE status = 'SENDING'
  AND (
        sending_started_at IS NULL
        OR sending_started_at <= DATE_SUB(NOW(), INTERVAL 5 MINUTE)
      );
