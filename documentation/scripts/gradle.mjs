import { execSync } from 'node:child_process';
import { mkdirSync, rmdirSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

export const ROOT = fileURLToPath(new URL('../..', import.meta.url));

const LOCK = process.env.JOID_GRADLE_LOCK ?? join(ROOT, 'build', 'gradle.lock');

function lock() {
	if (process.env.JOID_GRADLE_LOCK_HELD === '1') {
		return false;
	}
	mkdirSync(dirname(LOCK), { recursive: true });
	for (let waited = 0; ; waited += 5) {
		try {
			mkdirSync(LOCK);
			return true;
		} catch (error) {
			if (error.code !== 'EEXIST') {
				throw error;
			}
			if (waited % 60 === 0) {
				process.stdout.write(`Waiting for another Gradle run or site build to finish (lock ${LOCK})...\n`);
			}
			Atomics.wait(new Int32Array(new SharedArrayBuffer(4)), 0, 0, 5000);
		}
	}
}

export function withLock(action) {
	const locked = lock();
	const held = process.env.JOID_GRADLE_LOCK_HELD;
	process.env.JOID_GRADLE_LOCK_HELD = '1';
	const release = () => {
		if (held === undefined) {
			delete process.env.JOID_GRADLE_LOCK_HELD;
		} else {
			process.env.JOID_GRADLE_LOCK_HELD = held;
		}
		if (locked) {
			rmdirSync(LOCK);
		}
	};
	let result;
	try {
		result = action();
	} catch (error) {
		release();
		throw error;
	}
	if (result instanceof Promise) {
		return result.finally(release);
	}
	release();
	return result;
}

export function gradle(...tasks) {
	const wrapper = join(ROOT, process.platform === 'win32' ? 'gradlew.bat' : 'gradlew');
	const offline = process.env.CI ? [] : ['--offline', '--max-workers=2'];
	withLock(() => execSync(`"${wrapper}" ${[...tasks, ...offline].join(' ')}`, { cwd: ROOT, stdio: 'inherit' }));
}
