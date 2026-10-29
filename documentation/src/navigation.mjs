const group = (label, directory, collapsed = false) => ({ label, collapsed, items: [{ autogenerate: { directory } }] });

export function topics(reference) {
	return [
		{
			id: 'use',
			label: 'Use JOID',
			link: '/use/',
			items: [
				{ label: 'Get started', items: ['use', 'use/get-started/installation', 'use/get-started/quick-start', 'use/get-started/dev-tools'] },
				group('Tutorial', 'use/tutorial', true),
				{
					label: 'Learn',
					items: [
						'use/learn/how-joid-thinks',
						group('1. Describing the UI', 'use/learn/describing-the-ui'),
						group('2. Interactivity', 'use/learn/interactivity'),
						group('3. State', 'use/learn/state'),
						group('4. Style and motion', 'use/learn/style-and-motion'),
						group('5. Going further', 'use/learn/going-further'),
						group('6. Running your UI', 'use/learn/running'),
					],
				},
				'use/catalog',
				group('Guides', 'use/guides', true),
				group('In depth', 'use/in-depth', true),
				{ label: 'Help', collapsed: true, items: ['use/troubleshooting', 'use/best-practices'] },
			],
		},
		{
			id: 'build',
			label: 'Build on JOID',
			link: '/build/',
			items: [
				{ label: 'Overview', items: ['build', 'build/architecture'] },
				group('Bridges', 'build/bridges'),
				{ label: 'Writing a backend', items: ['build/writing-a-backend', 'build/resource-resolvers', 'build/input', 'build/embedding-in-a-host'] },
				group('Our backends', 'build/backends'),
				group('Base modules', 'build/base-modules'),
				{ label: 'Pipeline', items: ['build/pipeline', 'build/shader-translation', 'build/framebuffers-and-depth'] },
				{ label: 'Testing and demos', items: ['build/testing-a-backend', 'build/demos'] },
			],
		},
		{
			id: 'reference',
			label: 'Reference',
			link: '/reference/',
			items: ['reference', 'reference/errors', 'reference/glossary', ...reference],
		},
		{
			label: 'Showcase',
			link: '/showcase/',
		},
	];
}

export function order(items, pages) {
	const ids = [];
	const byOrder = (left, right) => (left.data.sidebar?.order ?? Number.MAX_SAFE_INTEGER) - (right.data.sidebar?.order ?? Number.MAX_SAFE_INTEGER) || left.id.localeCompare(right.id);
	for (const item of items) {
		if (typeof item === 'string') {
			ids.push(item);
		} else if (item.link) {
			ids.push(item.link.replace(/^\/|\/$/g, ''));
		} else if (item.autogenerate) {
			const directory = item.autogenerate.directory;
			const inside = pages.filter((page) => page.id === directory || page.id.startsWith(`${directory}/`));
			const own = inside.filter((page) => page.id === directory || !page.id.slice(directory.length + 1).includes('/')).sort(byOrder);
			ids.push(...own.map((page) => page.id));
			ids.push(...inside.filter((page) => !own.includes(page)).sort(byOrder).map((page) => page.id));
		} else if (item.items) {
			ids.push(...order(item.items, pages));
		}
	}
	return ids;
}