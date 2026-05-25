import { useState, useEffect } from "react";
import { api } from "../api/client";

function firstDay() {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-01`;
}

function toToday() {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
}

function fmtTime(dt) {
  if (!dt) return "—";
  return new Date(dt).toLocaleTimeString("ja-JP", { hour: "2-digit", minute: "2-digit" });
}

function fmtMinutes(min) {
  if (min == null) return "—";
  return `${Math.floor(min / 60)}h${String(min % 60).padStart(2, "0")}m`;
}

function timeToIso(timeStr, baseDate) {
  if (!timeStr) return null;
  return `${baseDate}T${timeStr}:00+09:00`;
}

function toTimeInput(iso) {
  if (!iso) return "";
  const d = new Date(iso);
  return `${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
}

const STATUS_LABEL = { NOT_STARTED: "未出勤", WORKING: "出勤中", COMPLETED: "退勤済" };
const STATUS_COLOR = { NOT_STARTED: "#6b7280", WORKING: "#16a34a", COMPLETED: "#2563eb" };

export default function Records({ me }) {
  const [from, setFrom] = useState(firstDay());
  const [to, setTo] = useState(toToday());
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(false);
  const [allUsers, setAllUsers] = useState([]);
  const [selectedUserId, setSelectedUserId] = useState("");

  // 修正申請モーダル
  const [corrRecord, setCorrRecord] = useState(null);
  const [corrForm, setCorrForm] = useState({ clockIn: "", clockOut: "", reason: "" });
  const [corrMsg, setCorrMsg] = useState("");
  const [corrLoading, setCorrLoading] = useState(false);

  // CSV出力
  const thisYear = new Date().getFullYear();
  const [exportYear, setExportYear] = useState(thisYear);
  const [exportMonth, setExportMonth] = useState(new Date().getMonth() + 1);
  const [exportUserId, setExportUserId] = useState("");
  const [exportLoading, setExportLoading] = useState(false);

  const isAdmin = me?.roles?.includes("ADMIN");

  useEffect(() => {
    if (!isAdmin) return;
    api.getAllUsers().then(setAllUsers).catch(() => {});
  }, [isAdmin]);

  const load = async (overrideUserId) => {
    setLoading(true);
    try {
      const uid = overrideUserId !== undefined ? overrideUserId : selectedUserId;
      setRecords(await api.getRecords(from, to, uid || null));
    } catch (e) {
      alert(e.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleUserChange = (e) => {
    const uid = e.target.value;
    setSelectedUserId(uid);
    load(uid);
  };

  const openCorrModal = (record) => {
    setCorrRecord(record);
    setCorrForm({
      clockIn: toTimeInput(record.clockInAt),
      clockOut: toTimeInput(record.clockOutAt),
      reason: "",
    });
    setCorrMsg("");
  };

  const submitCorrection = async () => {
    if (!corrForm.reason.trim()) { setCorrMsg("修正理由を入力してください"); return; }
    setCorrLoading(true); setCorrMsg("");
    try {
      await api.createCorrection(
        corrRecord.id,
        corrForm.reason,
        corrForm.clockIn ? timeToIso(corrForm.clockIn, corrRecord.workDate) : null,
        corrForm.clockOut ? timeToIso(corrForm.clockOut, corrRecord.workDate) : null,
      );
      setCorrMsg("修正申請を送信しました");
      setTimeout(() => setCorrRecord(null), 1200);
    } catch (e) { setCorrMsg(e.message); }
    finally { setCorrLoading(false); }
  };

  const handleExport = async () => {
    setExportLoading(true);
    try {
      const monthVal = exportMonth === 0 ? null : exportMonth;
      // 管理者で "" = 全員（userId なし）、それ以外は UUID を渡す
      const userIdParam = isAdmin ? (exportUserId === "" ? null : exportUserId) : null;
      const blob = await api.exportCsv(exportYear, monthVal, userIdParam);
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      const label = monthVal
        ? `${exportYear}_${String(monthVal).padStart(2, "0")}`
        : `${exportYear}`;
      a.href = url;
      a.download = `kintai_${label}.csv`;
      a.click();
      URL.revokeObjectURL(url);
    } catch (e) { alert(e.message); }
    finally { setExportLoading(false); }
  };

  const totalGross = records.reduce((s, r) => s + (r.grossWorkMinutes ?? 0), 0);
  const totalBreak = records.reduce((s, r) => s + (r.autoBreakMinutes ?? 0), 0);
  const totalNet   = records.reduce((s, r) => s + (r.netWorkMinutes ?? 0), 0);

  return (
    <div className="page">
      <h2>勤怠レコード</h2>

      {/* 検索フィルター */}
      <div className="filter-row">
        {isAdmin && (
          <select value={selectedUserId} onChange={handleUserChange}>
            <option value="">自分</option>
            {allUsers.map((u) => (
              <option key={u.id} value={u.id}>{u.name}</option>
            ))}
          </select>
        )}
        <input type="date" value={from} onChange={(e) => setFrom(e.target.value)} />
        <span>〜</span>
        <input type="date" value={to} onChange={(e) => setTo(e.target.value)} />
        <button onClick={() => load()} disabled={loading}>検索</button>
      </div>

      {/* レコードテーブル */}
      {loading ? <p>読込中...</p> : (
        <table className="tbl">
          <thead>
            <tr>
              <th>日付</th>
              <th>出勤</th>
              <th>退勤</th>
              <th>総労働</th>
              <th>休憩控除</th>
              <th>実働</th>
              <th>状態</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {records.length === 0 && (
              <tr><td colSpan={8} className="empty">レコードがありません</td></tr>
            )}
            {records.map((r) => (
              <tr key={r.id}>
                <td>{r.workDate}</td>
                <td>{fmtTime(r.clockInAt)}</td>
                <td>{fmtTime(r.clockOutAt)}</td>
                <td>{fmtMinutes(r.grossWorkMinutes)}</td>
                <td>{fmtMinutes(r.autoBreakMinutes)}</td>
                <td><strong>{fmtMinutes(r.netWorkMinutes)}</strong></td>
                <td>
                  <span className="badge" style={{ background: STATUS_COLOR[r.status] ?? "#6b7280" }}>
                    {STATUS_LABEL[r.status] ?? r.status}
                  </span>
                </td>
                <td>
                  <button className="btn-text small" onClick={() => openCorrModal(r)}>
                    修正申請
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
          {records.length > 0 && (
            <tfoot>
              <tr className="report-total">
                <td colSpan={3}>合計</td>
                <td>{fmtMinutes(totalGross)}</td>
                <td>{fmtMinutes(totalBreak)}</td>
                <td><strong>{fmtMinutes(totalNet)}</strong></td>
                <td colSpan={2}></td>
              </tr>
            </tfoot>
          )}
        </table>
      )}

      {/* CSV出力セクション */}
      <div className="export-section">
        <h3>CSV出力</h3>
        <div className="filter-row">
          <select value={exportYear} onChange={(e) => setExportYear(Number(e.target.value))}>
            {[thisYear - 1, thisYear, thisYear + 1].map((y) => (
              <option key={y} value={y}>{y}年</option>
            ))}
          </select>
          <select value={exportMonth} onChange={(e) => setExportMonth(Number(e.target.value))}>
            <option value={0}>年間（全月）</option>
            {Array.from({ length: 12 }, (_, i) => i + 1).map((m) => (
              <option key={m} value={m}>{m}月</option>
            ))}
          </select>
          {isAdmin && (
            <select value={exportUserId} onChange={(e) => setExportUserId(e.target.value)}>
              <option value="">全員</option>
              {allUsers.map((u) => (
                <option key={u.id} value={u.id}>{u.name}</option>
              ))}
            </select>
          )}
          <button className="btn-primary" onClick={handleExport} disabled={exportLoading}>
            {exportLoading ? "出力中..." : "CSVダウンロード"}
          </button>
        </div>
      </div>

      {/* 修正申請モーダル */}
      {corrRecord && (
        <div className="modal-overlay" onClick={() => setCorrRecord(null)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h3>時刻修正申請</h3>
            <p className="modal-note">
              {corrRecord.workDate} の修正申請<br />
              変更しない項目は空白のままで可
            </p>
            <div className="form-group">
              <label>出勤時刻</label>
              <input
                type="time"
                value={corrForm.clockIn}
                onChange={(e) => setCorrForm({ ...corrForm, clockIn: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label>退勤時刻</label>
              <input
                type="time"
                value={corrForm.clockOut}
                onChange={(e) => setCorrForm({ ...corrForm, clockOut: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label>修正理由 <span className="required">*</span></label>
              <textarea
                rows={3}
                placeholder="例：打刻ミスのため"
                value={corrForm.reason}
                onChange={(e) => setCorrForm({ ...corrForm, reason: e.target.value })}
              />
            </div>
            {corrMsg && (
              <p className={corrMsg.includes("送信") ? "msg success" : "msg error"}>{corrMsg}</p>
            )}
            <div className="modal-footer">
              <button className="btn-secondary" onClick={() => setCorrRecord(null)}>キャンセル</button>
              <button className="btn-primary" onClick={submitCorrection} disabled={corrLoading}>
                {corrLoading ? "送信中..." : "申請する"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
