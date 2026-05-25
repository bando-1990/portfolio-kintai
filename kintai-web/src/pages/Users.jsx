import { useState, useEffect, useRef } from "react";
import { api } from "../api/client";

export default function Users() {
  const [users, setUsers] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [form, setForm] = useState({ loginId: "", name: "", email: "", password: "", roles: ["MEMBER"] });
  const [showForm, setShowForm] = useState(false);
  const [error, setError] = useState("");
  const [importResult, setImportResult] = useState(null);
  const [importing, setImporting] = useState(false);
  const [selectedIds, setSelectedIds] = useState(new Set());
  const fileInputRef = useRef(null);

  const load = async (p = page) => {
    try {
      const res = await api.getUsers(p);
      setUsers(res.content ?? []);
      setTotalPages(res.totalPages ?? 0);
      setTotalElements(res.totalElements ?? 0);
      setSelectedIds(new Set());
    } catch (e) { alert(e.message); }
  };

  useEffect(() => { load(page); }, [page]);

  const submit = async (e) => {
    e.preventDefault(); setError("");
    try {
      await api.createUser(form);
      setForm({ loginId: "", name: "", email: "", password: "", roles: ["MEMBER"] });
      setShowForm(false);
      setPage(0);
      await load(0);
    } catch (e) { setError(e.message); }
  };

  const handleStatusChange = async (id, newActive) => {
    try { await api.toggleUserActive(id, newActive); await load(page); }
    catch (e) { alert(e.message); }
  };

  const handleCsvImport = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setImporting(true); setImportResult(null);
    try {
      const result = await api.importUsersCsv(file);
      setImportResult(result);
      setPage(0);
      await load(0);
    } catch (e) {
      setImportResult({ created: 0, skipped: 0, errors: 1, errorDetails: [e.message] });
    } finally {
      setImporting(false);
      e.target.value = "";
    }
  };

  const toggleCheck = (id) => {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  };

  const toggleAll = (e) => {
    setSelectedIds(e.target.checked ? new Set(users.map((u) => u.id)) : new Set());
  };

  const deleteSelected = async () => {
    if (selectedIds.size === 0) return;
    if (!confirm(`選択した ${selectedIds.size} 件のユーザーを完全に削除しますか？\nこの操作は取り消せません。`)) return;
    const errors = [];
    for (const id of selectedIds) {
      try { await api.deleteUser(id); }
      catch (e) {
        const user = users.find((u) => u.id === id);
        errors.push(`${user?.name ?? id}: ${e.message}`);
      }
    }
    setPage(0);
    await load(0);
    if (errors.length > 0) alert("以下のユーザーは削除できませんでした:\n" + errors.join("\n"));
  };

  const allChecked = users.length > 0 && selectedIds.size === users.length;

  return (
    <div className="page">
      <div className="page-header">
        <h2>ユーザー管理</h2>
        <div style={{ display: "flex", gap: "8px", alignItems: "center" }}>
          {selectedIds.size > 0 && (
            <button className="btn-delete-selected" onClick={deleteSelected}>
              選択削除（{selectedIds.size}件）
            </button>
          )}
          <button onClick={() => { setShowForm(!showForm); setImportResult(null); }}>
            {showForm ? "キャンセル" : "+ 新規ユーザー"}
          </button>
          <button onClick={() => fileInputRef.current?.click()} disabled={importing}>
            {importing ? "インポート中..." : "CSVインポート"}
          </button>
          <input ref={fileInputRef} type="file" accept=".csv"
            style={{ display: "none" }} onChange={handleCsvImport} />
        </div>
      </div>

      {importResult && (
        <div className={`import-result ${importResult.errors > 0 ? "has-error" : "success"}`}>
          <p>
            <strong>インポート完了</strong>
            　登録: {importResult.created}件　スキップ（重複）: {importResult.skipped}件　エラー: {importResult.errors}件
          </p>
          {importResult.created > 0 && (
            <p className="import-note">初期パスワードは各ユーザーの社員IDです。</p>
          )}
          {importResult.errorDetails?.length > 0 && (
            <ul className="import-errors">
              {importResult.errorDetails.map((d, i) => <li key={i}>{d}</li>)}
            </ul>
          )}
        </div>
      )}

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
          <tr>
            <th style={{ width: "36px" }}>
              <input type="checkbox" checked={allChecked} onChange={toggleAll} />
            </th>
            <th>ログインID</th>
            <th>氏名</th>
            <th>メール</th>
            <th>ロール</th>
            <th>状態</th>
          </tr>
        </thead>
        <tbody>
          {users.length === 0 && (
            <tr><td colSpan={6} className="empty">ユーザーがいません</td></tr>
          )}
          {users.map((u) => (
            <tr key={u.id} className={u.active ? "" : "row-inactive"}>
              <td>
                <input type="checkbox"
                  checked={selectedIds.has(u.id)}
                  onChange={() => toggleCheck(u.id)} />
              </td>
              <td>{u.loginId}</td>
              <td>{u.name}</td>
              <td>{u.email ?? "—"}</td>
              <td>{u.roles.join(", ")}</td>
              <td>
                <select
                  className="status-select"
                  value={u.active ? "active" : "inactive"}
                  onChange={(e) => handleStatusChange(u.id, e.target.value === "active")}
                >
                  <option value="active">有効</option>
                  <option value="inactive">無効</option>
                </select>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {totalPages > 1 && (
        <div className="pagination">
          <button
            className="page-btn"
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={page === 0}
          >
            前へ
          </button>
          <span className="page-info">
            {page + 1} / {totalPages} ページ（全{totalElements}件）
          </span>
          <button
            className="page-btn"
            onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
            disabled={page >= totalPages - 1}
          >
            次へ
          </button>
        </div>
      )}
    </div>
  );
}
