import { useState, useEffect } from "react";
import { api } from "../api/client";

export default function Users() {
  const [users, setUsers] = useState([]);
  const [form, setForm] = useState({ loginId: "", name: "", email: "", password: "", roles: ["MEMBER"] });
  const [showForm, setShowForm] = useState(false);
  const [error, setError] = useState("");

  const load = async () => {
    try {
      const res = await api.getUsers();
      setUsers(res.content ?? []);
    } catch (e) { alert(e.message); }
  };

  useEffect(() => { load(); }, []);

  const submit = async (e) => {
    e.preventDefault(); setError("");
    try {
      await api.createUser(form);
      setForm({ loginId: "", name: "", email: "", password: "", roles: ["MEMBER"] });
      setShowForm(false);
      await load();
    } catch (e) { setError(e.message); }
  };

  const deactivate = async (id) => {
    if (!confirm("このユーザーを無効化しますか？")) return;
    try { await api.deleteUser(id); await load(); }
    catch (e) { alert(e.message); }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h2>ユーザー管理</h2>
        <button onClick={() => setShowForm(!showForm)}>
          {showForm ? "キャンセル" : "+ 新規ユーザー"}
        </button>
      </div>

      {showForm && (
        <form className="user-form" onSubmit={submit}>
          <input placeholder="ログインID *" value={form.loginId}
            onChange={(e) => setForm({ ...form, loginId: e.target.value })} required />
          <input placeholder="氏名 *" value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })} required />
          <input placeholder="メールアドレス" value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })} />
          <input type="password" placeholder="パスワード *" value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })} required />
          <select value={form.roles[0]}
            onChange={(e) => setForm({ ...form, roles: [e.target.value] })}>
            <option value="MEMBER">MEMBER</option>
            <option value="MANAGER">MANAGER</option>
            <option value="ADMIN">ADMIN</option>
          </select>
          {error && <p className="error">{error}</p>}
          <button type="submit">作成</button>
        </form>
      )}

      <table className="tbl">
        <thead>
          <tr><th>ログインID</th><th>氏名</th><th>メール</th><th>ロール</th><th>状態</th><th></th></tr>
        </thead>
        <tbody>
          {users.length === 0 && (
            <tr><td colSpan={6} className="empty">ユーザーがいません</td></tr>
          )}
          {users.map((u) => (
            <tr key={u.id}>
              <td>{u.loginId}</td>
              <td>{u.name}</td>
              <td>{u.email ?? "—"}</td>
              <td>{u.roles.join(", ")}</td>
              <td>{u.active ? "有効" : "無効"}</td>
              <td>
                <button className="btn-sm danger" onClick={() => deactivate(u.id)}>無効化</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
