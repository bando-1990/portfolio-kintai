# 勤怠管理アプリ

打刻・勤怠レコード管理・修正申請・CSV出力・パスワードリセットを備えた勤怠管理Webアプリケーションです。

## 技術スタック

| レイヤー | 技術 |
|---|---|
| フロントエンド | React 18 / Vite / JavaScript |
| バックエンド | Spring Boot 3 / Java 21 / Spring Security / JWT |
| データベース | PostgreSQL 16 |
| マイグレーション | Flyway |
| インフラ | Docker / docker-compose |

## 機能一覧

- **ログイン / ログアウト**（JWT認証）
- **打刻**（出勤・退勤・自動休憩控除）
- **勤怠レコード一覧**（月次フィルタ・修正申請ボタン）
- **修正申請**（申請・承認・却下）
- **CSV出力**（月次/年次・ユーザー別/全員・UTF-8 BOM形式）
- **ユーザー管理**（管理者：作成・一括CSV登録・有効/無効切替）
- **プロフィール編集**（メールアドレス・パスワード変更）
- **パスワードリセット**（メール送信によるトークン認証）

## ロール

| ロール | 権限 |
|---|---|
| ADMIN | 全機能 + ユーザー管理 + 全員のCSV出力 |
| MANAGER | 修正申請の承認・却下 |
| MEMBER | 打刻・自分のレコード閲覧・修正申請 |

## セットアップ

### 前提条件

- Docker Desktop がインストール済みであること

### 手順

**1. リポジトリをクローン**

```bash
git clone https://github.com/bando-1990/portfolio-kintai.git
cd portfolio-kintai
```

**2. メール設定ファイルを作成**

```bash
cp kintai-app/src/main/resources/application-dev.properties.example \
   kintai-app/src/main/resources/application-dev.properties
```

`application-dev.properties` を開き、Gmailの設定を書き換えてください：

```properties
spring.mail.username=your-email@gmail.com
spring.mail.password=your-gmail-app-password  # Googleアカウントのアプリパスワード（16文字）
```

> **アプリパスワードの発行方法**：Googleアカウント → セキュリティ → 2段階認証を有効化 → アプリパスワードを発行

**3. 起動**

```bash
docker compose up
```

初回はイメージビルド・依存ライブラリのダウンロードのため 10〜15分かかります。

**4. ブラウザでアクセス**

```
http://localhost:5173
```

初期ログイン情報：

| 項目 | 値 |
|---|---|
| ログインID | admin |
| パスワード | password |

> ログイン後、プロフィール設定からパスワードを変更してください。

## 起動コマンド

```bash
# 起動
docker compose up

# バックグラウンド起動
docker compose up -d

# 停止
docker compose down

# バックエンドのみ再起動（Javaコード変更後）
docker compose restart backend
```

## アクセス先

| サービス | URL |
|---|---|
| フロントエンド | http://localhost:5173 |
| バックエンドAPI | http://localhost:8080 |
| PostgreSQL | localhost:5432 |

## DB接続情報（DBeaver等）

| 項目 | 値 |
|---|---|
| Host | localhost |
| Port | 5432 |
| Database | kintai |
| User | kintai |
| Password | kintai |

## アーキテクチャ

```
ブラウザ
  │
  ▼
kintai-frontend（React + Vite / port:5173）
  │  HTTP リクエスト
  ▼
kintai-backend（Spring Boot / port:8080）
  │  JPA / Hibernate
  ▼
kintai-postgres（PostgreSQL / port:5432）
```

### バックエンド構成

```
controller/   ← HTTPリクエストの受け口
service/      ← ビジネスロジック
repository/   ← DBアクセス（Spring Data JPA）
entity/       ← DBテーブルと対応するJavaクラス
dto/          ← リクエスト・レスポンスの型定義
config/       ← セキュリティ設定（JWT）
security/     ← JWTトークンの生成・検証
```

## 本番デプロイ構成（予定）

| レイヤー | サービス |
|---|---|
| フロントエンド | Render / Azure Static Web Apps |
| バックエンド | Render / Azure App Service |
| DB | Neon（クラウドPostgreSQL） |
