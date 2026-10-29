import { existsSync, readFileSync } from 'node:fs';
import { visit, SKIP } from 'unist-util-visit';
import { filmMarkup } from './film.mjs';
import { readRegion, parseMeta, EXAMPLES, SOURCE } from './snippets.mjs';

const INDEX = new URL('../src/generated/reference-index.json', import.meta.url);
const CAPTURES = new URL('../public/captures/', import.meta.url);
const LINK = /\[\[([A-Za-z0-9_.$]+)(?:#([A-Za-z0-9_<>]+)(\([A-Za-z0-9_.$[\], ]*\))?)?\]\]/g;
const DRAFTS = !process.argv.includes('build') || process.env.JOID_DOCS_DRAFTS === '1';

function referenceIndex() {
	if (!existsSync(INDEX)) {
		return { types: {} };
	}
	return JSON.parse(readFileSync(INDEX, 'utf-8'));
}

function fail(file, node, message) {
	const where = node.position ? `${file.path}:${node.position.start.line}` : file.path;
	throw new Error(`[docs] ${where}: ${message}`);
}

function includeSnippets(tree, file) {
	const sources = [];
	visit(tree, 'code', (node, position, parent) => {
		const meta = parseMeta(node.meta);
		if (!meta.file) {
			if (meta.fragment) {
				node.meta = meta.rest;
			}
			return;
		}
		try {
			node.value = readRegion(meta.file, meta.region);
		} catch (error) {
			fail(file, node, error.message);
		}
		node.lang = node.lang || 'java';
		const title = meta.title === undefined ? '' : ` title="${meta.title}"`;
		node.meta = `${meta.rest}${title}`.trim();
		node.data = { ...node.data, hProperties: { 'data-snippet': `${meta.file}#${meta.region ?? ''}` } };
		if (meta.region && parent && position !== undefined) {
			sources.push({ parent, node, source: meta.file });
		}
	});
	for (const { parent, node, source } of sources) {
		const name = source.slice(source.lastIndexOf('/') + 1);
		parent.children.splice(parent.children.indexOf(node) + 1, 0, { type: 'html', value: `<p class="joid-source"><a href="${SOURCE}${source}">Full file: <code>${escape(name)}</code></a></p>` });
	}
}

function resolveLinks(tree, file) {
	const index = referenceIndex();
	visit(tree, 'text', (node, position, parent) => {
		if (!parent || position === undefined || !LINK.test(node.value)) {
			return;
		}
		LINK.lastIndex = 0;
		const children = [];
		let last = 0;
		for (const match of node.value.matchAll(LINK)) {
			const [raw, name, member, parameters] = match;
			const type = index.types[name];
			if (!type) {
				fail(file, node, `unknown reference type [[${name}]] (run the docgen, or check the name)`);
			}
			let url = type.url;
			let label = type.name;
			if (member) {
				const anchor = (parameters ? type.calls?.[member] : undefined) ?? type.members[member];
				if (!anchor) {
					fail(file, node, `unknown member [[${name}#${member}]] (known: ${Object.keys(type.members).slice(0, 12).join(', ')}…)`);
				}
				url += `#${anchor}`;
				label = `${type.name}.${member}`;
			}
			if (parameters) {
				const types = parameters.slice(1, -1).split(',').map((parameter) => parameter.replace(/\s+/g, '')).filter((parameter) => parameter !== '');
				const key = `${member}(${types.join(',')})`;
				if (!(type.overloads ?? []).includes(key)) {
					fail(file, node, `no overload ${raw} (known: ${(type.overloads ?? []).filter((overload) => overload === member || overload.startsWith(`${member}(`)).join(', ')})`);
				}
				label += parameters;
			}
			if (match.index > last) {
				children.push({ type: 'text', value: node.value.slice(last, match.index) });
			}
			children.push({ type: 'link', url, children: [{ type: 'inlineCode', value: label }] });
			last = match.index + raw.length;
		}
		if (last < node.value.length) {
			children.push({ type: 'text', value: node.value.slice(last) });
		}
		parent.children.splice(position, 1, ...children);
		return [SKIP, position + children.length];
	});
}

function draftNotes(tree) {
	visit(tree, 'containerDirective', (node, position, parent) => {
		if (node.name !== 'todo' || !parent || position === undefined) {
			return;
		}
		if (!DRAFTS) {
			parent.children.splice(position, 1);
			return [SKIP, position];
		}
		node.name = 'caution';
		const labelled = node.children[0]?.data?.directiveLabel;
		if (!labelled) {
			node.children.unshift({ type: 'paragraph', data: { directiveLabel: true }, children: [{ type: 'text', value: 'To write' }] });
		}
	});
}

function escape(text) {
	return String(text ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

function sourceAlts(tree, file) {
	const source = String(file.value ?? '');
	visit(tree, 'image', (node) => {
		const start = node.position?.start.offset;
		const end = node.position?.end.offset;
		if (start === undefined || end === undefined || !source.startsWith('![', start)) {
			return;
		}
		const raw = source.slice(start + 2, source.lastIndexOf('](', end));
		if (/:[A-Za-z0-9]/.test(raw) && !/[`*_[\]<]/.test(raw)) {
			node.alt = raw.replace(/\\([!-/:-@[-`{-~])/g, '$1').replace(/\s+/g, ' ');
		}
	});
}

function stillFrames(tree) {
	visit(tree, 'image', (node, position, parent) => {
		const match = node.url.match(/^\/captures\/([\w.@-]+)\.webp$/);
		if (!match || !parent || position === undefined || !existsSync(new URL(`${match[1]}.still.webp`, CAPTURES))) {
			return;
		}
		const title = node.title ? ` title="${escape(node.title)}"` : '';
		parent.children.splice(position, 1, { type: 'html', value: filmMarkup(node.url, `/captures/${match[1]}.still.webp`, `<img src="${node.url}" alt="${escape(node.alt)}"${title} loading="lazy" />`) });
	});
}

function captions(tree, processor) {
	visit(tree, 'html', (node, position, parent) => {
		const match = node.value.match(/^<figcaption>([\s\S]*?)<\/figcaption>(\s*<\/figure>)?\s*$/);
		if (!match || !parent || position === undefined || !/`|\]\(|\*|\[\[/.test(match[1])) {
			return;
		}
		const parsed = processor.parse(match[1].trim());
		const inline = parsed.children.flatMap((child, index) => (child.type === 'paragraph' ? child.children : [child]).concat(index < parsed.children.length - 1 ? [{ type: 'text', value: ' ' }] : []));
		const nodes = [{ type: 'html', value: '<figcaption>' }, { type: 'paragraph', data: { hName: 'span' }, children: inline }, { type: 'html', value: `</figcaption>${match[2] ?? ''}` }];
		parent.children.splice(position, 1, ...nodes);
		return [SKIP, position + nodes.length];
	});
}

function headingIds(tree) {
	visit(tree, 'heading', (node) => {
		const last = node.children[node.children.length - 1];
		const match = last?.type === 'text' ? last.value.match(/\s*\{#([a-z0-9-]+)\}$/) : null;
		if (!match) {
			return;
		}
		last.value = last.value.slice(0, match.index);
		node.data = { ...node.data, id: match[1], hProperties: { ...node.data?.hProperties, id: match[1] } };
	});
}

export default function remarkJoid() {
	const processor = this;
	return (tree, file) => {
		draftNotes(tree);
		headingIds(tree);
		captions(tree, processor);
		sourceAlts(tree, file);
		stillFrames(tree);
		includeSnippets(tree, file);
		resolveLinks(tree, file);
	};
}

export { EXAMPLES };