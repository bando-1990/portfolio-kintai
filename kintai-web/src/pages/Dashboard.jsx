import { useState, useEffect } from "react";
import { api } from "../api/client";

function fmt(dt) {
  if (!dt) return "—";
  return new Date(dt).toLocaleTimeString("ja-JP", { hour: "2-digit", minute: "2-digit" });
}

function toToday() {
  return new Date().toISOString().slice(0, 10);
}

export default function Dashboard() {
  const [record, setRecord] = useState(null);
  const [loading, setLoading] = useState(false);
  const [msg, setMsg] = useState("");

  useEffect(() => {
    const today = toToday();
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

      {msg && <p className="msg">{msg}</p>}
    </div>
  );
}
