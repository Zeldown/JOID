package dev.joid.showcase;

import dev.joid.lib.shader.impl.BorderShader.BorderMode;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.impl.control.slider.DoubleSliderNode;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import lombok.NonNull;

public class ShowPaperSliderNode extends DoubleSliderNode {

	protected ShowPaperSliderNode(final double x, final double y, final double width) {
		super(x, y, width, 36);
		super.track(ContainerNode.create(0, 0, 0, 0).body(track -> {
			RectNode
			.create(0, 10, 0, 16)
			.color(ShowPaperKit.CARD)
			.<RectNode>width(track::getWidth)
			.effect(BorderNodeEffect.create(ShowPaperKit.INK, 3F).mode(BorderMode.IN))
			.attach(track);
			RectNode
			.create(0, 10, 0, 16)
			.color(ShowPaperKit.TOMATO)
			.<RectNode>width(() -> 18D + super.getProgress() * (track.getWidth() - 36D))
			.effect(BorderNodeEffect.create(ShowPaperKit.INK, 3F).mode(BorderMode.IN))
			.attach(track);
			for (int i = 1; i < 10; i++) {
				final double tick = i / 10D;
				RectNode.create(0, 30, 2, 8).color(ShowPaperKit.INK.withAlpha(0.35F)).<RectNode>x(() -> track.getWidth() * tick - 1D).attach(track);
			}
		}));
		super.thumb(ContainerNode.create(0, 0, 36, 36).body(thumb -> {
			RectNode.create(5, 5, 36, 36).color(ShowPaperKit.INK).attach(thumb);
			RectNode.create(0, 0, 36, 36).color(ShowPaperKit.BUTTER).effect(BorderNodeEffect.create(ShowPaperKit.INK, 3F).mode(BorderMode.IN)).attach(thumb);
		}));
	}

	public static @NonNull ShowPaperSliderNode create(final double x, final double y, final double width) {
		return new ShowPaperSliderNode(x, y, width);
	}

}