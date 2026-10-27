import { execSync } from 'node:child_process';
import { existsSync, readFileSync, readdirSync, rmSync, statSync } from 'node:fs';
import { basename, join, relative } from 'node:path';
import { fileURLToPath } from 'node:url';
import { gradle, ROOT, withLock } from './gradle.mjs';
import { EXAMPLES, parseMeta, readRegion } from '../plugins/snippets.mjs';
import { checkCatalog } from './catalog.mjs';
import { glossaryTerms } from './glossary.mjs';

const DOCUMENTATION = fileURLToPath(new URL('..', import.meta.url));
const DOCS = join(DOCUMENTATION, 'src', 'content', 'docs');
const DESCRIPTIONS = join(DOCUMENTATION, 'reference');
const DIST = join(DOCUMENTATION, 'dist');
const BANNED = ['simply', 'just', 'easy', 'easily', 'obviously', 'basically', 'actually', 'currently', 'of course', "it's worth noting", 'note that', 'please'];
const PHRASES = [
	{ pattern: /\b(?:a|the|is|are|our) new (?:feature|api|method|class|option|setting|way|system|node|control)\b|\bnew in \d|\bnewly\b|\bbrand new\b/i, reason: '"new" for a feature' },
	{ pattern: /\bwe\b|\bwe'(?:re|ll|ve)\b|\blet's\b/i, reason: '"we" for the reader' },
	{ pattern: /\bsince (?:v?\d|version|JOID)|\bpreviously\b|\bno longer\b|\bformerly\b|\bused to\b|\blegacy\b|\bmigrat\w* (?:from|to) (?:v?\d|JOID)|\bchangelog\b/i, reason: 'history (the documentation describes the current version, it has no changelog)' },
	{ pattern: /\bsuppliers? (?:\w+ )?read on every frame\b|\b(?:supplier|overload) follows (?:the|its) value on every frame\b/i, reason: 'a Supplier follows the signals it reads (signals, "What JOID follows"): only a lambda is computed on every frame' },
];
const FORBIDDEN = { pattern: /\bJOID-[A-Z][A-Za-z0-9]*\b|\b[Mm][Ii][Nn][Ee][Cc][Rr][Aa][Ff][Tt]\b/, reason: 'the documentation never names an external project built on JOID, nor a game or a host it runs in' };
const DEMOS = { pattern: /\bdemos?\b|\bUIDemo\w*|\bDemo[A-Z]\w*|runDemo/i, reason: 'Use JOID never mentions the demos (they belong to /build/demos/)' };
const LOMBOK = { pattern: /lombok/i, reason: 'Lombok is never required: the examples are plain Java and the pages never mention it' };
const OWNERS = join(DOCUMENTATION, 'rules', 'owners.md');
const INDEX = join(DOCUMENTATION, 'src', 'generated', 'reference-index.json');
const FIRST_MENTION_EXEMPT = /^(?:use\/catalog|reference\/(?!glossary$).*)$/;
const CODE_KEYWORDS = new Set(['new', 'final', 'extends', 'implements', 'instanceof', 'return', 'throws', 'static', 'public', 'protected', 'private', 'abstract', 'void', 'int', 'long', 'float', 'double', 'boolean', 'char', 'byte', 'short', 'var', 'class', 'interface', 'enum', 'throw', 'case', 'default', 'super', 'this']);
const OWNER_EXEMPT = /^(?:use\/get-started\/quick-start|use\/tutorial\/.*|.*\/index|use\/catalog|reference\/.*)$/;
const WRITING = new Set(['coverage']);
const FENCE = /^(\s*)(`{3,}|~{3,})(\S*)\s*(.*)$/;
const LINE_WIDTH = 120;
const TAB_WIDTH = 4;

const args = new Set(process.argv.slice(2));
const skipped = new Set([...args].filter((arg) => arg.startsWith('--skip=')).flatMap((arg) => arg.slice(7).split(',')));
const results = [];

function files(directory, extensions) {
	if (!existsSync(directory)) {
		return [];
	}
	return readdirSync(directory).flatMap((name) => {
		const path = join(directory, name);
		if (statSync(path).isDirectory()) {
			return files(path, extensions);
		}
		return extensions.some((extension) => name.endsWith(extension)) ? [path] : [];
	});
}

function run(name, check) {
	if (skipped.has(name)) {
		results.push({ name, status: 'skipped', problems: [] });
		return;
	}
	process.stdout.write(`\n▶ ${name}\n`);
	try {
		const problems = check() ?? [];
		const failed = args.has('--draft') && WRITING.has(name) ? 'warned' : 'failed';
		results.push({ name, status: problems.length === 0 ? 'ok' : failed, problems });
	} catch (error) {
		results.push({ name, status: 'failed', problems: [error.message.split('\n').slice(0, 20).join('\n')] });
	}
}

function codeBlocks(source) {
	const blocks = [];
	const lines = source.split(/\r?\n/);
	let open = null;
	lines.forEach((line, index) => {
		const match = line.match(FENCE);
		if (!open && match) {
			open = { fence: match[2], lang: match[3], meta: match[4], line: index + 1, body: [] };
			return;
		}
		if (open && line.trim().startsWith(open.fence) && line.trim().replace(/[`~]/g, '') === '') {
			blocks.push(open);
			open = null;
			return;
		}
		if (open) {
			open.body.push(line);
		}
	});
	return blocks;
}

function prose(source) {
	const lines = [];
	let fence = null;
	let todo = false;
	for (const line of source.split(/\r?\n/)) {
		if (fence === null && /^:::todo/.test(line.trim())) {
			todo = true;
		}
		if (todo) {
			lines.push('');
			todo = line.trim() !== ':::';
			continue;
		}
		const match = line.match(FENCE);
		if (fence === null && match) {
			fence = match[2];
			lines.push('');
		} else if (fence !== null) {
			if (line.trim().startsWith(fence) && line.trim().replace(/[`~]/g, '') === '') {
				fence = null;
			}
			lines.push('');
		} else {
			lines.push(line.replace(/`[^`]*`/g, '').replace(/<[^>]+>/g, '').replace(/\]\([^)]*\)/g, ']'));
		}
	}
	return lines;
}

function snippets() {
	const problems = [];
	for (const file of files(DOCS, ['.md', '.mdx'])) {
		const source = readFileSync(file, 'utf-8');
		for (const block of codeBlocks(source)) {
			const meta = parseMeta(block.meta);
			const where = `${relative(DOCUMENTATION, file)}:${block.line}`;
			if (meta.file) {
				try {
					readRegion(meta.file, meta.region);
				} catch (error) {
					problems.push(`${where}: ${error.message}`);
				}
			} else if (block.lang === 'java' && !meta.fragment) {
				problems.push(`${where}: a Java block includes a snippet (file="…" region="…") or carries the fragment marker`);
			} else if (block.lang === 'java' && block.body.filter((line) => line.trim() !== '').length > 5) {
				problems.push(`${where}: a Java fragment has 5 lines at most, move it to documentation/examples`);
			}
		}
	}
	return problems;
}

function pages() {
	return files(DOCS, ['.md', '.mdx']).map((file) => {
		const path = relative(DOCS, file).replace(/\\/g, '/').replace(/\.mdx?$/, '');
		return { file, path, source: readFileSync(file, 'utf-8') };
	});
}

function descriptions() {
	return files(DESCRIPTIONS, ['.md']).filter((file) => !/README\.md$/.test(file)).map((file) => ({ file, source: readFileSync(file, 'utf-8') }));
}

function scan(entries, rules, skip = () => false) {
	const problems = [];
	for (const entry of entries) {
		prose(entry.source).forEach((line, index) => {
			for (const rule of rules) {
				if (!skip(entry, rule) && rule.pattern.test(line)) {
					problems.push(`${relative(DOCUMENTATION, entry.file)}:${index + 1}: ${rule.reason}: ${line.trim().slice(0, 90)}`);
				}
			}
		});
	}
	return problems;
}

function generated(entry) {
	return entry.path && entry.path.startsWith('reference/') && entry.path !== 'reference/glossary';
}

function phrases() {
	const entries = [...pages(), ...descriptions()].map((entry) => (/errors\.md$/.test(entry.file) ? { ...entry, source: entry.source.replace(/^## .*$/gm, '') } : entry));
	return scan(entries, PHRASES, (entry) => generated(entry));
}

function mentions() {
	const all = [...pages(), ...descriptions()];
	const problems = scan(all, [FORBIDDEN]);
	const use = pages().filter((entry) => entry.path.startsWith('use/'));
	return problems.concat(scan(use, [DEMOS]));
}

function lombok() {
	const examples = files(join(DOCUMENTATION, 'examples', 'src'), ['']).filter((file) => LOMBOK.pattern.test(readFileSync(file, 'utf-8')));
	const problems = examples.map((file) => `${relative(DOCUMENTATION, file)}: ${LOMBOK.reason}`);
	for (const entry of [...pages().filter((entry) => !generated(entry)), ...descriptions()]) {
		entry.source.split(/\r?\n/).forEach((line, index) => {
			if (LOMBOK.pattern.test(line)) {
				problems.push(`${relative(DOCUMENTATION, entry.file)}:${index + 1}: ${LOMBOK.reason}: ${line.trim().slice(0, 90)}`);
			}
		});
	}
	return problems;
}

function images() {
	const problems = [];
	for (const entry of pages()) {
		const lines = entry.source.split(/\r?\n/);
		lines.forEach((line, index) => {
			if (line.trim() === '<figure>') {
				const next = lines.slice(index + 1, index + 5).map((part) => part.trim());
				if (next[0] !== '' || !/^(?:!\[|<img|<picture)/.test(next[1] ?? '') || next[2] !== '' || !(next[3] ?? '').startsWith('<figcaption>')) {
					problems.push(`${relative(DOCUMENTATION, entry.file)}:${index + 1}: write a figure as <figure>, a blank line, the image, a blank line, then <figcaption> (templates.md)`);
				}
			}
			for (const match of line.matchAll(/!\[([^\]]*)\]\(([^)\s]+)/g)) {
				if (match[1].trim() === '') {
					problems.push(`${relative(DOCUMENTATION, entry.file)}:${index + 1}: image ${match[2]} has no alt text`);
				}
				if (match[2].startsWith('/') && !existsSync(join(DOCUMENTATION, 'public', match[2]))) {
					problems.push(`${relative(DOCUMENTATION, entry.file)}:${index + 1}: image ${match[2]} does not exist in public/`);
				}
			}
			for (const match of line.matchAll(/<img\b[^>]*>/g)) {
				if (!/\balt="[^"]+"/.test(match[0])) {
					problems.push(`${relative(DOCUMENTATION, entry.file)}:${index + 1}: <img> without alt text`);
				}
			}
		});
	}
	return problems;
}

function ownerRows() {
	const rows = [];
	for (const line of readFileSync(OWNERS, 'utf-8').split(/\r?\n/)) {
		const cells = line.trim().replace(/^\||\|$/g, '').split('|').map((cell) => cell.trim());
		if (cells.length !== 2 || cells[0] === 'Notion' || /^-+$/.test(cells[0])) {
			continue;
		}
		const match = cells[1].match(/^((?:use|build|reference)(?:\/[\w<>.-]+)*)/);
		if (match) {
			rows.push({ notion: cells[0], owner: match[1] });
		}
	}
	return rows;
}

function owners() {
	const problems = [];
	const known = new Set(pages().map((entry) => entry.path.replace(/\/index$/, '')));
	const terms = new Map();
	for (const row of ownerRows()) {
		const expanded = row.owner.includes('<') ? [] : [row.owner.replace(/\/index$/, '')];
		for (const owner of expanded) {
			if (!known.has(owner)) {
				problems.push(`owners.md: the owner page ${owner} of "${row.notion}" does not exist`);
			}
		}
		for (const match of row.notion.matchAll(/`@?([A-Za-z][\w.]*)`/g)) {
			const term = match[1];
			terms.set(term, terms.has(term) && terms.get(term) !== row.owner ? null : row.owner);
		}
	}
	for (const entry of pages()) {
		if (OWNER_EXEMPT.test(entry.path)) {
			continue;
		}
		const page = entry.path.replace(/\/index$/, '');
		const lines = entry.source.split(/\r?\n/);
		lines.forEach((line, index) => {
			const heading = line.match(/^#{2,3}\s+(.*)$/);
			const defined = [...line.matchAll(/\*\*`?@?([A-Za-z][\w.]*)`?\*\*/g)].map((match) => match[1]);
			const candidates = heading ? [...heading[1].matchAll(/`?@?([A-Za-z][\w.]*)`?/g)].map((match) => match[1]) : [];
			for (const term of [...candidates, ...defined]) {
				const owner = terms.get(term);
				if (owner && owner !== page && !owner.startsWith(page + '/')) {
					problems.push(`${relative(DOCUMENTATION, entry.file)}:${index + 1}: "${term}" is explained on ${owner} (owners.md); here, one sentence and a link`);
				}
			}
		});
	}
	return problems;
}

function terms() {
	const problems = [];
	const avoided = glossaryTerms().flatMap((term) => term.avoid.map((word) => ({ word, term: term.name })));
	for (const entry of [...pages(), ...descriptions()]) {
		if (entry.path === 'reference/glossary') {
			continue;
		}
		prose(entry.source).forEach((line, index) => {
			for (const { word, term } of avoided) {
				if (new RegExp(`\\b${word.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}(?:e?s)?\\b`, 'i').test(line)) {
					problems.push(`${relative(DOCUMENTATION, entry.file)}:${index + 1}: write "${term}", not "${word}"`);
				}
			}
		});
	}
	return problems;
}

function learnLines() {
	const problems = [];
	for (const entry of descriptions().filter((entry) => !/(?:errors|families)\.md$/.test(entry.file))) {
		const lines = entry.source.split(/\r?\n/);
		const start = lines[0] === '---' ? lines.indexOf('---', 1) + 1 : 0;
		const end = lines.findIndex((line, index) => index >= start && line.startsWith('## '));
		const numbered = lines.slice(start, end < 0 ? lines.length : end).map((line, index) => ({ line, number: start + index + 1 }));
		for (const { line, number } of numbered.filter(({ line }) => /^(?:Guides|See|Read|Learn more):/.test(line))) {
			problems.push(`${relative(DOCUMENTATION, entry.file)}:${number}: write the line as "Learn: [Page](/url/), ...", the only form the docgen merges into "Learn more": ${line.trim().slice(0, 90)}`);
		}
		const body = numbered.filter(({ line }) => line.trim() !== '' && !/^Learn:/.test(line));
		const last = body[body.length - 1];
		if (last && /^(?:Learn|See|Read)\b(?!:)/.test(last.line) && last.line.includes('](/')) {
			problems.push(`${relative(DOCUMENTATION, entry.file)}:${last.number}: end the description with a "Learn: [Page](/url/)" line, the docgen merges it into "Learn more": ${last.line.trim().slice(0, 90)}`);
		}
	}
	return problems;
}

function referenceTypes() {
	return existsSync(INDEX) ? JSON.parse(readFileSync(INDEX, 'utf-8')).types : {};
}

function typeOwners() {
	const owners = new Map();
	for (const row of ownerRows().filter((row) => !row.owner.includes('<') && !row.owner.startsWith('reference'))) {
		const side = row.owner.split('/')[0];
		for (const match of row.notion.matchAll(/`@?([A-Za-z][\w.]*)`/g)) {
			const sides = owners.get(match[1]) ?? {};
			sides[side] = side in sides && sides[side] !== row.owner ? null : row.owner;
			owners.set(match[1], sides);
		}
	}
	return owners;
}

function learnTargets() {
	const problems = [];
	const types = referenceTypes();
	const owners = typeOwners();
	for (const entry of descriptions()) {
		const type = types[basename(entry.file, '.md')];
		const sides = type && owners.get(type.name);
		if (!sides) {
			continue;
		}
		const owner = sides.use ?? sides.build;
		if (!owner) {
			continue;
		}
		const lines = entry.source.split(/\r?\n/);
		const end = lines.findIndex((line) => line.startsWith('## '));
		const number = lines.slice(0, end < 0 ? lines.length : end).findIndex((line) => /^Learn:/.test(line));
		const expected = `/${owner.replace(/\/index$/, '')}/`;
		if (number < 0) {
			continue;
		}
		const first = lines[number].match(/\]\((\/[^)#\s]*)/);
		if (!first || first[1] !== expected) {
			problems.push(`${relative(DOCUMENTATION, entry.file)}:${number + 1}: owners.md gives ${type.name} to ${expected}, name that page first on the Learn: line: ${lines[number].trim().slice(0, 90)}`);
		}
	}
	return problems;
}

function mentionLines(source) {
	const lines = source.split(/\r?\n/);
	const front = lines[0] === '---' ? lines.indexOf('---', 1) : -1;
	let fence = null;
	let todo = false;
	let comment = false;
	return lines.map((line, index) => {
		if (index <= front) {
			return '';
		}
		if (fence === null && /^:::todo/.test(line.trim())) {
			todo = true;
		}
		if (todo) {
			todo = line.trim() !== ':::';
			return '';
		}
		const match = line.match(FENCE);
		if (fence === null && match) {
			fence = match[2];
			return '';
		}
		if (fence !== null) {
			fence = line.trim().startsWith(fence) && line.trim().replace(/[`~]/g, '') === '' ? null : fence;
			return '';
		}
		if (comment || line.includes('<!--')) {
			comment = !line.includes('-->');
			return '';
		}
		if (/^#{1,6}\s/.test(line) || /^import\s/.test(line)) {
			return '';
		}
		return line.replace(/!\[[^\]]*\]\([^)]*\)/g, '').replace(/\[([^\]]*)\]\(\/reference\/[^)]*\)/g, (link, text) => text.replace(/`@?([A-Za-z][\w.$]*)[^`]*`/g, '[[$1]]'));
	});
}

function nestedNames(types) {
	const names = new Set(Object.values(types).map((type) => type.name));
	const nested = new Map();
	for (const name of names) {
		const segments = name.split('.');
		for (let count = 1; count < segments.length; count++) {
			const suffix = segments.slice(count).join('.');
			nested.set(suffix, nested.has(suffix) && nested.get(suffix) !== name ? null : name);
		}
	}
	return new Map([...nested].filter(([suffix, name]) => name && !names.has(suffix)));
}

function mentionedTypes(span) {
	const lead = span.match(/^@?([A-Z]\w*(?:\.[A-Z]\w*)*)(?:<[^>]*>)?(?:(?:\.|#|::)\w+(?:\(.*?\))?)*(?:\(.*\))?$/);
	const code = span.replace(/"(?:[^"\\]|\\.)*"|'(?:[^'\\]|\\.)*'/g, '""');
	const words = [...code.matchAll(/\b([a-z]\w*)\s+[A-Za-z_@]/g)].map((word) => word[1]);
	const text = /^\[|\w\s+[-–]\s+\w|\b[A-Z]\w*\s+\d|<[^<>]*\w\s+[a-z][^<>]*>/.test(code) || (code.includes('/') && !code.includes('('));
	if (text || words.some((word) => !CODE_KEYWORDS.has(word))) {
		return lead ? [lead[1]] : [];
	}
	return [...code.matchAll(/(?<![\w.$#@'"])@?([A-Z]\w*(?:\.[A-Z]\w*)*)/g)].map((type) => type[1]);
}

function firstMention() {
	const problems = [];
	const types = referenceTypes();
	const nested = nestedNames(types);
	for (const entry of pages().filter((entry) => !FIRST_MENTION_EXEMPT.test(entry.path))) {
		const seen = new Set();
		mentionLines(entry.source).forEach((line, index) => {
			for (const match of line.matchAll(/\[\[([A-Za-z0-9_.$]+)[^\]]*\]\]|`([^`]+)`/g)) {
				if (match[1]) {
					const segments = match[1].split('.');
					const linked = segments.map((segment, count) => segments.slice(0, segments.length - count).join('.')).find((candidate) => types[candidate]);
					seen.add(linked ? types[linked].name : match[1]);
					continue;
				}
				for (const mention of mentionedTypes(match[2])) {
					const parts = mention.split('.');
					const candidates = parts.map((part, count) => parts.slice(0, parts.length - count).join('.'));
					const full = candidates.find((candidate) => types[candidate]?.name === candidate);
					const short = candidates.find((candidate) => nested.has(candidate));
					const name = full ?? (short && nested.get(short));
					if (!name || seen.has(name)) {
						continue;
					}
					seen.add(name);
					problems.push(`${relative(DOCUMENTATION, entry.file)}:${index + 1}: link the first mention of ${name} with [[${name}]]: ${line.trim().slice(0, 90)}`);
				}
			}
		});
	}
	return problems;
}

function width(line) {
	return [...line].reduce((sum, character) => sum + (character === '\t' ? TAB_WIDTH : 1), 0);
}

function shownLines(file, region) {
	const lines = readFileSync(join(EXAMPLES, file), 'utf-8').split(/\r?\n/);
	const start = /^\s*\/\/\s*#region\s+(\S+)\s*$/;
	const end = /^\s*\/\/\s*#endregion\b/;
	const shown = [];
	let depth = region ? 0 : 1;
	lines.forEach((line, index) => {
		const opened = line.match(start);
		if (opened || end.test(line)) {
			if (region && depth > 0) {
				depth += opened ? 1 : -1;
			} else if (region && opened && opened[1] === region) {
				depth = 1;
			}
			return;
		}
		if (depth > 0) {
			shown.push({ line, number: index + 1 });
		}
	});
	const indents = shown.filter(({ line }) => line.trim() !== '').map(({ line }) => line.match(/^\t*/)[0].length);
	const indent = indents.length === 0 ? 0 : Math.min(...indents);
	return shown.map(({ line, number }) => ({ line: line.slice(indent), number }));
}

function lineWidth() {
	const problems = new Map();
	const report = (where, line) => {
		const columns = width(line);
		if (columns > LINE_WIDTH && (problems.get(where)?.columns ?? 0) < columns) {
			problems.set(where, { columns, text: line.trim() });
		}
	};
	for (const entry of [...pages().filter((entry) => !generated(entry)), ...descriptions()]) {
		for (const block of codeBlocks(entry.source)) {
			const meta = parseMeta(block.meta);
			if (meta.file) {
				if (existsSync(join(EXAMPLES, meta.file))) {
					shownLines(meta.file, meta.region).forEach(({ line, number }) => report(`examples/src/main/java/${meta.file}:${number}`, line));
				}
				continue;
			}
			const indents = block.body.filter((line) => line.trim() !== '').map((line) => line.match(/^[\t ]*/)[0].length);
			const indent = indents.length === 0 ? 0 : Math.min(...indents);
			block.body.forEach((line, index) => report(`${relative(DOCUMENTATION, entry.file).replace(/\\/g, '/')}:${block.line + index + 1}`, line.slice(indent)));
		}
	}
	return [...problems].map(([where, { columns, text }]) => `${where}: ${columns} columns, ${LINE_WIDTH} at most (a chain puts one call per line): ${text.slice(0, 90)}`);
}

function challenges() {
	const problems = [];
	for (const entry of pages().filter((entry) => entry.path.startsWith('use/learn/'))) {
		const lines = entry.source.split(/\r?\n/);
		const start = lines.findIndex((line) => /^## Try it\s*$/.test(line));
		if (start < 0) {
			continue;
		}
		let fence = null;
		let open = false;
		let titled = false;
		for (let index = start + 1; index < lines.length && !/^## /.test(lines[index]); index++) {
			const line = lines[index];
			const where = `${relative(DOCUMENTATION, entry.file)}:${index + 1}`;
			const match = line.match(FENCE);
			if (fence !== null) {
				fence = line.trim().startsWith(fence) && line.trim().replace(/[`~]/g, '') === '' ? null : fence;
				continue;
			}
			if (match) {
				fence = match[2];
				continue;
			}
			if (/^<details>/.test(line.trim())) {
				if (!titled) {
					problems.push(`${where}: a challenge starts with a bold title on its own line, such as "**Faster wheel.**"`);
				}
				open = true;
				titled = false;
			} else if (/^<\/details>/.test(line.trim())) {
				open = false;
			} else if (open && /^<summary>/.test(line.trim()) && line.trim() !== '<summary>Solution</summary>') {
				problems.push(`${where}: write <summary>Solution</summary>: ${line.trim()}`);
			} else if (!open && /^\d+\.\s/.test(line)) {
				problems.push(`${where}: a challenge has a bold title, never a number: ${line.trim().slice(0, 60)}`);
			} else if (!open && /^\*\*[^*]+\.\*\*\s*$/.test(line)) {
				titled = true;
				if (lines[index + 1] !== undefined && lines[index + 1].trim() === '') {
					problems.push(`${where}: the task follows the title on the next line, without a blank line`);
				}
			} else if (!open && /^\*\*[^*]+\*\*\s*$/.test(line)) {
				problems.push(`${where}: the title of a challenge ends with a period inside the bold: ${line.trim()}`);
				titled = true;
			}
		}
	}
	return problems;
}

function bannedWords() {
	const problems = [];
	const pattern = new RegExp(`\\b(${BANNED.map((word) => word.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')).join('|')})\\b`, 'i');
	for (const file of [...files(DOCS, ['.md', '.mdx']), ...files(DESCRIPTIONS, ['.md'])]) {
		prose(readFileSync(file, 'utf-8')).forEach((line, index) => {
			const match = line.match(pattern);
			if (match) {
				problems.push(`${relative(DOCUMENTATION, file)}:${index + 1}: "${match[1]}"`);
			}
		});
	}
	return problems;
}

function wikiLinks() {
	const problems = [];
	if (!existsSync(DIST)) {
		throw new Error('No build in dist/, run the build check first');
	}
	for (const file of files(DIST, ['.html'])) {
		const url = `/${relative(DIST, file).replace(/\\/g, '/').replace(/index\.html$/, '')}`;
		const html = readFileSync(file, 'utf-8').replace(/<pre[\s>][\s\S]*?<\/pre>/g, '');
		for (const match of html.matchAll(/\[\[[^\]\s<]{0,80}\]?\]?/g)) {
			problems.push(`${url} shows ${match[0]} as raw text (a [[Type#member]] link the docgen or the plugin did not resolve)`);
		}
	}
	return problems;
}

function drafts() {
	return files(DOCS, ['.md', '.mdx']).filter((file) => /^:::todo/m.test(readFileSync(file, 'utf-8'))).map((file) => `${relative(DOCS, file)} is still a draft`);
}

run('docgen', () => {
	gradle(':tool-docgen:run', '-q');
	if (!args.has('--fresh')) {
		return [];
	}
	const changed = execSync('git status --porcelain -- src/content/docs src/generated', { cwd: DOCUMENTATION, encoding: 'utf-8' }).trim();
	return changed === '' ? [] : [`the generated reference is stale, run npm run docgen and commit:\n${changed}`];
});

run('examples', () => {
	gradle(':documentation-examples:compileJava', ':documentation-examples:ecj', '-q');
	return [];
});

run('run-examples', () => {
	const report = join(DOCUMENTATION, 'examples', 'build', 'run-examples', 'report.txt');
	rmSync(report, { force: true });
	gradle(':documentation-examples:runExamples', '-q');
	if (!existsSync(report)) {
		throw new Error('run-examples wrote no report, see the Gradle output above');
	}
	return readFileSync(report, 'utf-8').split(/\r?\n/).filter((line) => /^(?:FAIL|UNUSED|STALE) /.test(line));
});

run('catalog', checkCatalog);
run('owners', owners);
run('terms', terms);
run('learn-lines', learnLines);
run('learn-targets', learnTargets);
run('first-mention', firstMention);
run('challenges', challenges);
run('phrases', phrases);
run('mentions', mentions);
run('lombok', lombok);
run('images', images);

run('snippets', snippets);
run('line-width', lineWidth);
run('banned-words', bannedWords);

run('build', () => {
	withLock(() => execSync('npx astro build', { cwd: DOCUMENTATION, stdio: 'inherit' }));
	return [];
});

run('wiki-links', wikiLinks);

run('coverage', () => {
	const report = join(ROOT, 'tool', 'docgen', 'build', 'docgen', 'coverage.json');
	if (!existsSync(report)) {
		throw new Error('No coverage report, run the docgen check first');
	}
	const coverage = JSON.parse(readFileSync(report, 'utf-8'));
	const problems = [];
	const expected = Object.values(coverage.families).reduce((sum, counts) => sum + counts[0], 0);
	const described = Object.values(coverage.families).reduce((sum, counts) => sum + counts[1], 0);
	if (described < expected) {
		problems.push(`${expected - described} of ${expected} descriptions are missing (${(described * 100 / expected).toFixed(1)} % described), see tool/docgen/build/docgen/coverage.md`);
	}
	coverage.unknown.forEach((line) => problems.push(line));
	coverage.broken.forEach((line) => problems.push(line));
	return problems;
});

if (args.has('--release')) {
	run('drafts', drafts);
} else {
	const pending = drafts();
	if (pending.length > 0) {
		console.log(`\nℹ ${pending.length} pages are still drafts (:::todo), hidden from production builds; --release fails on them`);
	}
}

const limit = args.has('--all') ? Infinity : 30;
console.log('\nSummary');
for (const result of results) {
	const mark = { ok: '✓', skipped: '-', warned: '!' }[result.status] ?? '✗';
	console.log(`${mark} ${result.name}${result.status === 'skipped' ? ' (skipped)' : ''}`);
	for (const problem of result.problems.slice(0, limit)) {
		console.log(`    ${problem}`);
	}
	if (result.problems.length > limit) {
		console.log(`    … ${result.problems.length - limit} more (--all prints them)`);
	}
}
process.exitCode = results.some((result) => result.status === 'failed') ? 1 : 0;