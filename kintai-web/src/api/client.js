const BASE_URL = "http://localhost:8080";

function getToken() {
  return localStorage.getItem("token");
}

async function request(method, path, body = null, extraHeaders = {}) {
  const headers = { "Content-Type": "application/json", ...extraHeaders };
  const token = getToken();
  if (token) headers["Authorization"] = `Bearer ${token}`;

  const res = await fetch(`${BASE_URL}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : null,
  });

  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: res.statusText }));
    throw new Error(err.message || "エラーが発生しました");
  }

  if (res.status === 204) return null;
  return res.json();
}

export const api = {
  login: (loginId, password) =>
    request("POST", "/auth/login", { loginId, password }),

  me: () => request("GET", "/me"),

  clockIn: () =>
    request("POST", "/attendance/clock-in", {}, { "Idempotency-Key": crypto.randomUUID() }),

  clockOut: () =>
    request("POST", "/attendance/clock-out", {}, { "Idempotency-Key": crypto.randomUUID() }),

  getRecords: (from, to) =>
    request("GET", `/attendance/records?from=${from}&to=${to}`),

  getDailySummary: (from, to) =>
    request("GET", `/reports/summary/daily?from=${from}&to=${to}`),

  getUsers: (page = 0) => request("GET", `/users?page=${page}&size=20`),
  createUser: (data) => request("POST", "/users", data),
  updateUser: (id, data) => request("PATCH", `/users/${id}`, data),
  deleteUser: (id) => request("DELETE", `/users/${id}`),

  getCorrections: (role, status) => {
    const params = new URLSearchParams({ role });
    if (status) params.append("status", status);
    return request("GET", `/attendance/corrections?${params}`);
  },
  createCorrection: (recordId, reason, requestedClockIn, requestedClockOut) =>
    request("POST", "/attendance/corrections", { recordId, reason, requestedClockIn, requestedClockOut }),
  approveCorrection: (id, comment) =>
    request("POST", `/attendance/corrections/${id}/approve`, { comment }),
  rejectCorrection: (id, comment) =>
    request("POST", `/attendance/corrections/${id}/reject`, { comment }),
};
