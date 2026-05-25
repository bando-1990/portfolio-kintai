import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: true,        // コンテナ外（ブラウザ）からアクセス可能にする
    port: 5173,
    watch: {
      usePolling: true, // Dockerボリューム上のファイル変更を検知するために必要
      interval: 1000,
    },
    hmr: {
      clientPort: 5173, // HMR接続ポートをブラウザ側に正しく伝える
    },
  },
})
