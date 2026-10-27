package com.example.further;

import java.awt.image.BufferedImage;

import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.impl.glyph.IFontFace;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;

// #region face
public class FontsPixelFontFace implements IFontFace {

	private static final ResourceBuilder ATLAS = ResourceBuilder.create().cache(null).nearest().mipmap(false);

	private final int[]    advances;
	private final Resource atlas;

	protected FontsPixelFontFace(final int[] advances, final Resource atlas) {
		this.advances = advances;
		this.atlas = atlas;
	}

	public static FontsPixelFontFace from(final BufferedImage image) {
		final int[] advances = new int[95];
		advances[0] = 4;
		for (int codepoint = '!'; codepoint <= '~'; codepoint++) {
			final int left = FontsPixelFontFace.cellX(codepoint);
			final int top = FontsPixelFontFace.cellY(codepoint);
			int width = 0;
			for (int x = 0; x < FontsPixelFont.SIZE; x++) {
				for (int y = 0; y < FontsPixelFont.SIZE; y++) {
					if (image.getRGB(left + x, top + y) >>> 24 != 0) {
						width = x + 1;
					}
				}
			}
			advances[codepoint - ' '] = width + 1;
		}
		return new FontsPixelFontFace(advances, FontsPixelFontFace.ATLAS.of(image).blocking());
	}

	public Resource getAtlas() {
		return this.atlas;
	}

	@Override
	public boolean isItalic() {
		return false;
	}

	@Override
	public float getAscender() {
		return 7F / FontsPixelFont.SIZE;
	}

	@Override
	public float getDescender() {
		return -2F / FontsPixelFont.SIZE;
	}

	@Override
	public float getLineHeight() {
		return 9F / FontsPixelFont.SIZE;
	}

	@Override
	public float getUnderlineY() {
		return -1F / FontsPixelFont.SIZE;
	}

	@Override
	public float getUnderlineThickness() {
		return 1F / FontsPixelFont.SIZE;
	}

	@Override
	public String getName() {
		return "Pixel";
	}

	@Override
	public FontWeight getWeight() {
		return FontWeight.REGULAR;
	}

	@Override
	public float getAdvance(final int codepoint) {
		return this.hasGlyph(codepoint) ? (float) this.advances[codepoint - ' '] / FontsPixelFont.SIZE : 0F;
	}

	@Override
	public float getKerning(final int previous, final int current) {
		return 0F;
	}

	@Override
	public boolean hasGlyph(final int codepoint) {
		return codepoint >= ' ' && codepoint <= '~';
	}

	public static int cellX(final int codepoint) {
		return (codepoint - ' ') % 16 * FontsPixelFont.SIZE;
	}

	public static int cellY(final int codepoint) {
		return (codepoint - ' ') / 16 * FontsPixelFont.SIZE;
	}

}
// #endregion