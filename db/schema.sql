CREATE DATABASE IF NOT EXISTS labtrace
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE labtrace;

CREATE TABLE IF NOT EXISTS lab_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录用户名',
  password VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密后的密码',
  real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
  role VARCHAR(20) NOT NULL COMMENT '角色：ADMIN 或 USER',
  status INT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS sample (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  sample_no VARCHAR(80) NOT NULL UNIQUE COMMENT '样品编号',
  sample_name VARCHAR(100) NOT NULL COMMENT '样品名称',
  source_lab VARCHAR(100) NOT NULL COMMENT '来源实验室',
  creator_id BIGINT NOT NULL COMMENT '创建人 ID',
  custodian_id BIGINT NULL COMMENT '当前保管人 ID',
  status VARCHAR(30) NOT NULL COMMENT '状态：CREATED/HANDED_OVER/STORED/TESTING/COMPLETED/ABNORMAL',
  risk_level VARCHAR(20) NOT NULL COMMENT '风险等级：LOW/MEDIUM/HIGH',
  version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_sample_creator_id (creator_id),
  INDEX idx_sample_custodian_id (custodian_id),
  INDEX idx_sample_status (status),
  INDEX idx_sample_created_at (created_at),
  CONSTRAINT fk_sample_creator
    FOREIGN KEY (creator_id) REFERENCES lab_user(id),
  CONSTRAINT fk_sample_custodian
    FOREIGN KEY (custodian_id) REFERENCES lab_user(id)
) ENGINE=InnoDB COMMENT='样品表';

CREATE TABLE IF NOT EXISTS sample_handover (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  sample_id BIGINT NOT NULL COMMENT '样品 ID',
  from_user_id BIGINT NOT NULL COMMENT '交出人 ID',
  to_user_id BIGINT NOT NULL COMMENT '接收人 ID',
  from_status VARCHAR(30) NOT NULL COMMENT '原状态',
  to_status VARCHAR(30) NOT NULL COMMENT '目标状态',
  remark VARCHAR(255) NULL COMMENT '备注',
  handover_time DATETIME NOT NULL COMMENT '交接时间',
  INDEX idx_handover_sample_id (sample_id),
  INDEX idx_handover_time (handover_time),
  CONSTRAINT fk_handover_sample
    FOREIGN KEY (sample_id) REFERENCES sample(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_handover_from_user
    FOREIGN KEY (from_user_id) REFERENCES lab_user(id),
  CONSTRAINT fk_handover_to_user
    FOREIGN KEY (to_user_id) REFERENCES lab_user(id)
) ENGINE=InnoDB COMMENT='样品交接记录表';

CREATE TABLE IF NOT EXISTS sample_audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  event_id VARCHAR(64) NOT NULL UNIQUE COMMENT '事件唯一编号',
  sample_id BIGINT NOT NULL COMMENT '样品 ID',
  event_type VARCHAR(50) NOT NULL COMMENT '事件类型',
  operator_id BIGINT NULL COMMENT '操作人 ID',
  to_user_id BIGINT NULL COMMENT '接收人 ID',
  from_status VARCHAR(30) NULL COMMENT '原状态',
  to_status VARCHAR(30) NULL COMMENT '目标状态',
  remark VARCHAR(255) NULL COMMENT '备注',
  occurred_at DATETIME NOT NULL COMMENT '事件发生时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '日志写入时间',
  INDEX idx_audit_sample_id (sample_id),
  INDEX idx_audit_occurred_at (occurred_at),
  CONSTRAINT fk_audit_sample
    FOREIGN KEY (sample_id) REFERENCES sample(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_audit_operator
    FOREIGN KEY (operator_id) REFERENCES lab_user(id),
  CONSTRAINT fk_audit_to_user
    FOREIGN KEY (to_user_id) REFERENCES lab_user(id)
) ENGINE=InnoDB COMMENT='样品审计日志表';

CREATE TABLE IF NOT EXISTS outbox_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  event_id VARCHAR(64) NOT NULL UNIQUE COMMENT '事件唯一编号',
  event_type VARCHAR(50) NOT NULL COMMENT '事件类型',
  exchange_name VARCHAR(100) NOT NULL COMMENT 'RabbitMQ 交换机',
  routing_key VARCHAR(100) NOT NULL COMMENT 'RabbitMQ 路由键',
  payload JSON NOT NULL COMMENT '事件 JSON 内容',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SENDING/PUBLISHED/FAILED',
  retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
  sending_started_at DATETIME NULL COMMENT '开始发送时间',
  next_retry_at DATETIME NULL COMMENT '下次重试时间',
  last_error VARCHAR(500) NULL COMMENT '最近一次错误',
  published_at DATETIME NULL COMMENT '发送成功时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_outbox_event_id (event_id),
  INDEX idx_outbox_ready (status, next_retry_at),
  INDEX idx_outbox_created_at (created_at)
) ENGINE=InnoDB COMMENT='待发送消息表';
