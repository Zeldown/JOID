package dev.joid.showcase;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.control.slider.DoubleSliderNode;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import lombok.NonNull;

public class ShowSliderNode extends DoubleSliderNode {

	private final Color to;
	private final Color from;

	protected ShowSliderNode(final double x, final double y, final double width, final Color from, final Color to) {
		super(x, y, width, 36);
		this.from = from;
		this.to = to;
		super.track(ContainerNode.create(0, 0, 0, 0).body(track -> {
			RectNode
			.create(0, 13, 0, 10)
			.color(Color.WHITE.withAlpha(0.12F))
			.<RectNode>width(track::getWidth)
			.effect(RoundedNodeEffect.create(5F))
			.attach(track);
			RectNode
			.create(0, 13, 0, 10)
			.color(this.from.toGradient(this.to))
			.<RectNode>width(() -> 18D + super.getProgress() * (track.getWidth() - 36D))
			.effect(RoundedNodeEffect.create(5F))
			.effect(ShadowNodeEffect.create(this.to.withAlpha(0.6F), 14F))
			.attach(track);
		}));
		super.thumb(CircleNode
		.create(0, 0, 36)
		.color(Color.WHITE)
		.effect(ShadowNodeEffect.create(this.to.withAlpha(0.9F), 18F))
		.body(thumb -> {
			CircleNode.create(11, 11, 14).color(this.to).attach(thumb);
		}));
	}

	public static @NonNull ShowSliderNode create(final double x, final double y, final double width, final @NonNull Color from, final @NonNull Color to) {
		return new ShowSliderNode(x, y, width, from, to);
	}

}