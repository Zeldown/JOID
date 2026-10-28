package com.example.guides;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.control.checkbox.CheckboxNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class RoundGuideCheckboxNode extends CheckboxNode {

	// #region skin
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	protected RoundGuideCheckboxNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		final RectNode box = RectNode
		.create(0, 0, 0, 0)
		.color(RoundGuideCheckboxNode.ELEMENT)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(RoundGuideCheckboxNode.ACCENT, 2F));
		final RectNode mark = RectNode
		.create(0, 0, width / 2D, height / 2D)
		.color(RoundGuideCheckboxNode.ACCENT)
		.effect(RoundedNodeEffect.create(6F));

		super.box(box);
		super.mark(mark);
	}
	// #endregion

	public static RoundGuideCheckboxNode create(final double x, final double y, final double width, final double height) {
		return new RoundGuideCheckboxNode(x, y, width, height);
	}

}