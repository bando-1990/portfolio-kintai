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

  forgotPassword: (email) =>
    request("POST", "/auth/forgot-password", { email }),

  resetPassword: (token, newPassword) =>
    request("POST", "/auth/reset-password", { token, newPassword }),

  me: () => request("GET", "/me"),
  updateMyProfile: (data) => request("PATCH", "/me", data),

  clockIn: () =>
    request("POST", "/attendance/clock-in", {}, { "Idempotency-Key": crypto.randomUUID() }),

  clockOut: () =>
    request("POST", "/attendance/clock-out", {}, { "Idempotency-Key": crypto.randomUUID() }),

  getRecords: (from, to, userId) => {
    const params = new URLSearchParams({ from, to });
    if (userId) params.append("userId", userId);
    return request("GET", `/attendance/records?${params}`);
  },

  getUsers: (page = 0) => request("GET", `/users?page=${page}`),
  getAllUsers: () => request("GET", "/users/all"),
  toggleUserActive: (id, active) => request("PATCH", `/users/${id}`, { active }),
  importUsersCsv: (file) => {
    const form = new FormData();
    form.append("file", file);
    const token = getToken();
    const headers = token ? { Authorization: `Bearer ${token}` } : {};
    return fetch(`${BASE_URL}/users/import/csv`, { method: "POST", headers, body: form })
      .then(async (res) => {
        if (!res.ok) {
          const err = await res.json().catch(() => ({ message: res.statusText }));
          throw new Error(err.message || "エラーが発生しました");
        }
        return res.json();
      });
  },
  createUser: (data) => request("POST", "/users", data),
  updateUser: (id, data) => request("PATCH", `/users/${id}`, data),
  deleteUser: (id) => request("DELETE", `/users/${id}`),

  exportCsv: (year, month, userId) => {
    const params = new URLSearchParams({ year });
    if (month != null) params.append("month", month);
    if (userId != null) params.append("userId", userId);
    const token = getToken();
    return fetch(`${BASE_URL}/attendance/records/export?${params}`, {
      headers: { Authorization: `Bearer ${token}` },
    }).then(async (res) => {
      if (!res.ok) {
        const err = await res.json().catch(() => ({ message: res.statusText }));
        throw new Error(err.message || "エクスポートに失敗しました");
      }
      return res.blob();
    });
  },

  getCorrections: (role, status, page = 0, size = 10) => {
    const params = new URLSearchParams({ role, page, size });
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
