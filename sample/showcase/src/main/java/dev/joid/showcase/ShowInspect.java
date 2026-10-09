package dev.joid.showcase;

import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;

public class ShowInspect extends ShowLists {

	@Override
	protected void scene() {
		super.scene();
		RectNode
		.create(440, 80, 1040, 920)
		.color(() -> ShowUI.vertical(this.veil(0D), this.veil(0.25D)))
		.visible(() -> this.t() < 0.8D)
		.effect(RoundedNodeEffect.create(34F))
		.attach(this);
	}

	private Color veil(final double delay) {
		return Color.decode("#120C2A").copyAlpha((float) (1D - this.ease(TweenEquations.QUAD_INOUT, 0.1D + delay, 0.55D + delay)));
	}

}