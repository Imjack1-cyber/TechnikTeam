import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import { VitePWA } from 'vite-plugin-pwa';
import { fileURLToPath, URL } from 'url'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
	const env = loadEnv(mode, process.cwd(), '');
	const backendTarget = env.VITE_API_TARGET_URL || 'http://localhost:8080';
	const wsTarget = backendTarget.replace(/^http/, 'ws');

	return {
		base: '/',
		plugins: [
			react(),
			VitePWA({
				registerType: 'autoUpdate',
				workbox: {
					globPatterns: ['**/*.{js,css,html,ico,png,svg}']
				},
				manifest: {
					name: 'TechnikTeam',
					short_name: 'TechnikTeam',
					description: 'School Event & Crew Management System',
					theme_color: '#ffffff',
					icons: [
						{
							src: 'pwa-192x192.png',
							sizes: '192x192',
							type: 'image/png'
						},
						{
							src: 'pwa-512x512.png',
							sizes: '512x512',
							type: 'image/png'
						}
					]
				}
			})
		],
		server: {
			port: 3000,
			proxy: {
				'/TechnikTeam': {
					target: backendTarget,
					changeOrigin: true,
					secure: false,
				},
			},
		},
		resolve: {
			alias: {
				'@': fileURLToPath(new URL('./src', import.meta.url))
			}
		}
	}
})