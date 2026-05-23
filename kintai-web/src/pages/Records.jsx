import { useState, useEffect } from "react";
import { api } from "../api/client";

function firstDay() {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-01`;
}
function today() { return new Date().toISOString().slice(0, 10); }
function fmtTime(dt) {
  if (!dt) return "—";
  return new Date(dt).toLocaleTimeString("ja-JP", { hour: "2-digit", minute: "2-digit" });
}

export default function Records() {
  const [from, setFrom] = useState(firstDay());
  const [to, setTo] = useState(today());
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(false);

  const load = async () => {
    setLoading(true);
    try { setRecords(await api.getRecords(from, to)); }
    catch (e) { alert(e.message); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const statusLabel = { NOT_STARTED: "未出勤", WORKING: "出勤中", COMPLETED: "退勤済" };

  return (
    <div className="page">
      <h2>勤怠レコード</h2>
      <div className="filter-row">
        <input type="date" value={from} onChange={(e) => setFrom(e.target.value)} />
        <span>〜</span>
        <input type="date" value={to} onChange={(e) => setTo(e.target.value)} />
        <button onClick={load} disabled={loading}>検索</button>
      </div>

      {loading ? <p>読込中...</p> : (
        <table className="tbl">
          <thead>
            <tr><th>日付</th><th>出勤</th><th>退勤</th><th>実働</th><th>状態</th></tr>
          </thead>
          <tbody>
            {records.length === 0 && (
              <tr><td colSpan={5} className="empty">レコードがありません</td></tr>
            )}
            {records.map((r) => (
              <tr key={r.id}>
                <td>{r.workDate}</td>
                <td>{fmtTime(r.clockInAt)}</td>
                <td>{fmtTime(r.clockOutAt)}</td>
                <td>{r.netWorkMinutes != null
                  ? `${Math.floor(r.netWorkMinutes / 60)}h${r.netWorkMinutes % 60}m`
                  : "—"}
                </td>
                <td>{statusLabel[r.status] ?? r.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
