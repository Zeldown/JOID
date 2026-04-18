(function () {
	'use strict';

	const state = {
		nav: null,
		flatPages: [],
		currentPath: null,
		tocObserver: null
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
			root.appendChild(renderNode(s));
		}
	}

	function renderNode(node) {
		if (node.path && !node.items) {
			const a = document.createElement('a');
			a.className = 'nav-link';
			a.href = '#/' + node.path;
			a.textContent = node.title;
			a.dataset.path = node.path;
			const wrap = document.createElement('div');
			wrap.className = 'nav-section';
			wrap.appendChild(a);
			return wrap;
		}

		if (node.items) {
			if (node.group) {
				const grp = document.createElement('div');
				grp.className = 'nav-group';
				const title = document.createElement('div');
				title.className = 'nav-group-title';
				title.textContent = node.title;
				title.addEventListener('click', () => grp.classList.toggle('open'));
				grp.appendChild(title);
				const items = document.createElement('div');
				items.className = 'nav-group-items';
				for (const i of node.items) items.appendChild(renderNode(i));
				grp.appendChild(items);
				return grp;
			}
		}

		const section = document.createElement('div');
		section.className = 'nav-section';
		const sTitle = document.createElement('div');
		sTitle.className = 'nav-section-title';
		sTitle.textContent = node.title;
		section.appendChild(sTitle);
		if (node.items) {
			for (const i of node.items) section.appendChild(renderNode(i));
		}
		return section;
	}

	function highlightNav(path) {
		const links = document.querySelectorAll('.nav-link');
		links.forEach(l => {
			const match = l.dataset.path === path;
			l.classList.toggle('active', match);
			if (match) {
				let el = l.parentElement;
				while (el) {
					if (el.classList && el.classList.contains('nav-group')) {
						el.classList.add('open');
					}
					el = el.parentElement;
				}
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
				return '<a href="' + href + '"' + target + (title ? ' title="' + title + '"' : '') + '>' + text + '</a>';
			};
			const html = marked.parse(md, { renderer });
			article.innerHTML = html;
			state.currentPath = path;
			highlightNav(path);
			enhanceArticle(article);
			buildTOC(article);
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

	function buildTOC(article) {
		const tocEl = document.getElementById('toc');
		const headings = article.querySelectorAll('h2, h3');
		if (!headings.length) {
			tocEl.innerHTML = '';
			return;
		}
		const list = document.createElement('ul');
		list.className = 'toc-list';
		headings.forEach(h => {
			const li = document.createElement('li');
			li.className = 'toc-' + h.tagName.toLowerCase();
			const a = document.createElement('a');
			a.href = '#/' + state.currentPath + '#' + h.id;
			a.textContent = h.textContent;
			a.dataset.target = h.id;
			li.appendChild(a);
			list.appendChild(li);
		});
		tocEl.innerHTML = '<div class="toc-title">On this page</div>';
		tocEl.appendChild(list);

		if (state.tocObserver) state.tocObserver.disconnect();
		state.tocObserver = new IntersectionObserver(entries => {
			entries.forEach(e => {
				if (e.isIntersecting) {
					document.querySelectorAll('.toc-list a').forEach(a => {
						a.classList.toggle('active', a.dataset.target === e.target.id);
					});
				}
			});
		}, { rootMargin: '-10% 0px -80% 0px' });
		headings.forEach(h => state.tocObserver.observe(h));
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
