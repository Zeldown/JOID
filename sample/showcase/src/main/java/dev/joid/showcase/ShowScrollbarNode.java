package dev.joid.showcase;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.control.scrollbar.ScrollbarNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class ShowScrollbarNode extends ScrollbarNode {

	protected ShowScrollbarNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.track(RectNode.create(0, 0, 0, 0).color(Color.WHITE.withAlpha(0.06F)).effect(RoundedNodeEffect.create((float) width / 2F)));
		super.thumb(RectNode
		.create(0, 0, 0, 0)
		.color(ShowUI.vertical(ShowUI.FUCHSIA, ShowUI.VIOLET))
		.effect(RoundedNodeEffect.create((float) width / 2F))
		.effect(ShadowNodeEffect.create(ShowUI.VIOLET.withAlpha(0.6F), 12F)));
	}

	public static ShowScrollbarNode create(final double x, final double y, final double width, final double height) {
		return new ShowScrollbarNode(x, y, width, height);
	}

}