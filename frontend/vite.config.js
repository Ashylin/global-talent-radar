import {defineConfig} from 'vite';
import tailwindcss from '@tailwindcss/vite';
const proxy={'/api':{target:process.env.API_PROXY_TARGET || 'http://localhost:8080',changeOrigin:false}};
export default defineConfig({plugins:[tailwindcss()],server:{proxy},preview:{proxy}});

