package be.zeldown.joid.demo;

import java.io.InputStream;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.font.impl.msdf.MsdfFont;
import be.zeldown.joid.lib.font.impl.msdf.MsdfFontLoader;

public class DemoFont {

	public static MsdfFont MONTSERRAT;
	public static MsdfFont BATUPHAT;
	public static MsdfFont SPACE_GROTESK;

	public static void load() {
		DemoFont.MONTSERRAT = MsdfFontLoader.load(DemoFont.get("/assets/demo/fonts/Montserrat-Regular/")).join();
		DemoFont.BATUPHAT = MsdfFontLoader.load(DemoFont.get("/assets/demo/fonts/Batuphat-Script/")).join();
		DemoFont.SPACE_GROTESK = MsdfFontLoader.load(DemoFont.get("/assets/demo/fonts/Space-Grotesk/")).join();
	}

	private static InputStream get(final String path) {
		final String folder = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
		return JOID.class.getResourceAsStream(folder + "/font.msdf");
	}

	public static boolean isLoaded() {
		return DemoFont.MONTSERRAT != null && DemoFont.BATUPHAT != null && DemoFont.SPACE_GROTESK != null;
	}

}