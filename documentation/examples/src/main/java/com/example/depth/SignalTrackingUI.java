package com.example.depth;

import dev.joid.lib.ui.node.Node;
import dev.joid.lib.utils.align.Align;

public class SignalTrackingUI extends DepthDiagramUI {

	@Override
	public void init() {
		super.backdrop(350, 240, 1220, 600)
		.body(card -> {
			this.column(card, 40D, "1. Compute", "total.get()", "runs price * quantity", "Records", "price v1, quantity v1", "Keeps 12", "with the two versions");
			this.column(card, 430D, "2. Change", "quantity.set(4)", "a new value", "quantity v2", "its version grows", "Marks total stale", "only if total has subscribers");
			this.column(card, 820D, "3. Read again", "total.get()", "on the next draw", "quantity v2 != v1", "a version differs", "Computes 24", "the next reads return 24");
		})
		.attach(this);
	}

	private void column(final Node card, final double left, final String title, final String... steps) {
		super.label(left, 30, 360, 40, title, Align.START).attach(card);
		for (int i = 0; i < steps.length / 2; i++) {
			final double top = 90D + i * 160D;
			if (i == 0) {
				super.subject(left, top, 360, 110, steps[0], steps[1]).attach(card);
			} else {
				super.box(left, top, 360, 110, steps[i * 2], steps[i * 2 + 1]).attach(card);
			}

			if (i < steps.length / 2 - 1) {
				super.arrow(card, left + 180D, top + 112D, left + 180D, top + 158D);
			}
		}
	}

}