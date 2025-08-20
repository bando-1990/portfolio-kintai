-- V6__calc_policy.sql
CREATE TABLE IF NOT EXISTS work_policies (
  id serial PRIMARY KEY,
  code varchar(50) NOT NULL,         -- 例: FIXED_12-13
  version int NOT NULL DEFAULT 1,
  fixed_lunch_start time,            -- 例: 12:00
  fixed_lunch_end   time,            -- 例: 13:00
  thresholds jsonb,                  -- 例: {">=360":45,">=480":60}
  rounding varchar(16) DEFAULT 'floor',  -- floor|ceil|round
  active_from date NOT NULL DEFAULT CURRENT_DATE
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_work_policies ON work_policies(code, version);

-- 適用中のポリシを attendance に刻む（既存の calc_policy_code でも可）
