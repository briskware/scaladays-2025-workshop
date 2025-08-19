import { defineConfig } from 'vite'

export default defineConfig({
	build: {
		target: 'es2015',
		rollupOptions: {
			external: [],
		}
	},
	optimizeDeps: {
		include: []
	},
	define: {
		global: 'globalThis',
	},
	server: {
		proxy: {
			'/api': {
				target: 'http://localhost:8080',
				changeOrigin: true,
				secure: false
			}
		},
		fs: {
			allow: ['..']
		}
	}
})