import { useState, useEffect } from "react";
import { api } from "../api/client";

const STATUS_LABEL = { pending: "申請中", approved: "承認済", rejected: "却下" };
const STATUS_COLOR = { pending: "#d97706", approved: "#16a34a", rejected: "#dc2626" };

export default function Corrections({ me }) {
  const [corrections, setCorrections] = useState([]);
  const [role, setRole] = useState("applicant");
  const [status, setStatus] = useState("");
  const [comment, setComment] = useState("");
  const isManager = me?.roles?.includes("ADMIN") || me?.roles?.includes("MANAGER");

  const load = async () => {
    try { setCorrections(await api.getCorrections(role, status || undefined)); }
    catch (e) { alert(e.message); }
  };

  useEffect(() => { load(); }, [role, status]);

  const approve = async (id) => {
    try { await api.approveCorrection(id, comment); await load(); setComment(""); }
    catch (e) { alert(e.message); }
  };

  const reject = async (id) => {
    try { await api.rejectCorrection(id, comment); await load(); setComment(""); }
    catch (e) { alert(e.message); }
  };

  return (
    <div className="page">
      <h2>修正申請</h2>
      <div className="filter-row">
        {isManager && (
          <select value={role} onChange={(e) => setRole(e.target.value)}>
            <option value="applicant">申請者として</option>
            <option value="approver">承認者として</option>
          </select>
        )}
        <select value={status} onChange={(e) => setStatus(e.target.value)}>
          <option value="">すべて</option>
          <option value="pending">申請中</option>
          <option value="approved">承認済</option>
          <option value="rejected">却下</option>
        </select>
        <button onClick={load}>更新</button>
      </div>

      {corrections.length === 0 ? (
        <p className="empty">申請がありません</p>
      ) : corrections.map((c) => (
        <div key={c.id} className="correction-card">
          <div className="correction-header">
            <span className="badge" style={{ background: STATUS_COLOR[c.status] }}>
              {STATUS_LABEL[c.status] ?? c.status}
            </span>
            <span className="date">{c.createdAt?.slice(0, 10)}</span>
          </div>
          <p><strong>理由：</strong>{c.reason}</p>
          {c.reviewerComment && <p><strong>コメント：</strong>{c.reviewerComment}</p>}

          {isManager && c.status === "pending" && role === "approver" && (
            <div className="approval-row">
              <input
                placeholder="コメント（任意）"
                value={comment}
                onChange={(e) => setComment(e.target.value)}
              />
              <button className="btn-approve" onClick={() => approve(c.id)}>承認</button>
              <button className="btn-reject" onClick={() => reject(c.id)}>却下</button>
            </div>
          )}
        </div>
      ))}
    </div>
  );
}
