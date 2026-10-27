package com.example.kit;

import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.control.toggle.BaseToggleNode;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

// #region pill
public class KitPillToggleNode extends BaseToggleNode<Boolean, Boolean> {

	private final TweenAnimator knob;

	protected KitPillToggleNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		this.knob = TweenAnimator.create(0F);
		super.<KitPillToggleNode>onChange(event -> {
			this.knob.sequence(250F, event.getValue() ? 1F : 0F, TweenEquations.QUAD_OUT).start();
		});
	}

	public static KitPillToggleNode create(final double x, final double y, final double width, final double height) {
		return new KitPillToggleNode(x, y, width, height);
	}

	@Override
	public void init(final UI ui) {
		super.clearChildren();
		this.knob.value(super.isToggle() ? 1F : 0F);

		RectNode
		.create(0, 0, super.getWidth(), super.getHeight())
		.color(() -> KitColors.SURFACE.to(KitColors.INK, this.knob.get()))
		.effect(RoundedNodeEffect.create((float) super.dh(2D)))
		.attach(this);

		CircleNode
		.create(0, 4, super.getHeight() - 8D)
		.color(() -> this.knob.get() > 0.5F ? KitColors.SURFACE : KitColors.INK)
		.<CircleNode>x(() -> 4D + (super.getWidth() - super.getHeight()) * this.knob.get())
		.attach(this);
	}

}
// #endregion