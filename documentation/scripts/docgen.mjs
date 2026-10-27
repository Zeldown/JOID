import { generateCatalog } from './catalog.mjs';
import { generateGlossary } from './glossary.mjs';
import { gradle, withLock } from './gradle.mjs';

const tasks = [':tool-docgen:run', '-q'];
for (const arg of process.argv.slice(2)) {
	if (arg === '--strict') {
		tasks.push('-Pstrict');
	} else if (arg.startsWith('--descriptions=')) {
		tasks.push(`"-Pdescriptions=${arg.slice(15)}"`);
	}
}
withLock(() => {
	gradle(...tasks);
	generateCatalog();
	generateGlossary();
});
