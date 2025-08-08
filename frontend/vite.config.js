import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { fileURLToPath, URL } from 'url'

// https://vitejs.dev/config/
export default defineConfig({
	plugins: [react()],
	server: {
		port: 3000,
		proxy: {
			// Proxy all API requests starting with /api to the Spring Boot backend
			'/api': {
				target: 'http://localhost:8081',
				changeOrigin: true,
				secure: false,
				rewrite: (path) => path.replace(/^\/api/, '/TechnikTeam/api'),
			},
			// Proxy all WebSocket connections to the Spring Boot backend
			'/ws': {
				target: 'ws://localhost:8081',
				ws: true,
				rewrite: (path) => path.replace(/^\/ws/, '/TechnikTeam/ws'),
			},
			// Proxy direct access to backend resources like Swagger UI
			'/TechnikTeam': {
				target: 'http://localhost:8081',
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
})