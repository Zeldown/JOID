package com.example.catalog;

import dev.joid.lib.color.Color;
import dev.joid.lib.render.transform.Rotation;
import dev.joid.lib.render.transform.Transformation;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.node.effect.impl.BlurNodeEffect;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.CircleNodeEffect;
import dev.joid.lib.ui.node.effect.impl.MaskNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.effect.impl.TransformNodeEffect;
import dev.joid.lib.ui.node.impl.resource.ResourceNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class EffectsCatalogUI extends CatalogUI {

	@Override
	public void init() {
		super.card(0).body(card -> RectNode.create(70, 80, 200, 140).color(CatalogUI.INK).effect(RoundedNodeEffect.create(30F)).attach(card)).attach(this);

		super.card(1).body(card -> RectNode.create(90, 70, 160, 160).color(CatalogUI.INK).effect(CircleNodeEffect.create()).attach(card)).attach(this);

		super.card(2).body(card -> RectNode.create(70, 80, 200, 140).color(Color.WHITE).effect(BorderNodeEffect.create(CatalogUI.INK, 8F)).attach(card)).attach(this);

		super.card(3).body(card -> RectNode.create(70, 70, 200, 140).color(Color.WHITE).effect(ShadowNodeEffect.create(CatalogUI.INK, 24F).offsetY(12D)).attach(card)).attach(this);

		super.card(4).body(card -> ResourceNode.create(10, 60, 320, 180).resource(Resource.of("images/placeholder.png")).effect(BlurNodeEffect.create(6F)).attach(card)).attach(this);

		super.card(5).body(card -> {
			RectNode.create(70, 80, 200, 140).color(Color.WHITE).effect(BorderNodeEffect.create(CatalogUI.INK, 2F)).attach(card);
			RectNode.create(70, 80, 200, 140).color(CatalogUI.INK.toGradient(Color.WHITE)).effect(MaskNodeEffect.create().x(50D).y(30D).width(100D).height(80D)).attach(card);
		})
		.attach(this);

		super.card(6).body(card -> RectNode.create(90, 90, 160, 120).color(CatalogUI.INK).effect(TransformNodeEffect.create(Transformation.create().rotate(20D, Rotation.ROLL))).attach(card)).attach(this);
	}

}