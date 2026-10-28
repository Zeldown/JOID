package com.example.depth;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.utils.align.Align;

public class PixelMotionUI extends DepthDiagramUI {

	private final long start = BridgeHandler.CLOCK.get().currentTimeMillis();

	@Override
	public void init() {
		super.backdrop(620, 330, 680, 400)
		.body(card -> {
			this.grid(card, 20D, "Exact position", false);
			this.grid(card, 215D, "On the screen", true);
		})
		.attach(this);
	}

	private void grid(final Node card, final double top, final String title, final boolean snapped) {
		final double cell = 47.5D;
		super.label(40, top, 600, 40, title, Align.START).attach(card);
		card.layer((node, mouseX, mouseY) -> {
			final double time = (BridgeHandler.CLOCK.get().currentTimeMillis() - this.start) % 4000L;
			final double x = PixelMotionUI.getPosition(time);
			final double drawn = snapped ? PixelMotionUI.getDrawnPosition(time, x) : x;
			for (int column = 0; column < 13; column++) {
				final float coverage = (float) Math.max(0D, Math.min(column + 1D, drawn + 3D) - Math.max(column, drawn));
				for (int row = 0; row < 2; row++) {
					DrawUtils.SHAPE.drawRect(40D + column * cell, top + 60D + row * cell, cell - 3D, cell - 3D, DepthDiagramUI.ELEMENT);
					if (coverage > 0F) {
						DrawUtils.SHAPE.drawRect(40D + column * cell, top + 60D + row * cell, cell - 3D, cell - 3D, DepthDiagramUI.LABEL.withAlpha(coverage));
					}
				}
			}

			DrawUtils.SHAPE.drawBorder(40D + x * cell - 1D, top + 59D, 3D * cell, 2D * cell, DepthDiagramUI.ACCENT, 2D);
		});
	}

	private static double getPosition(final double time) {
		if (time < 500D) {
			return 1.3D;
		}
		if (time < 2000D) {
			return 1.3D + 8D * PixelMotionUI.ease((time - 500D) / 1500D);
		}
		if (time < 2500D) {
			return 9.3D;
		}
		return 9.3D - 8D * PixelMotionUI.ease((time - 2500D) / 1500D);
	}

	private static double getDrawnPosition(final double time, final double position) {
		if (time < 500D || time >= 2000D && time < 2500D) {
			return Math.floor(position + 0.5D);
		}

		final double rest = time < 2000D ? 1.3D : 9.3D;
		return Math.floor(rest + 0.5D) + Math.round(position - rest);
	}

	private static double ease(final double progress) {
		return progress * progress * (3D - 2D * progress);
	}

}