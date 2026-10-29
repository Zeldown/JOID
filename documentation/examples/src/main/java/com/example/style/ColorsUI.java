package com.example.style;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.NodeEffect.NodeEffectScope;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.utils.geom.Point;

public class ColorsUI extends UI {

	// #region palette
	private static final Color BRAND = Color.decode("#3366CC").lock();
	// #endregion

	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color BAND    = new Color(231, 233, 238).lock();

	@Override
	public void init() {
		RectNode
		.create(360, 60, 1200, 260)
		.color(ColorsUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F).scope(NodeEffectScope.CHILDREN))
		.effect(BorderNodeEffect.create(ColorsUI.ELEMENT, 1.5F).scope(NodeEffectScope.CHILDREN))
		.body(card -> {
			// #region create
			RectNode.create(0, 130, 1200, 130).color(ColorsUI.BAND).attach(card);
			RectNode.create(60, 60, 140, 140).color(Color.GRAY).attach(card);
			RectNode.create(260, 60, 140, 140).color(new Color(0.2F, 0.4F, 0.8F)).attach(card);
			RectNode.create(460, 60, 140, 140).color(new Color(230, 120, 50)).attach(card);
			RectNode.create(660, 60, 140, 140).color(Color.decode("#55AA55")).attach(card);
			RectNode.create(860, 60, 140, 140).color(Color.decode("rgba(51, 102, 204, 0.5)")).attach(card);
			// #endregion
		})
		.attach(this);

		RectNode
		.create(360, 410, 1200, 260)
		.color(ColorsUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F).scope(NodeEffectScope.CHILDREN))
		.effect(BorderNodeEffect.create(ColorsUI.ELEMENT, 1.5F).scope(NodeEffectScope.CHILDREN))
		.body(card -> {
			// #region derive
			RectNode.create(0, 130, 1200, 130).color(ColorsUI.BAND).attach(card);
			RectNode.create(60, 60, 140, 140).color(ColorsUI.BRAND).attach(card);
			RectNode.create(240, 60, 140, 140).color(ColorsUI.BRAND.darker()).attach(card);
			RectNode.create(420, 60, 140, 140).color(ColorsUI.BRAND.darker(0.25F)).attach(card);
			RectNode.create(600, 60, 140, 140).color(ColorsUI.BRAND.brighter(0.6F)).attach(card);
			RectNode.create(780, 60, 140, 140).color(ColorsUI.BRAND.withAlpha(0.5F)).attach(card);
			RectNode.create(960, 60, 140, 140).color(ColorsUI.BRAND.to(Color.WHITE, 0.5F)).attach(card);
			// #endregion
		})
		.attach(this);

		RectNode
		.create(360, 760, 1200, 260)
		.color(ColorsUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F).scope(NodeEffectScope.CHILDREN))
		.effect(BorderNodeEffect.create(ColorsUI.ELEMENT, 1.5F).scope(NodeEffectScope.CHILDREN))
		.body(card -> {
			// #region gradient
			RectNode.create(0, 130, 1200, 130).color(ColorsUI.BAND).attach(card);
			final Point top = Point.of(0D, 0D);
			final Point bottom = Point.of(0D, 1D);
			final Point corner = Point.of(1D, 1D);
			final Color clear = ColorsUI.BRAND.withAlpha(0F);
			RectNode.create(60, 60, 240, 140).color(ColorsUI.BRAND.toGradient(Color.WHITE)).attach(card);
			RectNode.create(340, 60, 240, 140).color(ColorsUI.BRAND.toGradient(Color.WHITE, top, bottom)).attach(card);
			RectNode.create(620, 60, 240, 140).color(ColorsUI.BRAND.toGradient(Color.WHITE, top, corner)).attach(card);
			RectNode.create(900, 60, 240, 140).color(ColorsUI.BRAND.toGradient(clear)).attach(card);
			// #endregion
		})
		.attach(this);
	}

}