package dev.joid.internal.font;

import java.io.InputStream;

import dev.joid.internal.JOID;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.font.impl.msdf.MsdfFontLoader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DevFont {

	public static MsdfFont MONTSERRAT;

	public static void load() {
		DevFont.MONTSERRAT = MsdfFontLoader.load(DevFont.get("Thin"), DevFont.get("ExtraLight"), DevFont.get("Light"), DevFont.get("Regular"), DevFont.get("Medium"), DevFont.get("SemiBold"), DevFont.get("Bold"), DevFont.get("ExtraBold"), DevFont.get("Black")).join();
	}

	private static InputStream get(final String weight) {
		return JOID.class.getResourceAsStream("/assets/dev/fonts/Montserrat/Montserrat-" + weight + ".ttf");
	}

}