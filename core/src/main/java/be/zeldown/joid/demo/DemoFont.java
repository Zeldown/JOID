package be.zeldown.joid.demo;

import java.io.InputStream;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.internal.font.InternalFont;
import be.zeldown.joid.lib.font.impl.msdf.MsdfFont;
import be.zeldown.joid.lib.font.impl.msdf.MsdfFontLoader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DemoFont {

	public static MsdfFont BATUPHAT;
	public static MsdfFont MONTSERRAT;
	public static MsdfFont SPACE_GROTESK;

	public static void load() {
		DemoFont.MONTSERRAT = InternalFont.MONTSERRAT;
		DemoFont.BATUPHAT = MsdfFontLoader.load(DemoFont.get("Batuphat-Script")).join();
		DemoFont.SPACE_GROTESK = MsdfFontLoader.load(DemoFont.get("Space-Grotesk")).join();
	}

	public static boolean isLoaded() {
		return DemoFont.MONTSERRAT != null && DemoFont.BATUPHAT != null && DemoFont.SPACE_GROTESK != null;
	}

	private static InputStream get(final String name) {
		return JOID.class.getResourceAsStream("/assets/demo/fonts/" + name + "/font.msdf");
	}

}