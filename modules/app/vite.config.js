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
		fs: {
			allow: ['..']
		}
	}
})