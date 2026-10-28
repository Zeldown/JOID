package com.example.describing.flex;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.layout.FlexNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.property.OverflowProperty;
import dev.joid.lib.utils.align.Align;

public class FlexCasesUI extends UI {

	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	@Override
	public void init() {
		RectNode
		.create(60, 100, 400, 300)
		.color(FlexCasesUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(FlexCasesUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region column
			FlexNode
			.vertical(20, 20, 360)
			.margin(10D)
			.body(flex -> {
				RectNode.create(0, 0, 200, 50).color(FlexCasesUI.ACCENT).attach(flex);
				RectNode.create(0, 0, 120, 50).color(FlexCasesUI.ACCENT).attach(flex);
				RectNode.create(0, 0, 280, 50).color(FlexCasesUI.ACCENT).attach(flex);
				RectNode.create(0, 0, 160, 50).color(FlexCasesUI.ACCENT).attach(flex);
			})
			.attach(card);
			// #endregion
		})
		.attach(this);

		RectNode
		.create(520, 100, 400, 300)
		.color(FlexCasesUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(FlexCasesUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region row
			FlexNode
			.horizontal(20, 20, 260)
			.margin(10D)
			.body(flex -> {
				RectNode.create(0, 0, 60, 120).color(FlexCasesUI.ACCENT).attach(flex);
				RectNode.create(0, 0, 90, 200).color(FlexCasesUI.ACCENT).attach(flex);
				RectNode.create(0, 0, 60, 260).color(FlexCasesUI.ACCENT).attach(flex);
				RectNode.create(0, 0, 120, 80).color(FlexCasesUI.ACCENT).attach(flex);
			})
			.attach(card);
			// #endregion
		})
		.attach(this);

		RectNode
		.create(980, 100, 400, 300)
		.color(FlexCasesUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(FlexCasesUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region align
			FlexNode
			.vertical(20, 20, 360)
			.margin(10D)
			.align(Align.CENTER)
			.body(flex -> {
				RectNode.create(0, 0, 200, 50).color(FlexCasesUI.ACCENT).attach(flex);
				RectNode.create(0, 0, 120, 50).color(FlexCasesUI.ACCENT).attach(flex);
				RectNode.create(0, 0, 280, 50).color(FlexCasesUI.ACCENT).attach(flex);
				RectNode.create(0, 0, 160, 50).color(FlexCasesUI.ACCENT).attach(flex);
			})
			.attach(card);
			// #endregion
		})
		.attach(this);

		RectNode
		.create(1440, 100, 400, 300)
		.color(FlexCasesUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(FlexCasesUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region offset
			FlexNode
			.vertical(20, 20, 360)
			.margin(10D)
			.body(flex -> {
				RectNode.create(0, 0, 200, 50).color(FlexCasesUI.ACCENT).attach(flex);
				RectNode.create(40, 30, 200, 50).color(FlexCasesUI.ELEMENT).attach(flex);
				RectNode.create(0, 0, 200, 50).color(FlexCasesUI.ACCENT).attach(flex);
			})
			.attach(card);
			// #endregion
		})
		.attach(this);

		RectNode
		.create(60, 460, 400, 300)
		.color(FlexCasesUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(FlexCasesUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region grow
			final FlexNode list = FlexNode
			.vertical(20, 20, 320)
			.margin(10D)
			.body(flex -> {
				RectNode.create(0, 0, 320, 50).color(FlexCasesUI.ACCENT).attach(flex);
				RectNode.create(0, 0, 320, 50).color(FlexCasesUI.ACCENT).attach(flex);
				RectNode.create(0, 0, 320, 50).color(FlexCasesUI.ACCENT).attach(flex);
			});
			RectNode
			.create(20, 20, 360, 0)
			.color(FlexCasesUI.ELEMENT)
			.height(() -> list.getHeight() + 40D)
			.body(panel -> list.attach(panel))
			.attach(card);
			// #endregion
		})
		.attach(this);

		RectNode
		.create(520, 460, 400, 300)
		.color(FlexCasesUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(FlexCasesUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region scroll
			FlexNode
			.vertical(20, 20, 360)
			.margin(10D)
			.height(260)
			.overflow(OverflowProperty.SCROLL)
			.body(flex -> {
				for (int i = 0; i < 8; i++) {
					RectNode.create(0, 0, 360, 50).color(i % 2 == 0 ? FlexCasesUI.ACCENT : FlexCasesUI.ELEMENT).attach(flex);
				}
			})
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}