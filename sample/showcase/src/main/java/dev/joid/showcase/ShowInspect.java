package dev.joid.showcase;

import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class ShowInspect extends ShowLists {

	private static final Color VEIL = Color.decode("#120C2A").lock();

	@Override
	protected void scene() {
		super.scene();
		RectNode
		.create(440, 80, 1040, 920)
		.color(() -> ShowUI.vertical(this.veil(0D), this.veil(0.25D)))
		.visible(() -> super.t() < 0.8D)
		.interactive(false)
		.effect(RoundedNodeEffect.create(34F))
		.attach(this);
	}

	private Color veil(final double delay) {
		return ShowInspect.VEIL.withAlpha((float) (1D - super.ease(TweenEquations.QUAD_IN_OUT, 0.1D + delay, 0.55D + delay)));
	}

}