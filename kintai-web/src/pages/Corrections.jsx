import { useState, useEffect } from "react";
import { api } from "../api/client";

const STATUS_LABEL = { pending: "申請中", approved: "承認済", rejected: "却下" };
const STATUS_COLOR = { pending: "#d97706", approved: "#16a34a", rejected: "#dc2626" };

function fmtTime(iso) {
  if (!iso) return "—";
  return new Date(iso).toLocaleTimeString("ja-JP", { hour: "2-digit", minute: "2-digit" });
}

export default function Corrections({ me }) {
  const [corrections, setCorrections] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [role, setRole] = useState("applicant");
  const [status, setStatus] = useState("pending");
  const [page, setPage] = useState(0);
  const [comments, setComments] = useState({});
  const isManager = me?.roles?.includes("ADMIN") || me?.roles?.includes("MANAGER");
  const PAGE_SIZE = 10;

  const load = async (r, s, p) => {
    try {
      const res = await api.getCorrections(r, s || undefined, p, PAGE_SIZE);
      setCorrections(res.content ?? []);
      setTotalPages(res.totalPages ?? 0);
      setTotalElements(res.totalElements ?? 0);
    } catch (e) { alert(e.message); }
  };

  useEffect(() => {
    setPage(0);
    load(role, status, 0);
  }, [role, status]);

  useEffect(() => {
    load(role, status, page);
  }, [page]);

  const approve = async (id) => {
    try { await api.approveCorrection(id, comments[id] ?? ""); await load(role, status, page); }
    catch (e) { alert(e.message); }
  };

  const reject = async (id) => {
    try { await api.rejectCorrection(id, comments[id] ?? ""); await load(role, status, page); }
    catch (e) { alert(e.message); }
  };

  const setComment = (id, val) => setComments((prev) => ({ ...prev, [id]: val }));

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
        <button onClick={() => load(role, status, page)}>更新</button>
      </div>

      {corrections.length === 0 ? (
        <p className="empty">申請がありません</p>
      ) : corrections.map((c) => (
        <div key={c.id} className="correction-card">
          <div className="correction-header">
            <span className="badge" style={{ background: STATUS_COLOR[c.status] }}>
              {STATUS_LABEL[c.status] ?? c.status}
            </span>
            <span className="date">申請日: {c.createdAt?.slice(0, 10)}</span>
          </div>

          {/* 希望時刻 */}
          {(c.requestedClockIn || c.requestedClockOut) && (
            <div className="time-change">
              <span>希望時刻：</span>
              {c.requestedClockIn && <span>出勤 <strong>{fmtTime(c.requestedClockIn)}</strong></span>}
              {c.requestedClockOut && <span>退勤 <strong>{fmtTime(c.requestedClockOut)}</strong></span>}
            </div>
          )}

          <p><strong>理由：</strong>{c.reason}</p>
          {c.reviewerComment && <p><strong>承認者コメント：</strong>{c.reviewerComment}</p>}
          {c.decidedAt && <p className="decided-at">処理日: {c.decidedAt.slice(0, 10)}</p>}

          {isManager && c.status === "pending" && role === "approver" && (
            <div className="approval-row">
              <input
                placeholder="コメント（任意）"
                value={comments[c.id] ?? ""}
                onChange={(e) => setComment(c.id, e.target.value)}
              />
              <button className="btn-approve" onClick={() => approve(c.id)}>承認</button>
              <button className="btn-reject" onClick={() => reject(c.id)}>却下</button>
            </div>
          )}
        </div>
      ))}

      {/* ページネーション */}
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
