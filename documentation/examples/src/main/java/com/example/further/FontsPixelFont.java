package com.example.further;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import javax.imageio.ImageIO;

import dev.joid.lib.font.ITextRenderer;
import dev.joid.lib.font.impl.bitmap.BitmapFont;
import dev.joid.lib.font.impl.glyph.FontFamily;

// #region font
public class FontsPixelFont extends BitmapFont<FontsPixelFontFace> {

	public static final int SIZE = 8;

	protected FontsPixelFont(final FontFamily<FontsPixelFontFace> family) {
		super(family, FontsPixelFont.SIZE);
	}

	public static FontsPixelFont load(final String path) {
		try (final InputStream stream = FontsPixelFont.class.getResourceAsStream(path)) {
			final BufferedImage image = ImageIO.read(stream);
			return new FontsPixelFont(FontFamily.of(FontsPixelFontFace.from(image)));
		} catch (final IOException exception) {
			throw new UncheckedIOException("The pixel font " + path + " cannot be read", exception);
		}
	}

	@Override
	public ITextRenderer getTextRenderer() {
		return FontsPixelTextRenderer.inst();
	}

}
// #endregion