package be.zeldown.joid.internal.font;

import java.io.InputStream;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.font.impl.msdf.MsdfFont;
import be.zeldown.joid.lib.font.impl.msdf.MsdfFontLoader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InternalFont {

	public static MsdfFont MONTSERRAT;

	public static void load() {
		InternalFont.MONTSERRAT = MsdfFontLoader.load(InternalFont.get("Thin"), InternalFont.get("ExtraLight"), InternalFont.get("Light"), InternalFont.get("Regular"), InternalFont.get("Medium"), InternalFont.get("SemiBold"), InternalFont.get("Bold"), InternalFont.get("ExtraBold"), InternalFont.get("Black")).join();
	}

	private static InputStream get(final String weight) {
		return JOID.class.getResourceAsStream("/assets/dev/fonts/Montserrat-" + weight + "/font.msdf");
	}

}