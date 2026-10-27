package com.example.describing.grid;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.layout.GridNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.property.OverflowProperty;
import dev.joid.lib.utils.align.Align;

public class GridCasesUI extends UI {

	private static final Color PLACEHOLDER = new Color(221, 221, 221);
	private static final Color INK         = new Color(153, 153, 153);

	@Override
	public void init() {
		RectNode
		.create(60, 100, 400, 300)
		.color(GridCasesUI.PLACEHOLDER)
		.body(card -> {
			// #region wrap
			GridNode
			.create(20, 20, 360, 0)
			.margin(10D)
			.body(grid -> {
				for (int i = 0; i < 9; i++) {
					RectNode.create(0, 0, 80, 80).color(GridCasesUI.INK).attach(grid);
				}
			})
			.attach(card);
			// #endregion
		})
		.attach(this);

		RectNode
		.create(520, 100, 400, 300)
		.color(GridCasesUI.PLACEHOLDER)
		.body(card -> {
			// #region margins
			GridNode
			.create(20, 20, 360, 0)
			.marginHorizontal(20D)
			.marginVertical(5D)
			.body(grid -> {
				for (int i = 0; i < 8; i++) {
					RectNode.create(0, 0, 70, 70).color(GridCasesUI.INK).attach(grid);
				}
			})
			.attach(card);
			// #endregion
		})
		.attach(this);

		RectNode
		.create(980, 100, 400, 300)
		.color(GridCasesUI.PLACEHOLDER)
		.body(card -> {
			// #region align
			GridNode
			.create(20, 20, 360, 0)
			.margin(10D)
			.alignX(Align.CENTER)
			.body(grid -> {
				for (int i = 0; i < 6; i++) {
					RectNode.create(0, 0, 80, 80).color(GridCasesUI.INK).attach(grid);
				}
			})
			.attach(card);
			// #endregion
		})
		.attach(this);

		RectNode
		.create(1440, 100, 400, 300)
		.color(GridCasesUI.PLACEHOLDER)
		.body(card -> {
			// #region rows
			GridNode
			.create(20, 20, 360, 0)
			.margin(10D)
			.body(grid -> {
				RectNode.create(0, 0, 110, 60).color(GridCasesUI.INK).attach(grid);
				RectNode.create(0, 0, 110, 120).color(Color.WHITE).attach(grid);
				RectNode.create(0, 0, 110, 80).color(GridCasesUI.INK).attach(grid);
				RectNode.create(0, 0, 170, 60).color(GridCasesUI.INK).attach(grid);
				RectNode.create(0, 0, 170, 60).color(GridCasesUI.INK).attach(grid);
			})
			.attach(card);
			// #endregion
		})
		.attach(this);

		RectNode
		.create(60, 460, 400, 300)
		.color(GridCasesUI.PLACEHOLDER)
		.body(card -> {
			// #region scroll
			GridNode
			.create(20, 20, 360, 260)
			.margin(10D)
			.overflow(OverflowProperty.SCROLL)
			.body(grid -> {
				for (int i = 0; i < 20; i++) {
					RectNode.create(0, 0, 80, 80).color(i % 2 == 0 ? GridCasesUI.INK : Color.WHITE).attach(grid);
				}
			})
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}