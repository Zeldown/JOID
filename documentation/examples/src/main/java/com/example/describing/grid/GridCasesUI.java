package com.example.describing.grid;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.layout.GridNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.utils.align.Align;

public class GridCasesUI extends UI {

	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	@Override
	public void init() {
		RectNode
		.create(60, 100, 400, 300)
		.color(GridCasesUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(GridCasesUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region wrap
			GridNode
			.create(20, 20, 360, 0)
			.margin(10D)
			.body(grid -> {
				for (int i = 0; i < 9; i++) {
					RectNode.create(0, 0, 80, 80).color(GridCasesUI.ACCENT).attach(grid);
				}
			})
			.attach(card);
			// #endregion
		})
		.attach(this);

		RectNode
		.create(520, 100, 400, 300)
		.color(GridCasesUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(GridCasesUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region margins
			GridNode
			.create(20, 20, 360, 0)
			.marginHorizontal(20D)
			.marginVertical(5D)
			.body(grid -> {
				for (int i = 0; i < 8; i++) {
					RectNode.create(0, 0, 70, 70).color(GridCasesUI.ACCENT).attach(grid);
				}
			})
			.attach(card);
			// #endregion
		})
		.attach(this);

		RectNode
		.create(980, 100, 400, 300)
		.color(GridCasesUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(GridCasesUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region align
			GridNode
			.create(20, 20, 360, 0)
			.margin(10D)
			.alignX(Align.CENTER)
			.body(grid -> {
				for (int i = 0; i < 6; i++) {
					RectNode.create(0, 0, 80, 80).color(GridCasesUI.ACCENT).attach(grid);
				}
			})
			.attach(card);
			// #endregion
		})
		.attach(this);

		RectNode
		.create(1440, 100, 400, 300)
		.color(GridCasesUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(GridCasesUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region rows
			GridNode
			.create(20, 20, 360, 0)
			.margin(10D)
			.body(grid -> {
				RectNode.create(0, 0, 110, 60).color(GridCasesUI.ACCENT).attach(grid);
				RectNode.create(0, 0, 110, 120).color(GridCasesUI.ELEMENT).attach(grid);
				RectNode.create(0, 0, 110, 80).color(GridCasesUI.ACCENT).attach(grid);
				RectNode.create(0, 0, 170, 60).color(GridCasesUI.ACCENT).attach(grid);
				RectNode.create(0, 0, 170, 60).color(GridCasesUI.ACCENT).attach(grid);
			})
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}