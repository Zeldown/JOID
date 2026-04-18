(function () {
	'use strict';

	const DEFAULT_LANG = 'en';
	const SUPPORTED_LANGS = ['en', 'fr'];

	const i18n = {
		en: {
			minRead: 'min read',
			previous: 'Previous',
			next: 'Next',
			copy: 'Copy',
			copied: 'Copied',
			searchPlaceholder: 'Search the documentation…',
			searchNoResults: 'No results',
			searchHintOpen: 'to open',
			searchHintNavigate: 'to navigate',
			searchHintClose: 'to close',
			searchEmpty: 'Type to search the documentation…',
			missingTranslation: 'This page is not yet translated to French — showing the English version.',
			missingTranslationKicker: 'EN',
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
		},
		fr: {
			minRead: 'min de lecture',
			previous: 'Précédent',
			next: 'Suivant',
			copy: 'Copier',
			copied: 'Copié',
			searchPlaceholder: 'Rechercher dans la documentation…',
			searchNoResults: 'Aucun résultat',
			searchHintOpen: 'pour ouvrir',
			searchHintNavigate: 'pour naviguer',
			searchHintClose: 'pour fermer',
			searchEmpty: 'Tapez pour rechercher dans la documentation…',
			missingTranslation: 'Cette page n\'est pas encore traduite en français — version anglaise affichée.',
			missingTranslationKicker: 'EN',
			sourceLabel: 'Source',
			sourceTitle: 'Voir sur GitHub',
			pdfLabel: 'PDF',
			pdfTitle: 'Télécharger en PDF',
			tipLabel: 'Astuce',
			noteLabel: 'Note',
			warningLabel: 'Attention',
			dangerLabel: 'Danger',
			docsTitle: 'JOID Documentation',
			docsSuffix: 'JOID Docs'
		}
	};

	function currentLang() {
		const stored = localStorage.getItem('joid-docs-lang');
		return SUPPORTED_LANGS.indexOf(stored) >= 0 ? stored : DEFAULT_LANG;
	}

	function t(key) {
		return (i18n[state.lang] || i18n[DEFAULT_LANG])[key] || i18n[DEFAULT_LANG][key] || key;
	}

	const state = {
		nav: null,
		flatPages: [],
		currentPath: null,
		lang: DEFAULT_LANG
	};
	state.lang = currentLang();

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
		const hash = window.location.hash || '#/getting-started/introduction';
		const [pagePart, anchor] = hash.slice(2).split('#');
		return { path: pagePart || 'getting-started/introduction', anchor };
	}

	async function fetchMarkdown(path) {
		let missingTranslation = false;
		if (state.lang !== DEFAULT_LANG) {
			const res = await fetch('content/' + path + '.' + state.lang + '.md');
			if (res.ok) return { md: await res.text(), missingTranslation };
			missingTranslation = true;
		}
		const res = await fetch('content/' + path + '.md');
		if (!res.ok) throw new Error('Page not found: ' + path);
		return { md: await res.text(), missingTranslation };
	}

	async function loadPage(path) {
		const article = document.getElementById('article');
		article.innerHTML = '<div class="loading">Loading…</div>';
		try {
			const { md, missingTranslation } = await fetchMarkdown(path);
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
			if (missingTranslation) injectTranslationBanner(article);
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
		});
		Object.assign(map, {
			'Signal': 'state/signals',
			'ListSignal': 'state/signals',
			'MapSignal': 'state/signals',
			'Watch': 'state/watch',
			'Store': 'state/stores',
			'UIStoreData': 'state/stores',
			'UI': 'ui/ui-class',
			'UIData': 'ui/ui-class',
			'Bridge': 'ui/bridge',
			'Node': 'nodes/node-fundamentals',
			'ShaderPipeline': 'shaders/pipeline',
			'ShaderPass': 'shaders/custom',
			'NodeEffect': 'effects/overview',
			'RoundedNodeEffect': 'effects/rounded',
			'CircleNodeEffect': 'effects/circle',
			'BlurNodeEffect': 'effects/blur',
			'BorderNodeEffect': 'effects/border',
			'GradientNodeEffect': 'effects/gradient',
			'TweenAnimator': 'animations/tween-animator',
			'TweenManager': 'animations/tween-animator',
			'Easing': 'animations/easing',
			'ResourceBuilder': 'resources/resource-builder',
			'Resource': 'resources/resource-builder',
			'ResourceDecoder': 'resources/decoders',
			'VideoResourceDecoder': 'resources/decoders',
			'ImageResourceDecoder': 'resources/decoders',
			'DrawUtils': 'drawing/draw-utils',
			'DrawShape': 'drawing/shapes',
			'DrawText': 'drawing/text',
			'DrawResource': 'drawing/resources',
			'DrawModel': 'drawing/models',
			'Text': 'drawing/text',
			'TextElement': 'drawing/text',
			'TextMode': 'drawing/text',
			'TextOverflow': 'drawing/text',
			'ITextModifier': 'drawing/text',
			'IDrawableModel': 'drawing/models',
			'Color': 'drawing/color',
			'MSDF': 'fonts/msdf-atlas',
			'CustomFontProvider': 'fonts/custom-font',
			'RectNode': 'nodes/design/rect',
			'CircleNode': 'nodes/design/circle',
			'TextNode': 'nodes/design/text',
			'ResourceNode': 'nodes/design/resource',
			'TextFieldNode': 'nodes/design/text-field',
			'MultilineTextFieldNode': 'nodes/design/multiline-text-field',
			'ProgressNode': 'nodes/design/progress',
			'ModelNode': 'nodes/design/model',
			'VideoPlayerNode': 'nodes/design/video-player',
			'ContainerNode': 'nodes/structure/container',
			'FlexNode': 'nodes/structure/flex',
			'GridNode': 'nodes/structure/grid',
			'ScrollbarNode': 'nodes/structure/scrollbar',
			'SliderNode': 'nodes/structure/slider',
			'CheckboxNode': 'nodes/structure/checkbox',
			'ToggleNode': 'nodes/structure/toggle',
			'SwitchNode': 'nodes/structure/switch',
			'SelectorNode': 'nodes/structure/selector',
			'ChartNode': 'nodes/structure/chart'
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
		const langSuffix = state.lang && state.lang !== DEFAULT_LANG ? '.' + state.lang : '';
		const filename = 'joid-docs-' + slug + versionPart + langSuffix + '.pdf';
		const clone = article.cloneNode(true);
		clone.querySelectorAll('.copy-btn, .source-link, .pdf-link, .translation-missing').forEach(el => el.remove());
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

	function injectTranslationBanner(article) {
		const el = document.createElement('div');
		el.className = 'translation-missing';
		el.innerHTML = '<strong>' + t('missingTranslationKicker') + '</strong>' + t('missingTranslation');
		article.insertBefore(el, article.firstChild);
	}

	const GITHUB_REPO = 'https://github.com/Zeldown/JOID';
	const GITHUB_BRANCH = 'main';
	const GITHUB_SOURCE_ROOT = 'src/main/java/be/zeldown/joid';

	const pathToSource = {
		'ui/ui-class': 'lib/ui/core/UI.java',
		'ui/bridge': 'lib/ui/bridge/UIBridge.java',
		'ui/transitions': 'lib/ui/core/transition/Transition.java',
		'nodes/node-fundamentals': 'lib/ui/node/Node.java',
		'nodes/design/rect': 'lib/ui/node/impl/design/shape/RectNode.java',
		'nodes/design/circle': 'lib/ui/node/impl/design/shape/CircleNode.java',
		'nodes/design/text': 'lib/ui/node/impl/design/text/TextNode.java',
		'nodes/design/resource': 'lib/ui/node/impl/design/resource/ResourceNode.java',
		'nodes/design/text-field': 'lib/ui/node/impl/design/textfield/TextFieldNode.java',
		'nodes/design/multiline-text-field': 'lib/ui/node/impl/design/textfield/MultilineTextFieldNode.java',
		'nodes/design/progress': 'lib/ui/node/impl/design/progress/ProgressNode.java',
		'nodes/design/model': 'lib/ui/node/impl/design/model/ModelNode.java',
		'nodes/design/video-player': 'lib/ui/node/impl/design/video/VideoPlayerNode.java',
		'nodes/structure/container': 'lib/ui/node/impl/structure/container/ContainerNode.java',
		'nodes/structure/flex': 'lib/ui/node/impl/structure/flex/FlexNode.java',
		'nodes/structure/grid': 'lib/ui/node/impl/structure/grid/GridNode.java',
		'nodes/structure/scrollbar': 'lib/ui/node/impl/structure/scrollbar/ScrollbarNode.java',
		'nodes/structure/slider': 'lib/ui/node/impl/structure/slider/SliderNode.java',
		'nodes/structure/checkbox': 'lib/ui/node/impl/structure/checkbox/CheckboxNode.java',
		'nodes/structure/toggle': 'lib/ui/node/impl/structure/toggle/ToggleNode.java',
		'nodes/structure/switch': 'lib/ui/node/impl/structure/sw/SwitchNode.java',
		'nodes/structure/selector': 'lib/ui/node/impl/structure/selector/SelectorNode.java',
		'nodes/structure/chart': 'lib/ui/node/impl/structure/chart/ChartNode.java',
		'effects/overview': 'lib/ui/node/effect/NodeEffect.java',
		'effects/rounded': 'lib/ui/node/effect/impl/RoundedNodeEffect.java',
		'effects/circle': 'lib/ui/node/effect/impl/CircleNodeEffect.java',
		'effects/blur': 'lib/ui/node/effect/impl/BlurNodeEffect.java',
		'effects/border': 'lib/ui/node/effect/impl/BorderNodeEffect.java',
		'effects/gradient': 'lib/ui/node/effect/impl/GradientNodeEffect.java',
		'shaders/pipeline': 'lib/shader/pipeline/ShaderPipeline.java',
		'shaders/custom': 'lib/shader/pipeline/ShaderPass.java',
		'animations/tween-animator': 'lib/animation/animator/TweenAnimator.java',
		'animations/easing': 'lib/animation/tweenengine/TweenEquations.java',
		'resources/resource-builder': 'lib/resource/ResourceBuilder.java',
		'resources/decoders': 'lib/resource/dto/decoder/ResourceDecoder.java',
		'state/signals': 'lib/utils/signal/Signal.java',
		'state/watch': 'lib/ui/node/property/watch/WatchProperty.java',
		'state/stores': 'lib/ui/core/hook/store/UIStore.java',
		'fonts/custom-font': 'lib/font/FontLoader.java',
		'drawing/draw-utils': 'lib/draw/DrawUtils.java',
		'drawing/shapes': 'lib/draw/shape/DrawShape.java',
		'drawing/text': 'lib/draw/text/DrawText.java',
		'drawing/resources': 'lib/draw/resource/DrawResource.java',
		'drawing/models': 'lib/draw/model/DrawModel.java',
		'drawing/color': 'lib/color/Color.java',
		'interactions/callbacks': 'lib/ui/node/callback/registry/NodeCallbackRegistry.java',
		'interactions/hover': 'lib/ui/node/hover/HoverElement.java',
		'interactions/drag-drop': 'lib/ui/node/property/draggable/DraggableProperty.java'
	};

	function sourceUrlFor(path) {
		const src = pathToSource[path];
		if (!src) return null;
		return GITHUB_REPO + '/blob/' + GITHUB_BRANCH + '/' + GITHUB_SOURCE_ROOT + '/' + src;
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

	function setupLangToggle() {
		const toggle = document.getElementById('lang-toggle');
		if (!toggle) return;
		const buttons = toggle.querySelectorAll('button[data-lang]');
		const refresh = () => {
			buttons.forEach(b => b.classList.toggle('active', b.dataset.lang === state.lang));
		};
		refresh();
		buttons.forEach(b => {
			b.addEventListener('click', () => {
				const next = b.dataset.lang;
				if (next === state.lang) return;
				state.lang = next;
				localStorage.setItem('joid-docs-lang', next);
				refresh();
				applyStaticTranslations();
				if (state.currentPath) loadPage(state.currentPath);
			});
		});
	}

	function applyStaticTranslations() {
		const input = document.getElementById('search-input');
		if (input) input.placeholder = t('searchPlaceholder');
		document.documentElement.style.setProperty('--search-empty', '"' + t('searchEmpty') + '"');
		document.documentElement.lang = state.lang;
		document.querySelectorAll('[data-i18n]').forEach(el => {
			const key = el.dataset.i18n;
			if (key) el.textContent = t(key);
		});
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
		setupLangToggle();
		setupSidebarToggle();
		applyStaticTranslations();
		await loadNav();
		const { path } = parseHash();
		await loadPage(path);
		window.addEventListener('hashchange', onHashChange);
	})();
})();
