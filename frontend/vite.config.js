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
				target: 'http://localhost:8080/TechnikTeam',
				changeOrigin: true,
				secure: false,
			},
			// Proxy all WebSocket connections to the Spring Boot backend
			'/ws': {
				target: 'ws://localhost:8080/TechnikTeam',
				ws: true, // Enable WebSocket proxying
			},
		},
	},
	resolve: {
		alias: {
			'@': fileURLToPath(new URL('./src', import.meta.url))
		}
	}
})
