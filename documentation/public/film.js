(() => {
	const reduced = window.matchMedia('(prefers-reduced-motion: reduce)');

	function show(button, paused) {
		const image = button.querySelector('img');
		button.querySelector('source')?.remove();
		image.src = paused ? button.dataset.still : button.dataset.film;
		button.setAttribute('aria-pressed', String(paused));
	}

	function start() {
		for (const button of document.querySelectorAll('.joid-film')) {
			show(button, reduced.matches);
			button.addEventListener('click', () => show(button, button.getAttribute('aria-pressed') !== 'true'));
		}
	}

	if (document.readyState === 'loading') {
		document.addEventListener('DOMContentLoaded', start);
	} else {
		start();
	}
})();