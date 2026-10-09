package dev.joid.demo;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import javax.imageio.ImageIO;

import dev.joid.demo.ui.font.pixel.DemoPixelFont;
import dev.joid.internal.JOID;
import dev.joid.internal.font.DevFont;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.font.impl.msdf.MsdfFontLoader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DemoFont {

	public static MsdfFont PACIFICO;
	public static MsdfFont MONTSERRAT;
	public static MsdfFont PLAYFAIR_DISPLAY;

	public static DemoPixelFont PIXEL;

	public static void load() {
		DemoFont.MONTSERRAT = DevFont.MONTSERRAT;
		DemoFont.PACIFICO = MsdfFontLoader.load(DemoFont.get("Pacifico/Pacifico-Regular.ttf")).join();
		DemoFont.PLAYFAIR_DISPLAY = MsdfFontLoader.load(DemoFont.get("Playfair-Display/PlayfairDisplay.ttf")).join();
		DemoFont.PIXEL = DemoPixelFont.create(DemoFont.read("Pixel/Pixel.png"));
	}

	public static boolean isLoaded() {
		return DemoFont.MONTSERRAT != null && DemoFont.PACIFICO != null && DemoFont.PLAYFAIR_DISPLAY != null && DemoFont.PIXEL != null;
	}

	private static InputStream get(final String file) {
		return JOID.class.getResourceAsStream("/assets/demo/fonts/" + file);
	}

	private static BufferedImage read(final String file) {
		try (InputStream stream = DemoFont.get(file)) {
			return ImageIO.read(stream);
		} catch (final IOException exception) {
			throw new UncheckedIOException(exception);
		}
	}

}