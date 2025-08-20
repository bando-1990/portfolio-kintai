# portfolio-kintai

複数リポジトリを一括で並べたプロジェクト親フォルダの雛形です。

- `kintai-spec` — OpenAPIと各種図
- `kintai-api` — Spring Boot API + Flyway DDL
- `kintai-infra` — ローカルDB(Docker)など
- `kintai-ops` — 監視/アラート（雛形）
- `kintai-web` — フロント（将来）

## 最初にやること
1. `kintai-infra/local/docker-compose.yml` でPostgres起動  
2. `kintai-api` を起動（FlywayがV1..適用）  
3. `kintai-spec/docs/openapi.yaml` を lint / mock で確認
