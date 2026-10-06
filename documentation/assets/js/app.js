(function () {
	'use strict';

	const TEXT = {
		minRead: 'min read',
		previous: 'Previous',
		next: 'Next',
		copy: 'Copy',
		copied: 'Copied',
		searchNoResults: 'No results',
		sourceLabel: 'Source',
		sourceTitle: 'View on GitHub',
		pdfLabel: 'PDF',
		pdfTitle: 'Download as PDF',
		tipLabel: 'Tip',
		noteLabel: 'Note',
		warningLabel: 'Warning',
		dangerLabel: 'Danger',
		docsTitle: 'JOID Documentation',
		docsSuffix: 'JOID Docs'
	};

	function t(key) {
		return TEXT[key] || key;
	}

	const state = {
		nav: null,
		flatPages: [],
		currentPath: null
	};

	async function loadNav() {
		const res = await fetch('nav.json');
		state.nav = await res.json();
		document.getElementById('brand-version').textContent = state.nav.version;
		state.flatPages = flattenPages(state.nav.sections);
		renderNav();
	}

	function flattenPages(sections, acc, section) {
		acc = acc || [];
		for (const s of sections) {
			if (s.path) {
				acc.push({
					title: s.title,
					path: s.path,
					section: section || s.title,
					source: s.source,
					refs: s.refs || []
				});
			}
			if (s.items) {
				flattenPages(s.items, acc, section || s.title);
			}
		}
		return acc;
	}

	function renderNav() {
		const root = document.getElementById('nav');
		root.innerHTML = '';
		for (const s of state.nav.sections) {
			root.appendChild(renderTopLevel(s));
		}
	}

	function renderTopLevel(node) {
		if (node.path && !node.items) {
			const wrap = document.createElement('div');
			wrap.className = 'nav-section';
			const a = document.createElement('a');
			a.className = 'nav-link';
			a.href = '#/' + node.path;
			a.textContent = node.title;
			a.dataset.path = node.path;
			wrap.appendChild(a);
			return wrap;
		}

		const grp = document.createElement('div');
		grp.className = 'nav-group';
		const title = document.createElement('div');
		title.className = 'nav-group-title';
		title.textContent = node.title;
		grp.appendChild(title);
		const items = document.createElement('div');
		items.className = 'nav-group-items';
		if (node.items) {
			for (const i of node.items) items.appendChild(renderItem(i, false));
		}
		grp.appendChild(items);
		return grp;
	}

	function renderItem(node, deep) {
		if (node.path && !node.items) {
			const a = document.createElement('a');
			a.className = 'nav-link' + (deep ? ' nav-link-deep' : '');
			a.href = '#/' + node.path;
			a.textContent = node.title;
			a.dataset.path = node.path;
			return a;
		}

		const wrap = document.createElement('div');
		wrap.className = 'nav-subgroup';
		const title = document.createElement('div');
		title.className = 'nav-subgroup-title';
		title.textContent = node.title;
		wrap.appendChild(title);
		const items = document.createElement('div');
		items.className = 'nav-subgroup-items';
		if (node.items) {
			for (const i of node.items) items.appendChild(renderItem(i, true));
		}
		wrap.appendChild(items);
		return wrap;
	}

	function highlightNav(path) {
		const links = document.querySelectorAll('.nav-link');
		links.forEach(l => {
			const match = l.dataset.path === path;
			l.classList.toggle('active', match);
			if (match) {
				l.scrollIntoView({ block: 'nearest' });
			}
		});
	}

	function parseHash() {
		const hash = window.location.hash || '#/getting-started/introduction';
		const [pagePart, anchor] = hash.slice(2).split('#');
		return { path: pagePart || 'getting-started/introduction', anchor };
	}

	async function fetchMarkdown(path) {
		const res = await fetch('content/' + path + '.md');
		if (!res.ok) throw new Error('Page not found: ' + path);
		return res.text();
	}

	async function loadPage(path) {
		const article = document.getElementById('article');
		article.innerHTML = '<div class="loading">Loading…</div>';
		try {
			const md = await fetchMarkdown(path);
			marked.setOptions({
				gfm: true,
				breaks: false,
				headerIds: true,
				mangle: false
			});
			const renderer = new marked.Renderer();
			const slugs = {};
			renderer.heading = function (text, level, raw) {
				const base = raw.toLowerCase().replace(/[^\w]+/g, '-').replace(/^-|-$/g, '');
				const slug = slugs[base] === undefined ? base : base + '-' + slugs[base];
				slugs[base] = (slugs[base] || 0) + 1;
				return '<h' + level + ' id="' + slug + '">' + text + '</h' + level + '>';
			};
			renderer.link = function (href, title, text) {
				let target = '';
				if (href && href.startsWith('http')) target = ' target="_blank" rel="noopener"';
				if (href && !href.startsWith('http') && !href.startsWith('#')) {
					const parts = path.split('/');
					parts.pop();
					let resolved = href.replace(/\.md$/, '');
					if (resolved.startsWith('../')) {
						while (resolved.startsWith('../')) {
							resolved = resolved.slice(3);
							parts.pop();
						}
						resolved = (parts.length ? parts.join('/') + '/' : '') + resolved;
					} else if (resolved.startsWith('./')) {
						resolved = (parts.length ? parts.join('/') + '/' : '') + resolved.slice(2);
					} else if (!resolved.includes('/')) {
						resolved = (parts.length ? parts.join('/') + '/' : '') + resolved;
					}
					href = '#/' + resolved;
				}
				return '<a href="' + href + '"' + target + (title ? ' title="' + title + '"' : '') + '>' + text + '</a>';
			};
			renderer.image = function (href, title, text) {
				let src = href;
				if (src && !/^(https?:|data:|\/)/.test(src)) {
					const parts = ('content/' + path).split('/');
					parts.pop();
					for (const part of src.split('/')) {
						if (part === '..') parts.pop();
						else if (part !== '.') parts.push(part);
					}
					src = parts.join('/');
				}
				const img = /\.(mp4|webm)$/i.test(src)
					? '<video src="' + src + '" aria-label="' + (text || '') + '" autoplay loop muted playsinline></video>'
					: '<img src="' + src + '" alt="' + (text || '') + '" loading="lazy">';
				return '<figure class="render">' + img + (title ? '<figcaption>' + title + '</figcaption>' : '') + '</figure>';
			};
			const html = marked.parse(md, { renderer });
			article.innerHTML = html;
			state.currentPath = path;
			highlightNav(path);
			injectPageMeta(article, path);
			autolinkReferences(article, path);
			enhanceArticle(article);
			buildPageNav(path);
			Prism.highlightAllUnder(article);
			const parsed = parseHash();
			if (parsed.anchor) {
				const el = document.getElementById(parsed.anchor);
				if (el) {
					setTimeout(() => el.scrollIntoView({ behavior: 'smooth' }), 60);
				}
			} else {
				window.scrollTo({ top: 0, behavior: 'instant' });
			}
			document.title = article.querySelector('h1')
				? article.querySelector('h1').textContent + ' · ' + t('docsSuffix')
				: t('docsTitle');
		} catch (err) {
			article.innerHTML = '<div class="error">' + err.message + '</div>';
		}
	}

	function buildRefMap() {
		const map = {};
		state.flatPages.forEach(p => {
			map[p.title] = p.path;
			p.refs.forEach(ref => { map[ref] = p.path; });
		});
		return map;
	}

	function autolinkReferences(article, currentPath) {
		const refMap = buildRefMap();
		article.querySelectorAll('code').forEach(code => {
			if (code.closest('pre')) return;
			if (code.closest('a')) return;
			const raw = code.textContent.trim();
			const key = raw.replace(/<[^>]+>/g, '').replace(/[()\[\]]/g, '').trim();
			const target = refMap[key];
			if (!target || target === currentPath) return;
			const a = document.createElement('a');
			a.href = '#/' + target;
			a.className = 'auto-ref';
			code.parentNode.insertBefore(a, code);
			a.appendChild(code);
		});
	}

	function exportCurrentPageAsPdf(article, path) {
		if (!window.html2pdf) return;
		const slug = (path || 'page')
			.toLowerCase()
			.replace(/[^a-z0-9/]+/g, '-')
			.replace(/\/+/g, '-')
			.replace(/^-|-$/g, '') || 'page';
		const version = (state.nav && state.nav.version) ? state.nav.version : '';
		const versionPart = version ? '-v' + version : '';
		const filename = 'joid-docs-' + slug + versionPart + '.pdf';
		const clone = article.cloneNode(true);
		clone.querySelectorAll('.copy-btn, .source-link, .pdf-link').forEach(el => el.remove());
		const wrapper = document.createElement('div');
		wrapper.className = 'markdown pdf-render';
		wrapper.appendChild(clone);
		html2pdf()
			.from(wrapper)
			.set({
				margin: [14, 12, 14, 12],
				filename: filename,
				image: { type: 'jpeg', quality: 0.98 },
				html2canvas: {
					scale: 2,
					backgroundColor: '#ffffff',
					useCORS: true,
					logging: false,
					onclone: (doc) => doc.documentElement.classList.add('pdf-render')
				},
				jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait', compress: true },
				pagebreak: { mode: ['css', 'legacy'] }
			})
			.save();
	}

	const GITHUB_REPO = 'https://github.com/Zeldown/JOID';
	const GITHUB_BRANCH = 'main';

	function sourceUrlFor(path) {
		const page = state.flatPages.find(p => p.path === path);
		if (!page || !page.source) return null;
		return GITHUB_REPO + '/blob/' + GITHUB_BRANCH + '/' + page.source;
	}

	function injectPageMeta(article, path) {
		const h1 = article.querySelector('h1');
		if (!h1) return;
		const clone = article.cloneNode(true);
		let codeLines = 0;
		clone.querySelectorAll('pre').forEach(pre => {
			codeLines += pre.textContent.split('\n').filter(l => l.trim().length).length;
			pre.remove();
		});
		const text = clone.textContent || '';
		const words = text.trim().split(/\s+/).filter(Boolean).length;
		const textSeconds = (words / 150) * 60;
		const codeSeconds = codeLines * 8;
		const totalMinutes = Math.max(1, Math.round((textSeconds + codeSeconds) / 60));

		const wrap = document.createElement('div');
		wrap.className = 'page-meta';

		const readTime = document.createElement('span');
		readTime.className = 'read-time';
		readTime.innerHTML = '<svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>' + totalMinutes + ' ' + t('minRead');
		wrap.appendChild(readTime);

		const sourceUrl = sourceUrlFor(path);
		if (sourceUrl) {
			const link = document.createElement('a');
			link.className = 'source-link';
			link.href = sourceUrl;
			link.target = '_blank';
			link.rel = 'noopener';
			link.title = t('sourceTitle');
			link.innerHTML = '<svg width="11" height="11" viewBox="0 0 16 16" fill="currentColor"><path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2 .37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27s1.36.09 2 .27c1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.01 8.01 0 0 0 16 8c0-4.42-3.58-8-8-8z"/></svg>' + t('sourceLabel');
			wrap.appendChild(link);
		}

		const pdfBtn = document.createElement('button');
		pdfBtn.type = 'button';
		pdfBtn.className = 'pdf-link';
		pdfBtn.title = t('pdfTitle');
		pdfBtn.innerHTML = '<svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>' + t('pdfLabel');
		pdfBtn.addEventListener('click', () => exportCurrentPageAsPdf(article, path));
		wrap.appendChild(pdfBtn);

		h1.insertAdjacentElement('afterend', wrap);
	}

	function enhanceArticle(article) {
		article.querySelectorAll('pre > code').forEach(code => {
			const pre = code.parentElement;
			const btn = document.createElement('button');
			btn.className = 'copy-btn';
			btn.type = 'button';
			btn.textContent = t('copy');
			btn.addEventListener('click', async () => {
				try {
					await navigator.clipboard.writeText(code.textContent);
					btn.textContent = t('copied');
					btn.classList.add('copied');
					setTimeout(() => {
						btn.textContent = t('copy');
						btn.classList.remove('copied');
					}, 1500);
				} catch (e) {}
			});
			pre.appendChild(btn);
		});

		article.querySelectorAll('blockquote').forEach(bq => {
			const firstP = bq.querySelector('p');
			if (!firstP) return;
			const txt = firstP.textContent.trim();
			const map = {
				'TIP': { cls: 'tip', label: t('tipLabel') },
				'NOTE': { cls: 'info', label: t('noteLabel') },
				'INFO': { cls: 'info', label: t('noteLabel') },
				'WARNING': { cls: 'warning', label: t('warningLabel') },
				'WARN': { cls: 'warning', label: t('warningLabel') },
				'DANGER': { cls: 'danger', label: t('dangerLabel') }
			};
			for (const key in map) {
				if (txt.startsWith(key + ':') || txt.startsWith('[' + key + ']')) {
					const entry = map[key];
					bq.className = 'callout ' + entry.cls;
					firstP.innerHTML = firstP.innerHTML.replace(
						new RegExp('^(\\[' + key + '\\]|' + key + ':)\\s*', 'i'),
						'<strong>' + entry.label + '.</strong> '
					);
					break;
				}
			}
		});
	}

	function buildPageNav(path) {
		const wrap = document.getElementById('page-nav');
		const idx = state.flatPages.findIndex(p => p.path === path);
		if (idx === -1) { wrap.innerHTML = ''; return; }
		const prev = state.flatPages[idx - 1];
		const next = state.flatPages[idx + 1];
		wrap.innerHTML = '';
		if (prev) {
			const a = document.createElement('a');
			a.className = 'prev';
			a.href = '#/' + prev.path;
			a.innerHTML = '<div class="page-nav-label">← ' + t('previous') + '</div><div class="page-nav-title">' + prev.title + '</div>';
			wrap.appendChild(a);
		} else {
			wrap.appendChild(document.createElement('span'));
		}
		if (next) {
			const a = document.createElement('a');
			a.className = 'next';
			a.href = '#/' + next.path;
			a.innerHTML = '<div class="page-nav-label">' + t('next') + ' →</div><div class="page-nav-title">' + next.title + '</div>';
			wrap.appendChild(a);
		}
	}

	function onHashChange() {
		const { path } = parseHash();
		if (path !== state.currentPath) {
			loadPage(path);
		} else {
			const { anchor } = parseHash();
			if (anchor) {
				const el = document.getElementById(anchor);
				if (el) el.scrollIntoView({ behavior: 'smooth' });
			}
		}
	}

	function setupSidebarToggle() {
		const btn = document.getElementById('sidebar-toggle');
		const sidebar = document.querySelector('.sidebar');
		btn.addEventListener('click', () => sidebar.classList.toggle('open'));
		document.addEventListener('click', (e) => {
			if (window.innerWidth > 900) return;
			if (!sidebar.contains(e.target) && e.target !== btn) {
				sidebar.classList.remove('open');
			}
		});
		sidebar.addEventListener('wheel', (e) => {
			const nav = sidebar.querySelector('.nav');
			if (!nav) return;
			nav.scrollTop += e.deltaY;
			e.preventDefault();
		}, { passive: false });
	}

	window.JOID_DOCS = { state, loadPage, t };

	(async function init() {
		setupSidebarToggle();
		await loadNav();
		const { path } = parseHash();
		await loadPage(path);
		window.addEventListener('hashchange', onHashChange);
	})();
})();
