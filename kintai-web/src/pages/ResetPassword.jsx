import { useState } from "react";
import { api } from "../api/client";

export default function ResetPassword({ token, onBack }) {
  const [newPassword, setNewPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (newPassword !== confirm) {
      setError("パスワードが一致しません");
      return;
    }
    if (newPassword.length < 8) {
      setError("パスワードは8文字以上で入力してください");
      return;
    }

    setLoading(true);
    try {
      await api.resetPassword(token, newPassword);
      setDone(true);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-wrap">
      <div className="login-card">
        <h1>新しいパスワードの設定</h1>

        {done ? (
          <div>
            <p className="msg success" style={{ marginBottom: "16px" }}>
              パスワードを変更しました。<br />
              新しいパスワードでログインしてください。
            </p>
            <button
              onClick={onBack}
              style={{ width: "100%", padding: "11px", background: "#3b82f6", color: "#fff", border: "none", borderRadius: "8px", fontSize: "15px", fontWeight: "600", cursor: "pointer" }}
            >
              ログイン画面へ
            </button>
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            <p style={{ fontSize: "14px", color: "#6b7280", marginBottom: "16px" }}>
              新しいパスワードを入力してください。<br />
              8文字以上で設定してください。
            </p>
            <label>新しいパスワード</label>
            <input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              placeholder="8文字以上"
              required
            />
            <label>パスワード（確認）</label>
            <input
              type="password"
              value={confirm}
              onChange={(e) => setConfirm(e.target.value)}
              placeholder="もう一度入力"
              required
            />
            {error && <p className="error">{error}</p>}
            <button type="submit" disabled={loading}>
              {loading ? "設定中..." : "パスワードを変更する"}
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
