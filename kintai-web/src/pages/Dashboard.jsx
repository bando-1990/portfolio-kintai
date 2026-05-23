import { useState, useEffect } from "react";
import { api } from "../api/client";

function fmt(dt) {
  if (!dt) return "—";
  return new Date(dt).toLocaleTimeString("ja-JP", { hour: "2-digit", minute: "2-digit" });
}

function toToday() {
  return new Date().toISOString().slice(0, 10);
}

// "HH:MM" を今日の日付の ISO8601 文字列に変換
function timeToIso(timeStr, baseDate) {
  if (!timeStr) return null;
  return `${baseDate}T${timeStr}:00+09:00`;
}

// input[type="time"] 用に HH:MM 形式（24時間）を返す
function toTimeInput(iso) {
  if (!iso) return "";
  const d = new Date(iso);
  return `${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
}

export default function Dashboard() {
  const [record, setRecord] = useState(null);
  const [loading, setLoading] = useState(false);
  const [msg, setMsg] = useState("");
  const [showModal, setShowModal] = useState(false);
  const [corrForm, setCorrForm] = useState({ clockIn: "", clockOut: "", reason: "" });
  const [corrMsg, setCorrMsg] = useState("");
  const [corrLoading, setCorrLoading] = useState(false);

  const today = toToday();

  useEffect(() => {
    api.getRecords(today, today)
      .then((list) => setRecord(list[0] ?? null))
      .catch(() => {});
  }, []);

  const clockIn = async () => {
    setLoading(true); setMsg("");
    try {
      const r = await api.clockIn();
      setRecord(r);
      setMsg("出勤打刻しました");
    } catch (e) { setMsg(e.message); }
    finally { setLoading(false); }
  };

  const clockOut = async () => {
    setLoading(true); setMsg("");
    try {
      const r = await api.clockOut();
      setRecord(r);
      setMsg("退勤打刻しました");
    } catch (e) { setMsg(e.message); }
    finally { setLoading(false); }
  };

  const openModal = () => {
    setCorrForm({
      clockIn: toTimeInput(record?.clockInAt),
      clockOut: toTimeInput(record?.clockOutAt),
      reason: "",
    });
    setCorrMsg("");
    setShowModal(true);
  };

  const submitCorrection = async () => {
    if (!corrForm.reason.trim()) { setCorrMsg("修正理由を入力してください"); return; }
    setCorrLoading(true); setCorrMsg("");
    try {
      await api.createCorrection(
        record.id,
        corrForm.reason,
        corrForm.clockIn ? timeToIso(corrForm.clockIn, today) : null,
        corrForm.clockOut ? timeToIso(corrForm.clockOut, today) : null,
      );
      setCorrMsg("修正申請を送信しました");
      setTimeout(() => setShowModal(false), 1200);
    } catch (e) { setCorrMsg(e.message); }
    finally { setCorrLoading(false); }
  };

  const status = record?.status ?? "NOT_STARTED";
  const statusLabel = { NOT_STARTED: "未出勤", WORKING: "出勤中", COMPLETED: "退勤済" }[status];
  const statusColor = { NOT_STARTED: "#6b7280", WORKING: "#16a34a", COMPLETED: "#2563eb" }[status];

  return (
    <div className="page">
      <h2>今日の打刻</h2>
      <div className="status-badge" style={{ background: statusColor }}>{statusLabel}</div>

      <div className="time-row">
        <div className="time-box">
          <span>出勤</span>
          <strong>{fmt(record?.clockInAt)}</strong>
        </div>
        <div className="time-box">
          <span>退勤</span>
          <strong>{fmt(record?.clockOutAt)}</strong>
        </div>
        {record?.netWorkMinutes != null && (
          <div className="time-box">
            <span>実働</span>
            <strong>{Math.floor(record.netWorkMinutes / 60)}時間{record.netWorkMinutes % 60}分</strong>
          </div>
        )}
      </div>

      <div className="btn-row">
        <button className="btn-clock in" onClick={clockIn}
          disabled={loading || status === "WORKING" || status === "COMPLETED"}>
          出勤
        </button>
        <button className="btn-clock out" onClick={clockOut}
          disabled={loading || status !== "WORKING"}>
          退勤
        </button>
      </div>

      {record && (
        <div className="correction-link">
          <button className="btn-text" onClick={openModal}>時刻の修正を申請する</button>
        </div>
      )}

      {msg && <p className="msg">{msg}</p>}

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h3>時刻修正申請</h3>
            <p className="modal-note">修正したい時刻を入力してください（変更しない項目は空白のままで可）</p>

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

            {corrMsg && <p className={corrMsg.includes("送信") ? "msg success" : "msg error"}>{corrMsg}</p>}

            <div className="modal-footer">
              <button className="btn-secondary" onClick={() => setShowModal(false)}>キャンセル</button>
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
