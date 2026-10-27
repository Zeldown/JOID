import { getCollection } from 'astro:content';
import reference from '../generated/reference-sidebar.json';
import { order, topics } from '../navigation.mjs';

const SITE = 'https://documentation.joid.dev';

export async function GET() {
	const pages = (await getCollection('docs')).filter((page) => !page.data.draft && page.id !== '404' && !/\/reference\/[^/]+\/[^/]+$/.test(page.id));
	const byId = new Map(pages.map((page) => [page.id, page]));
	const line = (page) => `- [${page.data.title}](${SITE}/${page.id === 'index' ? '' : `${page.id}/`}): ${page.data.description ?? ''}`;
	const section = (topic) => {
		const ids = [...new Set(order(topic.items, pages))].filter((id) => byId.has(id));
		const rest = pages.filter((page) => page.id.startsWith(`${topic.id}/`) && !ids.includes(page.id)).map((page) => page.id).sort();
		return [...ids, ...rest].map((id) => line(byId.get(id)));
	};
	const [use, build] = topics(reference);
	const body = [
		'# JOID',
		'',
		'> JOID builds interfaces in Java on a design canvas, rendered by OpenGL or Vulkan. The pages follow the order of the sidebar; the reference has one page per type under /use/reference/<family>/ and /build/reference/<family>/.',
		'',
		'## Use JOID',
		'',
		...section(use),
		'',
		'## Build on JOID',
		'',
		...section(build),
		'',
	].join('\n');
	return new Response(body, { headers: { 'Content-Type': 'text/plain; charset=utf-8' } });
}