import { existsSync, readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { join } from 'node:path';

export const EXAMPLES = fileURLToPath(new URL('../examples/src/main/java/', import.meta.url));
export const SOURCE = 'https://github.com/Zeldown/JOID/blob/main/documentation/examples/src/main/java/';

const ATTRIBUTE = /(\w+)(?:=(?:"([^"]*)"|(\{[^}]*\})|([^\s"{]+)))?/g;
const START = /^\s*\/\/\s*#region\s+(\S+)\s*$/;
const END = /^\s*\/\/\s*#endregion(?:\s+(\S+))?\s*$/;

export function parseMeta(meta) {
	const result = { rest: '' };
	if (!meta) {
		return result;
	}
	const rest = [];
	for (const match of meta.matchAll(ATTRIBUTE)) {
		const [raw, key, quoted, braced, bare] = match;
		const value = quoted ?? braced ?? bare;
		if (key === 'file' || key === 'region' || key === 'title') {
			result[key] = value ?? '';
		} else if (key === 'fragment' && value === undefined) {
			result.fragment = true;
		} else {
			rest.push(raw);
		}
	}
	result.rest = rest.join(' ');
	return result;
}

export function regionsOf(source) {
	const regions = new Map();
	const open = [];
	source.split(/\r?\n/).forEach((line, number) => {
		const start = line.match(START);
		if (start) {
			open.push({ name: start[1], lines: [], line: number + 1 });
			return;
		}
		const end = line.match(END);
		if (end) {
			const region = open.pop();
			if (!region || (end[1] && end[1] !== region.name)) {
				throw new Error(`unbalanced #endregion at line ${number + 1}`);
			}
			regions.set(region.name, region.lines);
			return;
		}
		for (const region of open) {
			region.lines.push(line);
		}
	});
	if (open.length > 0) {
		throw new Error(`#region ${open[0].name} at line ${open[0].line} is never closed`);
	}
	return regions;
}

function dedent(lines) {
	const indents = lines.filter((line) => line.trim() !== '').map((line) => line.match(/^\t*/)[0].length);
	const indent = indents.length === 0 ? 0 : Math.min(...indents);
	const trimmed = lines.map((line) => line.slice(indent));
	while (trimmed.length > 0 && trimmed[0].trim() === '') {
		trimmed.shift();
	}
	while (trimmed.length > 0 && trimmed[trimmed.length - 1].trim() === '') {
		trimmed.pop();
	}
	return trimmed.join('\n');
}

export function readRegion(file, region) {
	const path = join(EXAMPLES, file);
	if (!existsSync(path)) {
		throw new Error(`snippet file not found: documentation/examples/src/main/java/${file}`);
	}
	const read = readFileSync(path, 'utf-8');
	const source = file.startsWith('com/example/tutorial/') ? read.replace(/^package com\.example\.tutorial\.\w+;/m, 'package com.example.starfall;').replace(/^import (static )?com\.example\.tutorial\.\w+\./gm, 'import $1com.example.starfall.') : read;
	const regions = regionsOf(source);
	if (region === undefined || region === '') {
		return dedent(source.split(/\r?\n/).filter((line) => !START.test(line) && !END.test(line)));
	}
	const lines = regions.get(region);
	if (!lines) {
		throw new Error(`region "${region}" not found in ${file} (regions: ${[...regions.keys()].join(', ') || 'none'})`);
	}
	return dedent(lines);
}