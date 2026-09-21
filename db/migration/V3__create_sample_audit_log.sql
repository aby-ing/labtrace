USE labtrace;

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
