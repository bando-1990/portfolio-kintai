-- V6__insert_admin.sql : 初期管理者ユーザーの投入
-- ログインID: admin / 初期パスワード: password
-- ※ 初回ログイン後にプロフィール設定からパスワードを変更してください

INSERT INTO users (id, login_id, name, email, password_hash, active, created_at, updated_at)
VALUES (
  '00000000-0000-0000-0000-000000000001',
  'admin',
  '管理者',
  NULL,
  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
  true,
  NOW(),
  NOW()
) ON CONFLICT (login_id) DO NOTHING;

-- ADMIN ロールを付与（未付与の場合のみ）
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.login_id = 'admin'
  AND r.code = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur
    WHERE ur.user_id = u.id AND ur.role_id = r.id
  );
