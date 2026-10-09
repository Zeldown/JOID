package dev.joid.demo.ui.font.pixel;

import dev.joid.lib.font.impl.bitmap.BitmapCell;
import dev.joid.lib.font.impl.bitmap.BitmapTextRenderer;
import dev.joid.lib.font.impl.glyph.TextGlyph;
import dev.joid.lib.resource.Resource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DemoPixelTextRenderer extends BitmapTextRenderer<DemoPixelFontFace> {

	private static final DemoPixelTextRenderer INSTANCE = new DemoPixelTextRenderer();

	public static @NonNull DemoPixelTextRenderer inst() {
		return DemoPixelTextRenderer.INSTANCE;
	}

	@Override
	protected BitmapCell getCell(final @NonNull TextGlyph<DemoPixelFontFace> glyph) {
		final int codepoint = glyph.getCodepoint();
		final Resource atlas = glyph.getFace().getTexture();
		atlas.prepareBind();
		if (codepoint == ' ' || !glyph.hasGlyph(codepoint) || atlas.getTexture() == null) {
			return null;
		}

		final int cellX = DemoPixelFontFace.cellX(codepoint);
		final int cellY = DemoPixelFontFace.cellY(codepoint);
		return BitmapCell.create(atlas.getTexture(), cellX, cellY, cellX + DemoPixelFont.SIZE, cellY + DemoPixelFont.SIZE);
	}

}