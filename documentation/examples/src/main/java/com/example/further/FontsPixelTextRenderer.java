package com.example.further;

import dev.joid.lib.font.impl.bitmap.BitmapCell;
import dev.joid.lib.font.impl.bitmap.BitmapTextRenderer;
import dev.joid.lib.font.impl.glyph.TextGlyph;
import dev.joid.lib.resource.Resource;

// #region renderer
public final class FontsPixelTextRenderer extends BitmapTextRenderer<FontsPixelFontFace> {

	private static final FontsPixelTextRenderer INSTANCE = new FontsPixelTextRenderer();

	private FontsPixelTextRenderer() {}

	public static FontsPixelTextRenderer inst() {
		return FontsPixelTextRenderer.INSTANCE;
	}

	@Override
	protected BitmapCell getCell(final TextGlyph<FontsPixelFontFace> glyph) {
		final int codepoint = glyph.getCodepoint();
		final Resource atlas = glyph.getFace().getAtlas();
		atlas.prepareBind();
		if (codepoint == ' ' || !glyph.hasGlyph(codepoint) || atlas.getTexture() == null) {
			return null;
		}

		final int x = FontsPixelFontFace.cellX(codepoint);
		final int y = FontsPixelFontFace.cellY(codepoint);
		final boolean bold = glyph.getStyle().getWeight().getValue() > glyph.getFace().getWeight().getValue();
		return BitmapCell
		.create(atlas.getTexture(), x, y, x + FontsPixelFont.SIZE, y + FontsPixelFont.SIZE)
		.bold(bold);
	}

}
// #endregion