import vue from '@vitejs/plugin-vue'
import type { IncomingMessage, ServerResponse } from 'node:http'
import { defineConfig, loadEnv, type Plugin } from 'vite'
import { diagnoseFeishu, fetchFeedingRecords, isFeishuConfigured } from './server/feishu'

function attachFeishuApi(
  middlewares: { use: (fn: ReturnType<typeof feishuMiddleware>) => void },
  env: Record<string, string>,
) {
  middlewares.use(feishuMiddleware(env))
}

function feishuMiddleware(env: Record<string, string>) {
  return async (req: IncomingMessage, res: ServerResponse, next: () => void) => {
          const url = req.url?.split('?')[0]
          if (url === '/api/health') {
            res.setHeader('Content-Type', 'application/json; charset=utf-8')
            res.end(
              JSON.stringify({
                configured: isFeishuConfigured(env),
              }),
            )
            return
          }
          if (url === '/api/feishu/diagnose' && req.method === 'GET') {
            try {
              const report = await diagnoseFeishu(env)
              res.setHeader('Content-Type', 'application/json; charset=utf-8')
              res.end(JSON.stringify(report))
            } catch (err) {
              const message = err instanceof Error ? err.message : String(err)
              res.statusCode = 500
              res.setHeader('Content-Type', 'application/json; charset=utf-8')
              res.end(JSON.stringify({ error: message }))
            }
            return
          }
          if (url !== '/api/feedings' || req.method !== 'GET') {
            next()
            return
          }
          try {
            const items = await fetchFeedingRecords(env)
            res.setHeader('Content-Type', 'application/json; charset=utf-8')
            res.end(JSON.stringify({ items, fetchedAt: Date.now() }))
          } catch (err) {
            const message = err instanceof Error ? err.message : String(err)
            res.statusCode = 500
            res.setHeader('Content-Type', 'application/json; charset=utf-8')
            res.end(JSON.stringify({ error: message }))
          }
        }
}

function feishuApiPlugin(env: Record<string, string>): Plugin {
  return {
    name: 'hush-note-feishu-api',
    configureServer(server) {
      attachFeishuApi(server.middlewares, env)
    },
    configurePreviewServer(server) {
      attachFeishuApi(server.middlewares, env)
    },
  }
}

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  return {
    plugins: [vue(), feishuApiPlugin(env)],
    server: {
      port: 5173,
    },
  }
})
