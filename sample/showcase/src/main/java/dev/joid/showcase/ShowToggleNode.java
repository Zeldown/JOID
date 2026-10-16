package dev.joid.showcase;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.control.toggle.ToggleNode;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import lombok.NonNull;

public class ShowToggleNode extends ToggleNode<Boolean, Boolean> {

	private final Color on;

	protected ShowToggleNode(final double x, final double y, final Color on) {
		super(x, y, 76, 42);
		this.on = on;
		super.track(RectNode
		.create(0, 0, 0, 0)
		.color(() -> Color.WHITE.withAlpha(0.14F).to(this.on, super.getKnobProgress()))
		.effect(RoundedNodeEffect.create((float) super.dh(2)))
		.effect(ShadowNodeEffect.create(() -> this.on.withAlpha(0.7F * super.getKnobProgress()), () -> 18F)));
		super.knob(ContainerNode.create(0, 0, super.getHeight(), super.getHeight()).body(knob -> {
			CircleNode.create(5, 5, super.getHeight() - 10D).color(Color.WHITE).attach(knob);
		}));
	}

	public static @NonNull ShowToggleNode create(final double x, final double y, final @NonNull Color on) {
		return new ShowToggleNode(x, y, on);
	}

}