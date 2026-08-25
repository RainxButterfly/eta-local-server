-- 默认数据库建表脚本（仅存放用户登录凭证，用于注册/登录认证）
-- 用户档案信息在每个用户独立的 H2 文件库中，由 schema-user.sql 管理

CREATE TABLE IF NOT EXISTS eta_user_account (
  id VARCHAR(36) PRIMARY KEY,
  email VARCHAR(128) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  disabled BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP
);
