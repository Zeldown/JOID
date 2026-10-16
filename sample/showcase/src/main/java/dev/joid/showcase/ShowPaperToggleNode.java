package dev.joid.showcase;

import dev.joid.lib.shader.impl.BorderShader.BorderMode;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.impl.control.toggle.ToggleNode;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import lombok.NonNull;

public class ShowPaperToggleNode extends ToggleNode<Boolean, Boolean> {

	protected ShowPaperToggleNode(final double x, final double y) {
		super(x, y, 76, 42);
		super.track(ContainerNode.create(0, 0, 0, 0).body(track -> {
			RectNode.create(5, 5, 0, 0).color(ShowPaperKit.INK).<RectNode>width(track::getWidth).<RectNode>height(track::getHeight).attach(track);
			RectNode
			.create(0, 0, 0, 0)
			.color(() -> ShowPaperKit.CARD.to(ShowPaperKit.TOMATO, super.getKnobProgress()))
			.<RectNode>width(track::getWidth)
			.<RectNode>height(track::getHeight)
			.effect(BorderNodeEffect.create(ShowPaperKit.INK, 3F).mode(BorderMode.IN))
			.attach(track);
		}));
		super.knob(ContainerNode.create(0, 0, super.getHeight(), super.getHeight()).body(knob -> {
			RectNode
			.create(7, 7, 28, 28)
			.color(() -> ShowPaperKit.CARD.to(ShowPaperKit.INK, super.getKnobProgress()))
			.effect(BorderNodeEffect.create(ShowPaperKit.INK, 3F).mode(BorderMode.IN))
			.attach(knob);
		}));
	}

	public static @NonNull ShowPaperToggleNode create(final double x, final double y) {
		return new ShowPaperToggleNode(x, y);
	}

}