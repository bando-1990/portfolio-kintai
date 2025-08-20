# kintai-ops

Prometheus/Grafanaのダッシュボード・アラートを管理します（雛形）。

- `prometheus/rules.yml` に主要アラート（p95遅延、5xx 率など）を定義
- `grafana/dashboards/` にJSONダッシュボードを配置（必要に応じて追加）
