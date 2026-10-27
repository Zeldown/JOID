package com.example.further;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.effect.NodeEffect.NodeEffectScope;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class EffectsUI extends UI {

	private static final Color INK  = new Color(153, 153, 153).lock();
	private static final Color DARK = new Color(85, 85, 85).lock();

	@Override
	public void init() {
		// #region shadow
		RectNode
		.create(100, 100, 200, 120)
		.color(Color.WHITE)
		.effect(HardShadowNodeEffect.create(EffectsUI.DARK, 12D))
		.attach(this);
		// #endregion

		// #region lift
		RectNode
		.create(400, 100, 200, 120)
		.color(EffectsUI.INK)
		.effect(LiftNodeEffect.create(12D))
		.attach(this);
		// #endregion

		// #region invert
		RectNode
		.create(700, 100, 200, 120)
		.color(EffectsUI.DARK)
		.effect(InvertNodeEffect.create(1F))
		.attach(this);

		RectNode
		.create(1000, 100, 200, 120)
		.color(EffectsUI.DARK)
		.effect(InvertNodeEffect.create(1F).scope(NodeEffectScope.CHILDREN))
		.body(rect -> CircleNode.create(60, 20, 80).color(Color.WHITE).attach(rect))
		.attach(this);
		// #endregion
	}

}