import { useState, useEffect } from "react";
import { api } from "./api/client";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import Records from "./pages/Records";
import Corrections from "./pages/Corrections";
import Users from "./pages/Users";
import "./App.css";

const NAV = [
  { key: "dashboard", label: "打刻" },
  { key: "records",   label: "勤怠レコード" },
  { key: "corrections", label: "修正申請" },
];

export default function App() {
  const [loggedIn, setLoggedIn] = useState(!!localStorage.getItem("token"));
  const [page, setPage] = useState("dashboard");
  const [me, setMe] = useState(null);

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
  };

  if (!loggedIn) return <Login onLogin={() => setLoggedIn(true)} />;

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
        {page === "records"     && <Records />}
        {page === "corrections" && <Corrections me={me} />}
        {page === "users"       && isAdmin && <Users />}
      </main>
    </div>
  );
}
