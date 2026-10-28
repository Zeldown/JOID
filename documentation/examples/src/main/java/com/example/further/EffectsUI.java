package com.example.further;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.NodeEffect.NodeEffectScope;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class EffectsUI extends UI {

	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	@Override
	public void init() {
		// #region shadow
		RectNode
		.create(100, 100, 200, 120)
		.color(EffectsUI.ELEMENT)
		.effect(HardShadowNodeEffect.create(EffectsUI.ACCENT, 12D))
		.attach(this);
		// #endregion

		// #region lift
		RectNode
		.create(400, 100, 200, 120)
		.color(EffectsUI.ACCENT)
		.effect(LiftNodeEffect.create(12D))
		.attach(this);
		// #endregion

		// #region invert
		RectNode
		.create(700, 100, 200, 120)
		.color(EffectsUI.ELEMENT)
		.effect(InvertNodeEffect.create(1F))
		.body(rect -> CircleNode.create(60, 20, 80).color(EffectsUI.ACCENT).attach(rect))
		.attach(this);

		RectNode
		.create(1000, 100, 200, 120)
		.color(EffectsUI.ELEMENT)
		.effect(InvertNodeEffect.create(1F).scope(NodeEffectScope.CHILDREN))
		.body(rect -> CircleNode.create(60, 20, 80).color(EffectsUI.ACCENT).attach(rect))
		.attach(this);
		// #endregion
	}

}