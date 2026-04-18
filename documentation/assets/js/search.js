(function () {
	'use strict';

	let index = null;
	let docs = {};
	let buildPromise = null;

	function waitForNav() {
		return new Promise(resolve => {
			const check = () => {
				if (window.JOID_DOCS && window.JOID_DOCS.state && window.JOID_DOCS.state.flatPages.length) {
					resolve();
				} else {
					setTimeout(check, 50);
				}
			};
			check();
		});
	}

	async function buildIndex() {
		await waitForNav();
		const pages = window.JOID_DOCS.state.flatPages;
		const rawDocs = [];
		for (const p of pages) {
			try {
				const res = await fetch('content/' + p.path + '.md');
				if (!res.ok) continue;
				const text = await res.text();
				const sections = splitIntoSections(text, p);
				for (const s of sections) rawDocs.push(s);
			} catch (e) {}
		}
		index = lunr(function () {
			this.ref('id');
			this.field('title', { boost: 10 });
			this.field('heading', { boost: 6 });
			this.field('body');
			rawDocs.forEach(d => this.add(d));
		});
		rawDocs.forEach(d => { docs[d.id] = d; });
	}

	function splitIntoSections(md, page) {
		const lines = md.split('\n');
		const sections = [];
		let currentTitle = page.title;
		let currentHeading = '';
		let currentAnchor = '';
		let currentBody = [];
		let inCode = false;
		const flush = () => {
			const bodyText = currentBody.join(' ')
				.replace(/```[\s\S]*?```/g, '')
				.replace(/`[^`]+`/g, '')
				.replace(/[#*_>\[\]\(\)]/g, ' ')
				.replace(/\s+/g, ' ')
				.trim();
			if (bodyText || currentHeading) {
				sections.push({
					id: page.path + (currentAnchor ? '#' + currentAnchor : '') + '::' + sections.length,
					path: page.path,
					anchor: currentAnchor,
					title: currentTitle,
					heading: currentHeading,
					section: page.section,
					body: bodyText
				});
			}
			currentBody = [];
		};
		for (const line of lines) {
			if (line.startsWith('```')) { inCode = !inCode; currentBody.push(line); continue; }
			if (inCode) { currentBody.push(line); continue; }
			const h = line.match(/^(#{1,4})\s+(.+?)\s*$/);
			if (h) {
				if (currentHeading || currentBody.length) flush();
				const level = h[1].length;
				const text = h[2].trim();
				if (level === 1) {
					currentTitle = text;
					currentHeading = '';
					currentAnchor = '';
				} else {
					currentHeading = text;
					currentAnchor = text.toLowerCase().replace(/[^\w]+/g, '-').replace(/^-|-$/g, '');
				}
				continue;
			}
			currentBody.push(line);
		}
		flush();
		return sections;
	}

	function ensureIndex() {
		if (buildPromise) return buildPromise;
		buildPromise = buildIndex();
		return buildPromise;
	}

	function highlight(text, terms) {
		if (!terms.length) return escapeHtml(text);
		let result = escapeHtml(text);
		for (const t of terms) {
			const re = new RegExp('(' + escapeRegex(t) + ')', 'gi');
			result = result.replace(re, '<mark>$1</mark>');
		}
		return result;
	}

	function escapeHtml(s) {
		return s.replace(/[&<>"']/g, c => ({
			'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
		}[c]));
	}

	function escapeRegex(s) { return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'); }

	function snippet(body, terms, max) {
		if (!body) return '';
		max = max || 140;
		if (!terms.length) return body.slice(0, max) + (body.length > max ? '…' : '');
		const lower = body.toLowerCase();
		let bestIdx = -1;
		for (const t of terms) {
			const idx = lower.indexOf(t.toLowerCase());
			if (idx !== -1 && (bestIdx === -1 || idx < bestIdx)) bestIdx = idx;
		}
		if (bestIdx === -1) return body.slice(0, max) + (body.length > max ? '…' : '');
		const start = Math.max(0, bestIdx - 40);
		const end = Math.min(body.length, bestIdx + max - 40);
		return (start > 0 ? '…' : '') + body.slice(start, end) + (end < body.length ? '…' : '');
	}

	function render(results, query) {
		const el = document.getElementById('search-results');
		const terms = query.trim().split(/\s+/).filter(Boolean);
		if (!results.length) {
			el.innerHTML = '<div class="search-result" style="cursor: default;"><div class="search-result-title" style="color: var(--text-3);">No results</div></div>';
			return;
		}
		el.innerHTML = '';
		results.slice(0, 12).forEach((r, i) => {
			const doc = docs[r.ref];
			if (!doc) return;
			const div = document.createElement('div');
			div.className = 'search-result';
			if (i === 0) div.classList.add('active');
			const target = '#/' + doc.path + (doc.anchor ? '#' + doc.anchor : '');
			div.dataset.href = target;
			const titleTxt = doc.title + (doc.heading ? ' · ' + doc.heading : '');
			const titleHtml = '<span>' + highlight(titleTxt, terms) + '</span>' +
				'<span class="search-result-section">' + escapeHtml(doc.section) + '</span>';
			const snipHtml = highlight(snippet(doc.body, terms), terms);
			div.innerHTML =
				'<div class="search-result-title">' + titleHtml + '</div>' +
				(snipHtml ? '<div class="search-result-snippet">' + snipHtml + '</div>' : '');
			div.addEventListener('click', () => {
				window.location.hash = target;
				closeModal();
			});
			el.appendChild(div);
		});
	}

	async function runQuery(q) {
		const el = document.getElementById('search-results');
		if (!q || q.length < 2) { el.innerHTML = ''; return; }
		await ensureIndex();
		if (!index) return;
		let results = [];
		try {
			results = index.search(q);
			if (!results.length) {
				const fuzzy = q.split(/\s+/).map(t => t + '*').join(' ');
				results = index.search(fuzzy);
			}
		} catch (e) {
			try { results = index.search(q.split(/\s+/).map(t => t + '*').join(' ')); }
			catch (e2) {}
		}
		render(results, q);
	}

	function openModal() {
		const modal = document.getElementById('search-modal');
		const input = document.getElementById('search-input');
		modal.classList.add('open');
		modal.setAttribute('aria-hidden', 'false');
		setTimeout(() => input.focus(), 10);
		ensureIndex();
		if (input.value) runQuery(input.value);
	}

	function closeModal() {
		const modal = document.getElementById('search-modal');
		modal.classList.remove('open');
		modal.setAttribute('aria-hidden', 'true');
	}

	function setupModal() {
		const trigger = document.getElementById('search-trigger');
		const backdrop = document.getElementById('search-backdrop');
		const input = document.getElementById('search-input');
		const resultsEl = document.getElementById('search-results');

		trigger.addEventListener('click', openModal);
		backdrop.addEventListener('click', closeModal);

		document.addEventListener('keydown', (e) => {
			if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
				e.preventDefault();
				openModal();
				return;
			}
			if (e.key === 'Escape') {
				const modal = document.getElementById('search-modal');
				if (modal.classList.contains('open')) {
					e.preventDefault();
					closeModal();
				}
			}
		});

		let debounce;
		input.addEventListener('input', () => {
			clearTimeout(debounce);
			debounce = setTimeout(() => runQuery(input.value), 120);
		});
		input.addEventListener('keydown', (e) => {
			const active = resultsEl.querySelector('.search-result.active');
			if (e.key === 'ArrowDown') {
				e.preventDefault();
				if (active && active.nextElementSibling) {
					active.classList.remove('active');
					active.nextElementSibling.classList.add('active');
					active.nextElementSibling.scrollIntoView({ block: 'nearest' });
				} else if (!active && resultsEl.firstElementChild) {
					resultsEl.firstElementChild.classList.add('active');
				}
			} else if (e.key === 'ArrowUp') {
				e.preventDefault();
				if (active && active.previousElementSibling) {
					active.classList.remove('active');
					active.previousElementSibling.classList.add('active');
					active.previousElementSibling.scrollIntoView({ block: 'nearest' });
				}
			} else if (e.key === 'Enter') {
				e.preventDefault();
				if (active && active.dataset.href) {
					window.location.hash = active.dataset.href;
					closeModal();
				}
			}
		});
	}

	if (document.readyState === 'loading') {
		document.addEventListener('DOMContentLoaded', setupModal);
	} else {
		setupModal();
	}
})();
