import { useState } from "react";
import { api } from "../api/client";

export default function MyProfile({ me, onUpdated }) {
  const [email, setEmail] = useState(me?.email ?? "");
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [emailMsg, setEmailMsg] = useState("");
  const [emailError, setEmailError] = useState("");
  const [pwMsg, setPwMsg] = useState("");
  const [pwError, setPwError] = useState("");
  const [loadingEmail, setLoadingEmail] = useState(false);
  const [loadingPw, setLoadingPw] = useState(false);

  // メールアドレスのみ更新
  const handleEmailSubmit = async (e) => {
    e.preventDefault();
    setEmailMsg(""); setEmailError("");
    setLoadingEmail(true);
    try {
      const updated = await api.updateMyProfile({ email });
      setLoadingEmail(false);
      setEmailMsg("メールアドレスを更新しました");
      onUpdated(updated);
    } catch (err) {
      setLoadingEmail(false);
      setEmailError(err.message);
    }
  };

  // パスワードのみ更新
  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    setPwMsg(""); setPwError("");

    if (newPassword !== confirmPassword) {
      setPwError("新しいパスワードが一致しません");
      return;
    }

    setLoadingPw(true);
    try {
      await api.updateMyProfile({ currentPassword, newPassword });
      setLoadingPw(false);
      setPwMsg("パスワードを変更しました");
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
    } catch (err) {
      setLoadingPw(false);
      setPwError(err.message);
    }
  };

  return (
    <div className="page">
      <h2>プロフィール設定</h2>

      {/* メールアドレス設定 */}
      <div className="profile-section">
        <h3>メールアドレス</h3>
        <p className="profile-note">
          パスワードを忘れた際のリセットメール送信先として使用します。
        </p>
        <form onSubmit={handleEmailSubmit} className="profile-form">
          <div className="form-group">
            <label>メールアドレス</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="example@email.com"
            />
          </div>
          {emailMsg && <p className="msg success">{emailMsg}</p>}
          {emailError && <p className="msg error">{emailError}</p>}
          <div className="profile-footer">
            <button type="submit" className="btn-primary" disabled={loadingEmail}>
              {loadingEmail ? "更新中..." : "メールアドレスを保存"}
            </button>
          </div>
        </form>
      </div>

      {/* パスワード変更 */}
      <div className="profile-section">
        <h3>パスワード変更</h3>
        <p className="profile-note">
          初回ログイン時や定期的なパスワード変更にご利用ください。
        </p>
        <form onSubmit={handlePasswordSubmit} className="profile-form">
          <div className="form-group">
            <label>現在のパスワード <span className="required">*</span></label>
            <input
              type="password"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              required
            />
          </div>
          <div className="form-group">
            <label>新しいパスワード <span className="required">*</span></label>
            <input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              placeholder="8文字以上"
              required
              minLength={8}
            />
          </div>
          <div className="form-group">
            <label>新しいパスワード（確認） <span className="required">*</span></label>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="もう一度入力"
              required
            />
          </div>
          {pwMsg && <p className="msg success">{pwMsg}</p>}
          {pwError && <p className="msg error">{pwError}</p>}
          <div className="profile-footer">
            <button type="submit" className="btn-primary" disabled={loadingPw}>
              {loadingPw ? "変更中..." : "パスワードを変更する"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
