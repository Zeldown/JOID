import { readFileSync } from 'node:fs';
import { defineConfig } from 'astro/config';
import { unified } from '@astrojs/markdown-remark';
import starlight from '@astrojs/starlight';
import starlightLinksValidator from 'starlight-links-validator';
import starlightSidebarTopics from 'starlight-sidebar-topics';
import remarkJoid from './plugins/remark-joid.mjs';
import { renamedFamilyRedirects, writeLegacyRedirectScript } from './plugins/legacy-redirects.mjs';
import { topics } from './src/navigation.mjs';

const reference = JSON.parse(readFileSync(new URL('./src/generated/reference-sidebar.json', import.meta.url), 'utf-8'));

export default defineConfig({
	site: 'https://documentation.joid.dev',
	trailingSlash: 'always',
	redirects: renamedFamilyRedirects(),
	markdown: {
		processor: unified({ remarkPlugins: [remarkJoid] }),
	},
	integrations: [
		starlight({
			title: 'JOID',
			description: 'JOID builds interfaces in Java on a design canvas, rendered by OpenGL or Vulkan.',
			logo: { src: './src/assets/logo.png', replacesTitle: false },
			favicon: '/favicon-32.png',
			social: [
				{ icon: 'github', label: 'GitHub', href: 'https://github.com/Zeldown/JOID' },
			],
			editLink: { baseUrl: 'https://github.com/Zeldown/JOID/edit/main/documentation/' },
			lastUpdated: false,
			pagination: true,
			tableOfContents: { minHeadingLevel: 2, maxHeadingLevel: 3 },
			customCss: ['./src/styles/joid.css'],
			head: [
				{ tag: 'script', attrs: { src: writeLegacyRedirectScript(), defer: true } },
				{ tag: 'link', attrs: { rel: 'icon', type: 'image/png', sizes: '16x16', href: '/favicon-16.png' } },
				{ tag: 'link', attrs: { rel: 'apple-touch-icon', sizes: '180x180', href: '/apple-touch-icon.png' } },
				{ tag: 'meta', attrs: { property: 'og:image', content: 'https://documentation.joid.dev/og.png' } },
				{ tag: 'meta', attrs: { name: 'twitter:card', content: 'summary_large_image' } },
			],
			components: { Footer: './src/components/Footer.astro' },
			routeMiddleware: './src/sidebar.ts',
			expressiveCode: {
				themes: ['github-dark', 'github-light'],
				defaultProps: { wrap: false },
				styleOverrides: { codeFontSize: '0.85rem' },
			},
			plugins: [
				starlightSidebarTopics(
					topics(reference),
					{ exclude: ['/', '/404', '/404/'] },
				),
				starlightLinksValidator({ errorOnInvalidHashes: true, errorOnLocalLinks: true }),
			],
		}),
	],
});