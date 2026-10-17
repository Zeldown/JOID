package dev.joid.showcase;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.control.toggle.ToggleNode;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import lombok.NonNull;

public class ShowThemeToggleNode extends ToggleNode<Boolean, Boolean> {

	private static final Color MOON        = Color.decode("#F5F3FF").lock();
	private static final Color TRACK_DARK  = Color.decode("#2A2846").lock();
	private static final Color TRACK_LIGHT = Color.decode("#E4E4EE").lock();

	protected ShowThemeToggleNode(final double x, final double y) {
		super(x, y, 112, 56);
		super.speed(0.15F);
		super.track(RectNode.create(0, 0, 0, 0).color(this::getTrackColor).effect(RoundedNodeEffect.create((float) super.dh(2))));
		super.knob(ContainerNode.create(0, 0, super.getHeight(), super.getHeight()).body(knob -> {
			CircleNode
			.create(8, 8, 40)
			.color(() -> ShowUI.AMBER.to(ShowThemeToggleNode.MOON, super.getKnobProgress()))
			.effect(ShadowNodeEffect.create(ShowUI.AMBER, 18F).color(() -> ShowUI.AMBER.to(ShowUI.VIOLET, super.getKnobProgress()).withAlpha(0.7F)))
			.attach(knob);
			CircleNode
			.create(28, 28, 0)
			.color(this::getTrackColor)
			.<CircleNode>x(() -> 28D - 6D * super.getKnobProgress())
			.<CircleNode>y(() -> 28D - 22D * super.getKnobProgress())
			.<CircleNode>width(() -> 30D * super.getKnobProgress())
			.<CircleNode>height(() -> 30D * super.getKnobProgress())
			.attach(knob);
		}));
	}

	public static @NonNull ShowThemeToggleNode create(final double x, final double y) {
		return new ShowThemeToggleNode(x, y);
	}

	private Color getTrackColor() {
		return ShowThemeToggleNode.TRACK_LIGHT.to(ShowThemeToggleNode.TRACK_DARK, super.getKnobProgress());
	}

}