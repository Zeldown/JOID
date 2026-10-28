package com.example.style;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.render.transform.Rotation;
import dev.joid.lib.render.transform.Transformation;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect.NodeEffectScope;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.MaskNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.effect.impl.TransformNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class EffectsUI extends UI {

	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();
	private static final Color ACTIVE  = new Color(125, 164, 255).lock();

	@Override
	public void init() {
		// #region stack
		RectNode
		.create(150, 100, 300, 180)
		.color(EffectsUI.SURFACE)
		.effect(RoundedNodeEffect.create(24F))
		.effect(BorderNodeEffect.create(EffectsUI.ACCENT, 2F))
		.effect(ShadowNodeEffect.create(EffectsUI.ACCENT, 24F).offsetY(8D))
		.attach(this);
		// #endregion

		// #region hover
		RectNode
		.create(610, 100, 300, 180)
		.color(EffectsUI.SURFACE)
		.self(rect -> {
			rect.effect(RoundedNodeEffect.create(() -> 8F + rect.hoverValue(40F)));
			rect.effect(BorderNodeEffect.create(EffectsUI.ACCENT, 1F).width(() -> 1F + rect.hoverValue(5F)));
		})
		.attach(this);
		// #endregion

		// #region scope
		RectNode
		.create(1070, 80, 300, 220)
		.color(EffectsUI.SURFACE)
		.effect(RoundedNodeEffect.create(30F))
		.body(card -> {
			RectNode.create(0, 0, 300, 70).color(EffectsUI.ACCENT).attach(card);
		})
		.attach(this);

		RectNode
		.create(1470, 80, 300, 220)
		.color(EffectsUI.SURFACE)
		.effect(RoundedNodeEffect.create(30F).scope(NodeEffectScope.CHILDREN))
		.body(card -> {
			RectNode.create(0, 0, 300, 70).color(EffectsUI.ACCENT).attach(card);
		})
		.attach(this);
		// #endregion

		RectNode
		.create(250, 480, 200, 200)
		.color(EffectsUI.ACCENT.withAlpha(0.3F))
		.effect(TransformNodeEffect.create(Transformation.create().rotate(20D, Rotation.ROLL)))
		.attach(this);

		RectNode
		.create(650, 480, 200, 200)
		.color(EffectsUI.ACCENT.withAlpha(0.3F))
		.effect(TransformNodeEffect.create(Transformation.create().rotate(20D, Rotation.ROLL)))
		.attach(this);

		// #region priority
		RectNode
		.create(250, 480, 200, 200)
		.color(EffectsUI.ACTIVE)
		.effect(TransformNodeEffect.create(Transformation.create().rotate(20D, Rotation.ROLL)))
		.effect(MaskNodeEffect.create().y(70D).height(60D))
		.attach(this);

		RectNode
		.create(650, 480, 200, 200)
		.color(EffectsUI.ACTIVE)
		.effect(TransformNodeEffect.create(Transformation.create().rotate(20D, Rotation.ROLL)))
		.effect(MaskNodeEffect.create().y(70D).height(60D).priority(-200))
		.attach(this);
		// #endregion

		// #region clip
		RectNode
		.create(1270, 480, 300, 200)
		.onDraw(event -> {
			final Node node = event.getNode();
			final double centerX = node.getX() + node.getWidth() / 2D;
			final double centerY = node.getY() + node.getHeight() / 2D;
			super.mask(node.getX(), node.getY(), node.getWidth(), node.getHeight() / 2D, () -> {
				DrawUtils.SHAPE.drawCircle(centerX, centerY, EffectsUI.ACCENT, 90D);
			});
		})
		.attach(this);
		// #endregion
	}

}