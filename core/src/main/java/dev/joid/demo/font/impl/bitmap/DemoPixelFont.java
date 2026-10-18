package dev.joid.demo.font.impl.bitmap;

import java.awt.image.BufferedImage;

import dev.joid.lib.font.ITextRenderer;
import dev.joid.lib.font.impl.bitmap.BitmapFont;
import dev.joid.lib.font.impl.glyph.FontFamily;
import lombok.NonNull;

public final class DemoPixelFont extends BitmapFont<DemoPixelFontFace> {

	public static final int SIZE = 8;

	private DemoPixelFont(final FontFamily<DemoPixelFontFace> family) {
		super(family, DemoPixelFont.SIZE);
	}

	public static @NonNull DemoPixelFont from(final @NonNull BufferedImage atlas) {
		return new DemoPixelFont(FontFamily.of(DemoPixelFontFace.from(atlas)));
	}

	@Override
	public @NonNull ITextRenderer getTextRenderer() {
		return DemoPixelTextRenderer.inst();
	}

}