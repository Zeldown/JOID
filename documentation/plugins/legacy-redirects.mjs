import { existsSync, readdirSync, readFileSync, writeFileSync } from 'node:fs';

const RENAMED_FAMILIES = { 'backends-use': 'backends' };
const REDIRECTS = new URL('../src/legacy-redirects.json', import.meta.url);
const INDEX = new URL('../src/generated/reference-index.json', import.meta.url);
const SCRIPT = new URL('../public/legacy-redirects.js', import.meta.url);

export function legacyRedirects() {
	const redirects = JSON.parse(readFileSync(REDIRECTS, 'utf-8'));
	const index = existsSync(INDEX) ? JSON.parse(readFileSync(INDEX, 'utf-8')) : { types: {} };
	const resolved = {};
	for (const [legacy, target] of Object.entries(redirects)) {
		if (target.startsWith('@')) {
			const type = index.types[target.slice(1)];
			resolved[legacy] = type ? type.url : '/reference/';
		} else {
			resolved[legacy] = target;
		}
	}
	return resolved;
}

export function renamedFamilyRedirects() {
	const redirects = {};
	for (const [legacy, family] of Object.entries(RENAMED_FAMILIES)) {
		const directory = new URL(`../src/content/docs/reference/${family}/`, import.meta.url);
		if (!existsSync(directory)) {
			continue;
		}
		for (const name of readdirSync(directory).filter((file) => /\.mdx?$/.test(file))) {
			const page = name.replace(/(?:^index)?\.mdx?$/, '');
			const path = page === '' ? '' : `${page}/`;
			redirects[`/reference/${legacy}/${path}`] = `/reference/${family}/${path}`;
		}
	}
	return redirects;
}

export function legacyRedirectScript() {
	const map = JSON.stringify(legacyRedirects());
	return `(function(){var h=location.hash;if(location.pathname!=='/'||h.indexOf('#/')!==0)return;var p=h.slice(2).split('#')[0].replace(/[/]$/,'');var m=${map};location.replace(m[p]||'/use/');})();`;
}

export function writeLegacyRedirectScript() {
	writeFileSync(SCRIPT, legacyRedirectScript());
	return '/legacy-redirects.js';
}
