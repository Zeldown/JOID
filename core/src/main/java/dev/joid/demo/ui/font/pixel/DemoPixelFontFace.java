package dev.joid.demo.ui.font.pixel;

import java.awt.image.BufferedImage;

import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.impl.glyph.dto.IFontFace;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import lombok.Getter;
import lombok.NonNull;

public final class DemoPixelFontFace implements IFontFace {

	private static final ResourceBuilder BUILDER = ResourceBuilder.create().cache(null).blocking().nearest().mipmap(false);

	private final int[] advances;

	@Getter private final Resource texture;

	private DemoPixelFontFace(final int[] advances, final Resource texture) {
		this.advances = advances;
		this.texture = texture;
	}

	public static @NonNull DemoPixelFontFace create(final @NonNull BufferedImage atlas) {
		final int[] advances = new int[95];
		advances[0] = DemoPixelFont.SIZE / 2;
		for (int codepoint = '!'; codepoint <= '~'; codepoint++) {
			int width = 0;
			for (int x = 0; x < DemoPixelFont.SIZE; x++) {
				for (int y = 0; y < DemoPixelFont.SIZE; y++) {
					if (atlas.getRGB(DemoPixelFontFace.cellX(codepoint) + x, DemoPixelFontFace.cellY(codepoint) + y) >>> 24 != 0) {
						width = x + 1;
					}
				}
			}
			advances[codepoint - ' '] = width + 1;
		}

		return new DemoPixelFontFace(advances, DemoPixelFontFace.BUILDER.of(atlas));
	}

	@Override
	public boolean isItalic() {
		return false;
	}

	@Override
	public float getAscender() {
		return 7F / DemoPixelFont.SIZE;
	}

	@Override
	public float getDescender() {
		return -2F / DemoPixelFont.SIZE;
	}

	@Override
	public float getLineHeight() {
		return 9F / DemoPixelFont.SIZE;
	}

	@Override
	public float getUnderlineY() {
		return -1F / DemoPixelFont.SIZE;
	}

	@Override
	public float getUnderlineThickness() {
		return 1F / DemoPixelFont.SIZE;
	}

	@Override
	public @NonNull String getName() {
		return "Pixel";
	}

	@Override
	public @NonNull FontWeight getWeight() {
		return FontWeight.REGULAR;
	}

	@Override
	public float getAdvance(final int codepoint) {
		return this.hasGlyph(codepoint) ? (float) this.advances[codepoint - ' '] / DemoPixelFont.SIZE : 0F;
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
		return (codepoint - ' ') % 16 * DemoPixelFont.SIZE;
	}

	public static int cellY(final int codepoint) {
		return (codepoint - ' ') / 16 * DemoPixelFont.SIZE;
	}

}