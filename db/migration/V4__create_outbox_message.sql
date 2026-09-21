USE labtrace;

CREATE TABLE IF NOT EXISTS outbox_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  event_id VARCHAR(64) NOT NULL UNIQUE COMMENT '事件唯一编号',
  event_type VARCHAR(50) NOT NULL COMMENT '事件类型',
  exchange_name VARCHAR(100) NOT NULL COMMENT 'RabbitMQ 交换机',
  routing_key VARCHAR(100) NOT NULL COMMENT 'RabbitMQ 路由键',
  payload JSON NOT NULL COMMENT '事件 JSON 内容',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SENDING/PUBLISHED/FAILED',
  retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
  next_retry_at DATETIME NULL COMMENT '下次重试时间',
  last_error VARCHAR(500) NULL COMMENT '最近一次错误',
  published_at DATETIME NULL COMMENT '发送成功时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_outbox_event_id (event_id),
  INDEX idx_outbox_ready (status, next_retry_at),
  INDEX idx_outbox_created_at (created_at)
) ENGINE=InnoDB COMMENT='待发送消息表';
