# kintai-spec

このリポジトリは **API仕様と図** のソース・オブ・トゥルースです。

- `docs/openapi.yaml` — OpenAPI（v0.2.0）。休憩は「申請なし／自動算出」。  
- `docs/diagrams/*.mmd` — Mermaid図（状態遷移／ER／クラス）。
- `docs/index.html` — ReDoc等で生成して公開可能（任意）。

## 開発メモ
```bash
# Lint
npx @redocly/cli lint docs/openapi.yaml

# ドキュメントHTML出力
npx @redocly/cli build-docs docs/openapi.yaml -o docs/index.html

# モックAPI
npx @stoplight/prism-cli mock docs/openapi.yaml -p 4010
