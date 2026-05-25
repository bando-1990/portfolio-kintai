-- V5__add_unique_email.sql : メールアドレスのユニーク制約追加
-- NULL は複数ユーザーが持てるが、値がある場合は重複不可
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_email
  ON users (email)
  WHERE email IS NOT NULL;
