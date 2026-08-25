-- 用户数据库建表脚本（每个用户注册时在其独立 H2 文件库中执行）
-- 不含用户登录凭证表（eta_user_account 在默认数据源中）

-- 用户档案
CREATE TABLE IF NOT EXISTS eta_user_profile (
  id VARCHAR(36) PRIMARY KEY,
  nickname VARCHAR(64) NOT NULL,
  avatar VARCHAR(512),
  bio VARCHAR(512),
  created_at TIMESTAMP
);

-- 笔记
CREATE TABLE IF NOT EXISTS eta_note (
  id VARCHAR(36) PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  body CLOB NOT NULL,
  tone VARCHAR(255),
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

-- 笔记标签
CREATE TABLE IF NOT EXISTS note_tag (
  id VARCHAR(36) PRIMARY KEY,
  name VARCHAR(64) NOT NULL UNIQUE,
  color VARCHAR(20)
);

-- 笔记-标签关联
CREATE TABLE IF NOT EXISTS note_note_tag (
  note_id VARCHAR(36) NOT NULL,
  tag_id VARCHAR(36) NOT NULL,
  PRIMARY KEY (note_id, tag_id)
);

-- 任务
CREATE TABLE IF NOT EXISTS eta_task (
  id VARCHAR(36) PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  description VARCHAR(1000),
  category_id VARCHAR(36),
  category VARCHAR(255),
  tag VARCHAR(255),
  tone VARCHAR(255),
  due VARCHAR(255),
  due_at TIMESTAMP,
  status VARCHAR(255),
  progress INTEGER,
  priority VARCHAR(255),
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

-- 任务分类
CREATE TABLE IF NOT EXISTS eta_task_category (
  id VARCHAR(36) PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  color VARCHAR(255)
);

-- 错题
CREATE TABLE IF NOT EXISTS eta_error (
  id VARCHAR(36) PRIMARY KEY,
  question CLOB NOT NULL,
  answer CLOB NOT NULL,
  subject VARCHAR(64),
  source VARCHAR(128),
  level VARCHAR(20),
  tone VARCHAR(20),
  wrong_count INTEGER,
  mastered BOOLEAN,
  created_at TIMESTAMP,
  last_wrong_at TIMESTAMP,
  next_review_at TIMESTAMP,
  difficulty DOUBLE,
  stability DOUBLE,
  last_review_at TIMESTAMP
);

-- 错题标签
CREATE TABLE IF NOT EXISTS error_tags (
  error_id VARCHAR(36) NOT NULL,
  tag VARCHAR(255)
);

-- 简历
CREATE TABLE IF NOT EXISTS eta_resume (
  id VARCHAR(36) PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  template_id VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  real_name VARCHAR(255),
  role VARCHAR(255),
  email VARCHAR(255),
  phone VARCHAR(255)
);

-- 简历-教育经历
CREATE TABLE IF NOT EXISTS resume_education (
  resume_id VARCHAR(36) NOT NULL,
  sort_order INTEGER,
  title VARCHAR(255),
  subtitle VARCHAR(255),
  period VARCHAR(255)
);

-- 简历-工作经历
CREATE TABLE IF NOT EXISTS resume_experience (
  resume_id VARCHAR(36) NOT NULL,
  sort_order INTEGER,
  title VARCHAR(255),
  subtitle VARCHAR(255),
  period VARCHAR(255)
);

-- 简历-项目经历
CREATE TABLE IF NOT EXISTS resume_projects (
  resume_id VARCHAR(36) NOT NULL,
  sort_order INTEGER,
  title VARCHAR(255),
  subtitle VARCHAR(255),
  period VARCHAR(255)
);

-- 简历-技能
CREATE TABLE IF NOT EXISTS resume_skills (
  resume_id VARCHAR(36) NOT NULL,
  sort_order INTEGER,
  skill VARCHAR(255)
);
