package com.example.catalog;

import dev.joid.lib.ui.node.impl.shape.RectNode;

public class TransitionsCatalogUI extends CatalogUI {

	@Override
	public void init() {
		RectNode.create(790, 390, 340, 300).color(CatalogUI.PLACEHOLDER).attach(this);
	}

}