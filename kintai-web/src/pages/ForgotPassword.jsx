import { useState } from "react";
import { api } from "../api/client";

export default function ForgotPassword({ onBack }) {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await api.forgotPassword(email);
      setSent(true);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-wrap">
      <div className="login-card">
        <h1>パスワードをお忘れの方</h1>

        {sent ? (
          <div>
            <p className="msg success" style={{ marginBottom: "16px" }}>
              パスワードリセット用のメールを送信しました。<br />
              メールボックスをご確認ください。
            </p>
            <p style={{ fontSize: "13px", color: "#6b7280", marginBottom: "20px" }}>
              ※ リンクの有効期限は1時間です。<br />
              ※ メールが届かない場合は迷惑メールフォルダもご確認ください。
            </p>
            <button onClick={onBack} style={{ width: "100%", padding: "11px", background: "#f1f5f9", color: "#374151", border: "none", borderRadius: "8px", fontSize: "15px", cursor: "pointer" }}>
              ログイン画面に戻る
            </button>
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            <p style={{ fontSize: "14px", color: "#6b7280", marginBottom: "16px" }}>
              登録済みのメールアドレスを入力してください。<br />
              パスワードリセット用のリンクをお送りします。
            </p>
            <label>メールアドレス</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="example@email.com"
              required
            />
            {error && <p className="error">{error}</p>}
            <button type="submit" disabled={loading}>
              {loading ? "送信中..." : "リセットメールを送信"}
            </button>
            <button
              type="button"
              onClick={onBack}
              style={{ width: "100%", marginTop: "8px", padding: "11px", background: "none", color: "#6b7280", border: "none", fontSize: "14px", cursor: "pointer" }}
            >
              ← ログイン画面に戻る
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
