package com.example.catalog;

import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class TransitionsCatalogUI extends CatalogUI {

	@Override
	public void init() {
		RectNode
		.create(790, 390, 340, 300)
		.color(CatalogUI.PLACEHOLDER)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(CatalogUI.ELEMENT, 1.5F))
		.attach(this);
	}

}