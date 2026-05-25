# ─────────────────────────────────────
# 開発ステージ（docker compose up で使用）
# ─────────────────────────────────────
FROM node:20-alpine AS development
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
# --host でコンテナ外（ブラウザ）からアクセス可能にする
CMD ["npm", "run", "dev", "--", "--host"]

# ─────────────────────────────────────
# 本番ビルドステージ
# ─────────────────────────────────────
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# ─────────────────────────────────────
# 本番配信ステージ（nginxで静的ファイルを配信）
# ─────────────────────────────────────
FROM nginx:alpine AS production
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
