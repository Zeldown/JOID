package com.example.catalog;

import dev.joid.lib.input.cursor.Cursor;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class CursorsCatalogUI extends CatalogUI {

	@Override
	public void init() {
		for (final Cursor cursor : Cursor.values()) {
			super.card(cursor.ordinal()).body(card -> RectNode.create(70, 80, 200, 140).color(CatalogUI.ELEMENT).cursor(cursor).attach(card)).attach(this);
		}
	}

}