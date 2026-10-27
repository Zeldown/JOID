import { defineRouteMiddleware } from '@astrojs/starlight/route-data';

type Entry = { type: 'link'; label: string; href: string; isCurrent: boolean; badge: unknown; attrs: Record<string, unknown> } | { type: 'group'; label: string; entries: Entry[]; collapsed: boolean; badge: unknown };

const holds = (entry: Entry): boolean => (entry.type === 'link' ? entry.isCurrent : entry.entries.some(holds));

const first = (entry: Entry): string | undefined => (entry.type === 'link' ? entry.href : entry.entries.map(first).find((href) => href !== undefined));

const prune = (entries: Entry[]): Entry[] =>
	entries.map((entry) => {
		if (entry.type === 'link') {
			return entry;
		}
		if (holds(entry)) {
			return { ...entry, collapsed: false, entries: prune(entry.entries) };
		}
		return { type: 'link', label: entry.label, href: first(entry) ?? '/', isCurrent: false, badge: undefined, attrs: { class: 'joid-sidebar-section' } };
	});

export const onRequest = defineRouteMiddleware(async (context, next) => {
	await next();
	const route = context.locals.starlightRoute;
	route.sidebar = prune(route.sidebar as Entry[]) as typeof route.sidebar;
});
