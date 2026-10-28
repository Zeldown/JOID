import { existsSync, mkdirSync, readFileSync, readdirSync, rmSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { fileURLToPath } from 'node:url';
import sharp from 'sharp';
import { gradle, withLock } from './gradle.mjs';

const DOCUMENTATION = fileURLToPath(new URL('..', import.meta.url));
const SCENARIOS = join(DOCUMENTATION, 'examples', 'captures');
const EXPANDED = join(DOCUMENTATION, 'examples', 'build', 'captures', 'scenarios');
const RAW = join(DOCUMENTATION, 'examples', 'build', 'captures', 'raw');
const OUTPUT = join(DOCUMENTATION, 'public', 'captures');
const DIRECTIVE = /^#\s*(crop|pad|still|hold|quality|keys)\s+(\S+)\s+(.*)$/;
const FILM = /^#\s*film\s+(\S+)(?:\s+(\d+))?\s*$/;
const TWEENS = { resizeto: 'resize', zoomto: 'zoom', scaleto: 'scale' };

const args = process.argv.slice(2);
const only = args.filter((arg) => !arg.startsWith('--'));

function coordinate(value) {
	return String(Number(value.toFixed(3)));
}

const KEY_NAMES = {
	LEFT_CONTROL: 'Ctrl',
	RIGHT_CONTROL: 'Ctrl',
	LEFT_SHIFT: 'Shift',
	RIGHT_SHIFT: 'Shift',
	LEFT_ALT: 'Alt',
	RIGHT_ALT: 'Alt',
	ENTER: 'Enter',
	ESCAPE: 'Esc',
	BACKSPACE: 'Backspace',
	DELETE: 'Delete',
	TAB: 'Tab',
	SPACE: 'Space',
	UP: '↑',
	DOWN: '↓',
	LEFT: '←',
	RIGHT: '→',
	HOME: 'Home',
	END: 'End',
	EQUAL: '=',
	MINUS: '-',
};

function keyName(combination) {
	return combination
		.split('+')
		.map((key) => KEY_NAMES[key] ?? (key.length === 1 ? key : key.charAt(0) + key.slice(1).toLowerCase().replaceAll('_', ' ')))
		.join(' + ');
}

function typedName(text) {
	return text === ' ' ? 'Space' : text;
}

function wheelName(parts) {
	const vertical = Number(parts[parts.length - 1]);
	const horizontal = parts.length > 2 ? Number(parts[1]) : 0;
	if (vertical !== 0) {
		return vertical < 0 ? 'Wheel ↓' : 'Wheel ↑';
	}
	return horizontal < 0 ? 'Wheel ←' : 'Wheel →';
}

function expand(file, films) {
	const output = [];
	const mouse = { x: -5000, y: -5000 };
	const view = { resize: [1920, 1080], zoom: [1], scale: [1] };
	let film = null;

	const label = () => {
		const shown = film.keys.filter((key) => key.start <= film.time && (key.end === null || film.time < key.end));
		return shown.length === 0 ? null : shown[shown.length - 1].label;
	};
	const capture = () => {
		output.push(`shot ${film.name}@${String(film.times.length).padStart(4, '0')}`);
		film.times.push(film.time);
		film.labels.push(label());
	};
	const advance = (duration, command) => {
		const start = film.time;
		const end = start + duration;
		for (let next = Math.round((film.times.length * 1000) / film.fps); next <= end; next = Math.round((film.times.length * 1000) / film.fps)) {
			output.push(command(next - film.time, (next - start) / duration));
			film.time = next;
			capture();
		}
		if (film.time < end) {
			output.push(command(end - film.time, 1));
			film.time = end;
		}
	};
	const cut = () => {
		if (film === null) {
			return;
		}
		if (film.time > film.times[film.times.length - 1]) {
			advance(Math.round((film.times.length * 1000) / film.fps) - film.time, (duration) => `wait ${duration}`);
		}
		films[film.name] = { fps: film.fps, times: film.times, labels: film.labels };
		film = null;
	};

	for (const line of readFileSync(file, 'utf-8').split(/\r?\n/)) {
		const command = line.trim();
		const start = command.match(FILM);
		if (start) {
			cut();
			film = { name: start[1], fps: Number(start[2] ?? 30), time: 0, times: [], labels: [], keys: [] };
			output.push(line);
			capture();
			continue;
		}
		if (command === 'cut') {
			cut();
			continue;
		}
		if (command === '' || command.startsWith('#')) {
			output.push(line);
			continue;
		}

		const parts = command.split(/\s+/);
		if (parts[0] === 'shot' && parts[1].includes('@')) {
			throw new Error(`${file}: ${parts[1]} is a numbered shot, film the motion with # film ${parts[1].split('@')[0]}`);
		}
		if (parts[0] === 'ui') {
			mouse.x = -5000;
			mouse.y = -5000;
			view.zoom = [1];
		}
		if (film !== null && parts[0] === 'type') {
			film.keys.push({ label: typedName(line.trim().slice(5)), start: film.time, end: film.time + 400 });
		} else if (film !== null && parts[0] === 'key') {
			film.keys.push({ label: keyName(parts[1]), start: film.time, end: film.time + 400 });
		} else if (film !== null && parts[0] === 'scroll') {
			film.keys.push({ label: wheelName(parts), start: film.time, end: film.time + 400 });
		} else if (film !== null && parts[0] === 'down') {
			film.keys.push({ label: keyName(parts[1]), start: film.time, end: null, key: parts[1] });
		} else if (film !== null && parts[0] === 'up') {
			const held = film.keys.findLast((key) => key.key === parts[1] && key.end === null);
			if (held) {
				held.end = Math.max(film.time, held.start + 400);
			}
		}
		if (film !== null && parts[0] === 'wait') {
			advance(Number(parts[1]), (duration) => `wait ${duration}`);
		} else if (film !== null && parts[0] === 'moveto') {
			const from = { ...mouse };
			const to = { x: Number(parts[1]), y: Number(parts[2]) };
			advance(Number(parts[3]), (duration, progress) => `moveto ${coordinate(from.x + (to.x - from.x) * progress)} ${coordinate(from.y + (to.y - from.y) * progress)} ${duration}`);
		} else if (TWEENS[parts[0]]) {
			const setting = TWEENS[parts[0]];
			const from = view[setting];
			const to = from.map((_, index) => Number(parts[index + 1]));
			const duration = Number(parts[to.length + 1]);
			const value = (progress) => from.map((start, index) => (setting === 'resize' ? Math.round(start + (to[index] - start) * progress) : coordinate(start + (to[index] - start) * progress))).join(' ');
			if (film === null) {
				output.push(`wait ${duration}`, `${setting} ${value(1)}`);
			} else {
				advance(duration, (step, progress) => `wait ${step}\r\n${setting} ${value(progress)}`);
			}
			view[setting] = to;
		} else {
			output.push(line);
		}
		if (view[parts[0]]) {
			view[parts[0]] = parts.slice(1, view[parts[0]].length + 1).map(Number);
		}
		if (parts[0] === 'move' || parts[0] === 'moveto') {
			mouse.x = Number(parts[1]);
			mouse.y = Number(parts[2]);
		}
	}
	cut();
	return output;
}

function scenarios() {
	const films = {};
	rmSync(EXPANDED, { recursive: true, force: true });
	mkdirSync(EXPANDED, { recursive: true });
	for (const file of readdirSync(SCENARIOS).filter((name) => name.endsWith('.txt'))) {
		writeFileSync(join(EXPANDED, file), expand(join(SCENARIOS, file), films).join('\r\n'));
	}
	return films;
}

function render() {
	rmSync(RAW, { recursive: true, force: true });
	const tasks = [':documentation-examples:captures', '-q'];
	if (only.length > 0) {
		tasks.push(`-Pcapture=${only.join(',')}`);
	}
	try {
		gradle(...tasks);
		return true;
	} catch {
		return false;
	}
}

function directives() {
	const result = {};
	for (const file of readdirSync(SCENARIOS).filter((name) => name.endsWith('.txt'))) {
		for (const line of readFileSync(join(SCENARIOS, file), 'utf-8').split(/\r?\n/)) {
			const match = line.trim().match(DIRECTIVE);
			if (match) {
				result[match[2]] = { ...result[match[2]], [match[1]]: match[3].trim().split(/\s+/).map(Number) };
			}
		}
	}
	return result;
}

async function bounds(file, pad) {
	const { data, info } = await sharp(file).raw().toBuffer({ resolveWithObject: true });
	const channels = info.channels;
	const background = [data[0], data[1], data[2]];
	let left = info.width;
	let top = info.height;
	let right = -1;
	let bottom = -1;
	for (let y = 0; y < info.height; y++) {
		for (let x = 0; x < info.width; x++) {
			if (Math.abs((x % 20) - 10) <= 2 && Math.abs((y % 20) - 10) <= 2) {
				continue;
			}
			const index = (y * info.width + x) * channels;
			const distance = Math.abs(data[index] - background[0]) + Math.abs(data[index + 1] - background[1]) + Math.abs(data[index + 2] - background[2]);
			if (distance > 18) {
				left = Math.min(left, x);
				top = Math.min(top, y);
				right = Math.max(right, x);
				bottom = Math.max(bottom, y);
			}
		}
	}
	if (right < 0) {
		return { left: 0, top: 0, right: info.width - 1, bottom: info.height - 1 };
	}
	return { left: Math.max(0, left - pad), top: Math.max(0, top - pad), right: Math.min(info.width - 1, right + pad), bottom: Math.min(info.height - 1, bottom + pad) };
}

function escape(text) {
	return text.replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;');
}

function badgeWidth(label) {
	return Math.max(34, Math.round(label.length * 9.5 + 24));
}

function badge(label, left, top) {
	const width = badgeWidth(label);
	return `<rect x="${left}" y="${top + 3}" width="${width}" height="32" rx="9" fill="#262B35"/>`
		+ `<rect x="${left}" y="${top}" width="${width}" height="32" rx="9" fill="#171A21" stroke="#3A4150" stroke-width="1.5"/>`
		+ `<text x="${left + width / 2}" y="${top + 21.5}" font-family="Segoe UI, Arial, sans-serif" font-weight="600" font-size="16" fill="#E7E9EE" text-anchor="middle">${escape(label)}</text>`;
}

async function keyed(image, label, region, position) {
	if (!label) {
		return image.png().toBuffer();
	}
	const width = badgeWidth(label);
	const left = position ? position[0] : region.width - width - 14;
	const top = position ? position[1] : region.height - 32 - 14;
	const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${width + 4}" height="38">${badge(label, 2, 2)}</svg>`;
	const base = await image.png().toBuffer();
	return sharp(base).composite([{ input: Buffer.from(svg), left: Math.max(0, left - 2), top: Math.max(0, top - 2) }]).png().toBuffer();
}

async function framed(frames) {
	const sizes = await Promise.all(frames.map((frame) => sharp(frame).metadata()));
	if (sizes.every((size) => size.width === sizes[0].width && size.height === sizes[0].height)) {
		return frames;
	}
	const width = Math.max(...sizes.map((size) => size.width)) + 48;
	const height = Math.max(...sizes.map((size) => size.height)) + 88;
	return Promise.all(frames.map((frame, index) => {
		const right = 24 + sizes[index].width;
		const bottom = 24 + sizes[index].height;
		const label = `${sizes[index].width} × ${sizes[index].height}`;
		const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}">`
			+ `<rect x="23" y="23" width="${right - 22}" height="${bottom - 22}" fill="none" stroke="#3A4150" stroke-width="2"/>`
			+ `<circle cx="${right}" cy="${bottom}" r="6" fill="#262B35" stroke="#8B93A3" stroke-width="1.5"/>`
			+ badge(label, right - badgeWidth(label), bottom + 16)
			+ '</svg>';
		return sharp({ create: { width, height, channels: 3, background: '#0E1014' } }).composite([{ input: frame, left: 24, top: 24 }, { input: Buffer.from(svg), left: 0, top: 0 }]).png().toBuffer();
	}));
}

async function main() {
	const films = scenarios();
	const rendered = args.includes('--skip-render') || render();
	if (!existsSync(RAW)) {
		throw new Error(`No raw capture in ${RAW}`);
	}

	const settings = directives();
	const groups = new Map();
	for (const file of readdirSync(RAW).filter((name) => name.endsWith('.png')).sort()) {
		const name = file.slice(0, -4).split('@')[0];
		groups.set(name, [...(groups.get(name) ?? []), join(RAW, file)]);
	}

	mkdirSync(OUTPUT, { recursive: true });
	for (const [name, files] of groups) {
		const setting = settings[name] ?? {};
		const film = films[name];
		const frames = film ? await framed(files) : files;
		let region;
		if (setting.crop) {
			const [left, top, width, height] = setting.crop;
			region = { left, top, width, height };
		} else if (frames !== files) {
			const { width, height } = await sharp(frames[0]).metadata();
			region = { left: 0, top: 0, width, height };
		} else {
			const pad = setting.pad?.[0] ?? 32;
			let box = null;
			for (const frame of frames) {
				const next = await bounds(frame, pad);
				box = box === null ? next : { left: Math.min(box.left, next.left), top: Math.min(box.top, next.top), right: Math.max(box.right, next.right), bottom: Math.max(box.bottom, next.bottom) };
			}
			region = { left: box.left, top: box.top, width: box.right - box.left + 1, height: box.bottom - box.top + 1 };
		}

		const target = join(OUTPUT, `${name}.webp`);
		rmSync(join(OUTPUT, `${name}.still.webp`), { force: true });
		if (!film) {
			await sharp(frames[0]).extract(region).webp({ lossless: true, effort: 6 }).toFile(target);
		} else {
			if (film.times.length !== frames.length) {
				throw new Error(`${name} has ${frames.length} raw frames instead of ${film.times.length}, render its scenario again`);
			}
			const delay = film.times.map((time, index) => (index + 1 < film.times.length ? film.times[index + 1] - time : setting.hold?.[0] ?? Math.round(1000 / film.fps)));
			const buffers = await Promise.all(frames.map((frame, index) => keyed(sharp(frame).extract(region), film.labels[index], region, setting.keys)));
			const encoding = setting.quality ? { quality: setting.quality[0], smartSubsample: true } : { lossless: true, effort: 6 };
			await sharp(buffers, { join: { animated: true } }).webp({ ...encoding, minSize: true, loop: 0, delay }).toFile(target);
			const still = Math.min(setting.still?.[0] ?? buffers.length - 1, buffers.length - 1);
			await sharp(buffers[still]).webp(encoding).toFile(join(OUTPUT, `${name}.still.webp`));
		}
		console.log(`${name}.webp ${region.width}x${region.height}${film ? `, ${frames.length} frames, ${film.times[film.times.length - 1]} ms` : ''}`);
	}
	if (!rendered) {
		throw new Error('Some scenarios did not render, see the [DocCapture] FAIL lines above: the captures of the other scenarios are written');
	}
}

await withLock(main);