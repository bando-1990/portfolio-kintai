import { useState } from "react";
import { api } from "../api/client";

export default function Login({ onLogin, onForgotPassword }) {
  const [loginId, setLoginId] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const res = await api.login(loginId, password);
      localStorage.setItem("token", res.accessToken);
      onLogin();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-wrap">
      <div className="login-card">
        <h1>勤怠管理システム</h1>
        <form onSubmit={handleSubmit}>
          <label>ログインID</label>
          <input value={loginId} onChange={(e) => setLoginId(e.target.value)} required />
          <label>パスワード</label>
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
          {error && <p className="error">{error}</p>}
          <button type="submit" disabled={loading}>
            {loading ? "ログイン中..." : "ログイン"}
          </button>
        </form>
        <div style={{ textAlign: "center", marginTop: "16px" }}>
          <button className="btn-text" onClick={onForgotPassword}>
            パスワードをお忘れの方はこちら
          </button>
        </div>
      </div>
    </div>
  );
}
