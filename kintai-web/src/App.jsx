import { useState, useEffect } from "react";
import { api } from "./api/client";
import Login from "./pages/Login";
import ForgotPassword from "./pages/ForgotPassword";
import ResetPassword from "./pages/ResetPassword";
import Dashboard from "./pages/Dashboard";
import Records from "./pages/Records";
import Corrections from "./pages/Corrections";
import Users from "./pages/Users";
import MyProfile from "./pages/MyProfile";
import "./App.css";

const NAV = [
  { key: "dashboard",   label: "打刻" },
  { key: "records",     label: "勤怠レコード" },
  { key: "corrections", label: "修正申請" },
  { key: "profile",     label: "プロフィール設定" },
];

// URLクエリパラメータからリセットトークンを取得
function getResetToken() {
  return new URLSearchParams(window.location.search).get("token");
}
function getViewParam() {
  return new URLSearchParams(window.location.search).get("view");
}
function clearUrlParams() {
  window.history.replaceState({}, "", window.location.pathname);
}

export default function App() {
  const [loggedIn, setLoggedIn] = useState(!!localStorage.getItem("token"));
  const [page, setPage] = useState("dashboard");
  const [me, setMe] = useState(null);
  const [authView, setAuthView] = useState(() => {
    // メールリンクからの遷移を判定
    const view = getViewParam();
    const token = getResetToken();
    if (view === "reset" && token) return "reset";
    return "login";
  });

  useEffect(() => {
    if (!loggedIn) return;
    api.me()
      .then(setMe)
      .catch(() => { localStorage.removeItem("token"); setLoggedIn(false); });
  }, [loggedIn]);

  const logout = () => {
    localStorage.removeItem("token");
    setLoggedIn(false);
    setMe(null);
    setPage("dashboard");
  };

  const goToLogin = () => {
    clearUrlParams();
    setAuthView("login");
  };

  if (!loggedIn) {
    if (authView === "forgot") return <ForgotPassword onBack={goToLogin} />;
    if (authView === "reset") {
      const token = getResetToken();
      return <ResetPassword token={token} onBack={goToLogin} />;
    }
    return <Login onLogin={() => { setLoggedIn(true); setPage("dashboard"); }} onForgotPassword={() => setAuthView("forgot")} />;
  }

  const isAdmin = me?.roles?.includes("ADMIN");
  const nav = isAdmin ? [...NAV, { key: "users", label: "ユーザー管理" }] : NAV;


  return (
    <div className="layout">
      <nav className="sidebar">
        <div className="app-title">勤怠管理</div>
        {nav.map((n) => (
          <button
            key={n.key}
            className={`nav-btn ${page === n.key ? "active" : ""}`}
            onClick={() => setPage(n.key)}
          >
            {n.label}
          </button>
        ))}
        <div className="spacer" />
        {me && <div className="user-info">{me.name}</div>}
        <button className="nav-btn logout" onClick={logout}>ログアウト</button>
      </nav>

      <main className="content">
        {page === "dashboard"   && <Dashboard />}
        {page === "records"     && <Records me={me} />}
        {page === "corrections" && <Corrections me={me} />}
        {page === "users"       && isAdmin && <Users />}
        {page === "profile"     && <MyProfile me={me} onUpdated={(updated) => setMe(updated)} />}
      </main>
    </div>
  );
}
