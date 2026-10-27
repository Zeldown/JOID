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
const DIRECTIVE = /^#\s*(crop|pad|still|hold)\s+(\S+)\s+(.*)$/;
const FILM = /^#\s*film\s+(\S+)(?:\s+(\d+))?\s*$/;

const args = process.argv.slice(2);
const only = args.find((arg) => !arg.startsWith('--'));

function coordinate(value) {
	return String(Number(value.toFixed(3)));
}

function expand(file, films) {
	const output = [];
	const mouse = { x: -5000, y: -5000 };
	let film = null;

	const capture = () => {
		output.push(`shot ${film.name}@${String(film.times.length).padStart(4, '0')}`);
		film.times.push(film.time);
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
		films[film.name] = { fps: film.fps, times: film.times };
		film = null;
	};

	for (const line of readFileSync(file, 'utf-8').split(/\r?\n/)) {
		const command = line.trim();
		const start = command.match(FILM);
		if (start) {
			cut();
			film = { name: start[1], fps: Number(start[2] ?? 30), time: 0, times: [] };
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
		}
		if (film !== null && parts[0] === 'wait') {
			advance(Number(parts[1]), (duration) => `wait ${duration}`);
		} else if (film !== null && parts[0] === 'moveto') {
			const from = { ...mouse };
			const to = { x: Number(parts[1]), y: Number(parts[2]) };
			advance(Number(parts[3]), (duration, progress) => `moveto ${coordinate(from.x + (to.x - from.x) * progress)} ${coordinate(from.y + (to.y - from.y) * progress)} ${duration}`);
		} else {
			output.push(line);
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
	if (only) {
		tasks.push(`-Pcapture=${only}`);
	}
	gradle(...tasks);
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

async function main() {
	const films = scenarios();
	if (!args.includes('--skip-render')) {
		render();
	}
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
	for (const [name, frames] of groups) {
		const setting = settings[name] ?? {};
		let region;
		if (setting.crop) {
			const [left, top, width, height] = setting.crop;
			region = { left, top, width, height };
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
		const film = films[name];
		if (!film) {
			await sharp(frames[0]).extract(region).webp({ lossless: true }).toFile(target);
		} else {
			if (film.times.length !== frames.length) {
				throw new Error(`${name} has ${frames.length} raw frames instead of ${film.times.length}, render its scenario again`);
			}
			const delay = film.times.map((time, index) => (index + 1 < film.times.length ? film.times[index + 1] - time : setting.hold?.[0] ?? Math.round(1000 / film.fps)));
			const buffers = await Promise.all(frames.map((frame) => sharp(frame).extract(region).png().toBuffer()));
			await sharp(buffers, { join: { animated: true } }).webp({ lossless: true, loop: 0, delay }).toFile(target);
			const still = Math.min(setting.still?.[0] ?? buffers.length - 1, buffers.length - 1);
			await sharp(buffers[still]).webp({ lossless: true }).toFile(join(OUTPUT, `${name}.still.webp`));
		}
		console.log(`${name}.webp ${region.width}x${region.height}${film ? `, ${frames.length} frames, ${film.times[film.times.length - 1]} ms` : ''}`);
	}
}

await withLock(main);