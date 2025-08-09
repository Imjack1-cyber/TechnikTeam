import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import { fileURLToPath, URL } from 'url'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
	const env = loadEnv(mode, process.cwd(), '');
	const backendTarget = env.VITE_API_TARGET_URL || 'http://localhost:8081';
	const wsTarget = backendTarget.replace(/^http/, 'ws');

	return {
		plugins: [react()],
		server: {
			port: 3000,
			proxy: {
				// Proxy all API requests starting with /api to the Spring Boot backend
				'/api': {
					target: backendTarget,
					changeOrigin: true,
					secure: false,
					rewrite: (path) => path.replace(/^\/api/, '/TechnikTeam/api'),
				},
				// Proxy all WebSocket connections to the Spring Boot backend
				'/ws': {
					target: wsTarget,
					ws: true,
					rewrite: (path) => path.replace(/^\/ws/, '/TechnikTeam/ws'),
				},
				// Proxy direct access to backend resources like Swagger UI
				'/TechnikTeam': {
					target: backendTarget,
					changeOrigin: true,
					secure: false,
				}
			},
		},
		resolve: {
			alias: {
				'@': fileURLToPath(new URL('./src', import.meta.url))
			}
		}
	}
})