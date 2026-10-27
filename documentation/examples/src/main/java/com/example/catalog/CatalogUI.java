package com.example.catalog;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public abstract class CatalogUI extends UI {

	public static final Color INK         = new Color(153, 153, 153).lock();
	public static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	protected final RectNode card(final int index) {
		return RectNode.create(40 + index % 5 * 370, 40 + index / 5 * 340, 340, 300).color(CatalogUI.PLACEHOLDER);
	}

}