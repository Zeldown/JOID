(function () {
	'use strict';

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
					section: section || s.title
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
		const hash = window.location.hash || '#/introduction';
		const [pagePart, anchor] = hash.slice(2).split('#');
		return { path: pagePart || 'introduction', anchor };
	}

	async function loadPage(path) {
		const article = document.getElementById('article');
		article.innerHTML = '<div class="loading">Loading…</div>';
		try {
			const res = await fetch('content/' + path + '.md');
			if (!res.ok) throw new Error('Page not found: ' + path);
			const md = await res.text();
			marked.setOptions({
				gfm: true,
				breaks: false,
				headerIds: true,
				mangle: false
			});
			const renderer = new marked.Renderer();
			renderer.heading = function (text, level, raw) {
				const slug = raw.toLowerCase().replace(/[^\w]+/g, '-').replace(/^-|-$/g, '');
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
			const html = marked.parse(md, { renderer });
			article.innerHTML = html;
			state.currentPath = path;
			highlightNav(path);
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
				? article.querySelector('h1').textContent + ' · JOID Docs'
				: 'JOID Documentation';
		} catch (err) {
			article.innerHTML = '<div class="error">' + err.message + '</div>';
		}
	}

	function enhanceArticle(article) {
		article.querySelectorAll('pre > code').forEach(code => {
			const pre = code.parentElement;
			const btn = document.createElement('button');
			btn.className = 'copy-btn';
			btn.type = 'button';
			btn.textContent = 'Copy';
			btn.addEventListener('click', async () => {
				try {
					await navigator.clipboard.writeText(code.textContent);
					btn.textContent = 'Copied';
					btn.classList.add('copied');
					setTimeout(() => {
						btn.textContent = 'Copy';
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
				'TIP': 'tip',
				'NOTE': 'info',
				'INFO': 'info',
				'WARNING': 'warning',
				'WARN': 'warning',
				'DANGER': 'danger'
			};
			for (const key in map) {
				if (txt.startsWith(key + ':') || txt.startsWith('[' + key + ']')) {
					bq.className = 'callout ' + map[key];
					firstP.innerHTML = firstP.innerHTML.replace(
						new RegExp('^(\\[' + key + '\\]|' + key + ':)\\s*', 'i'), ''
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
			a.innerHTML = '<div class="page-nav-label">← Previous</div><div class="page-nav-title">' + prev.title + '</div>';
			wrap.appendChild(a);
		} else {
			wrap.appendChild(document.createElement('span'));
		}
		if (next) {
			const a = document.createElement('a');
			a.className = 'next';
			a.href = '#/' + next.path;
			a.innerHTML = '<div class="page-nav-label">Next →</div><div class="page-nav-title">' + next.title + '</div>';
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
	}

	window.JOID_DOCS = { state, loadPage };

	(async function init() {
		setupSidebarToggle();
		await loadNav();
		const { path } = parseHash();
		await loadPage(path);
		window.addEventListener('hashchange', onHashChange);
	})();
})();
