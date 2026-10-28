package com.example.catalog;

import com.example.capture.CaptureStyle;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public abstract class CatalogUI extends UI {

	public static final Color INK         = CaptureStyle.ACCENT;
	public static final Color TEXT        = CaptureStyle.TEXT;
	public static final Color ELEMENT     = CaptureStyle.STROKE;
	public static final Color PLACEHOLDER = CaptureStyle.SURFACE;
	public static final Color ACTIVE      = new Color(125, 164, 255).lock();

	protected final RectNode card(final int index) {
		return RectNode
		.create(40 + index % 5 * 370, 40 + index / 5 * 340, 340, 300)
		.color(CatalogUI.PLACEHOLDER)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(CatalogUI.ELEMENT, 1.5F));
	}

}