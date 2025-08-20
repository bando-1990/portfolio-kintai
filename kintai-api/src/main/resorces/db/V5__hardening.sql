-- V5__hardening.sql
-- 1) 出退勤の時間整合（両方ある時のみチェック）
ALTER TABLE attendance_records
  ADD CONSTRAINT chk_time_order
  CHECK (
    clock_in_at IS NULL
    OR clock_out_at IS NULL
    OR clock_out_at >= clock_in_at
  );

-- 2) email の重複禁止（NULLは許容）
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_email_not_null
  ON users (email) WHERE email IS NOT NULL;

-- 3) idempotency_keys のTTL運用想定（48h）
--   → クリーニング用に created_at へインデックスは既設。運用で定期削除ジョブ実装。
