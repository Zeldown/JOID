export function filmMarkup(src, still, image) {
	return `<button type="button" class="joid-film" aria-pressed="false" data-film="${src}" data-still="${still}"><picture><source srcset="${still}" media="(prefers-reduced-motion: reduce)" />${image}</picture><span class="joid-film-state">Pause the animation</span></button>`;
}