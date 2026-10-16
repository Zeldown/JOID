package dev.joid.demo.ui.node.impl.control.toggle;

import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.control.toggle.BaseToggleNode;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import lombok.NonNull;

public class DemoPillToggleNode extends BaseToggleNode<Boolean, Boolean> {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	private final TweenAnimator knob;

	protected DemoPillToggleNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.knob = TweenAnimator.create(0F);

		super.<DemoPillToggleNode>onChange(event -> event.getNode().knob.sequence(250F, event.getValue() ? 1F : 0F, TweenEquations.QUAD_OUT).start());
	}

	public static @NonNull DemoPillToggleNode create(final double x, final double y, final double width, final double height) {
		return new DemoPillToggleNode(x, y, width, height);
	}

	@Override
	public void init(final @NonNull UI ui) {
		super.clearChildren();
		this.knob.value(super.isToggle() ? 1F : 0F);

		RectNode
		.create(0, 0, super.getWidth(), super.getHeight())
		.color(() -> DemoPillToggleNode.PLACEHOLDER.to(DemoPillToggleNode.INK, this.knob.get()))
		.effect(RoundedNodeEffect.create((float) super.dh(2)))
		.attach(this);

		CircleNode
		.create(0, 4, super.getHeight() - 8D)
		.color(Color.WHITE)
		.<CircleNode>x(() -> 4D + (super.getWidth() - super.getHeight()) * this.knob.get())
		.attach(this);
	}

}