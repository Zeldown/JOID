package com.example.depth;

import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.utils.align.Align;

public class PixelAlignmentUI extends DepthDiagramUI {

	@Override
	public void init() {
		super.backdrop(300, 320, 1320, 440)
		.body(card -> {
			this.grid(card, 40D, "Exact edges", 1.5D, 1D, 4D, 3D);
			this.grid(card, 470D, "Snapped edges", 2D, 1D, 4D, 3D);
			this.grid(card, 900D, "Thin line", 1D, 2.3D, 6D, 0.4D);
		})
		.attach(this);
	}

	private void grid(final Node card, final double left, final String title, final double x, final double y, final double width, final double height) {
		final double cell = 47.5D;
		super.label(left, 30, 380, 40, title, Align.START).attach(card);
		card.layer((node, mouseX, mouseY) -> {
			for (int column = 0; column < 8; column++) {
				for (int row = 0; row < 6; row++) {
					final double coverageX = Math.max(0D, Math.min(column + 1D, x + width) - Math.max(column, x));
					final double coverageY = Math.max(0D, Math.min(row + 1D, y + height) - Math.max(row, y));
					final float coverage = (float) (coverageX * coverageY);
					DrawUtils.SHAPE.drawRect(left + column * cell, 90D + row * cell, cell - 3D, cell - 3D, DepthDiagramUI.ELEMENT);
					if (coverage > 0F) {
						DrawUtils.SHAPE.drawRect(left + column * cell, 90D + row * cell, cell - 3D, cell - 3D, DepthDiagramUI.LABEL.withAlpha(coverage));
					}
				}
			}

			DrawUtils.SHAPE.drawBorder(left + x * cell - 1D, 90D + y * cell - 1D, width * cell, Math.max(2D, height * cell), DepthDiagramUI.ACCENT, 2D);
		});
	}

}