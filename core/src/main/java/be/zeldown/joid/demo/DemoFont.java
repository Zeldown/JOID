package be.zeldown.joid.demo;

import java.io.InputStream;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.font.FontLoader;
import be.zeldown.joid.lib.font.impl.custom.CustomFont;

public class DemoFont {

	public static CustomFont MONTSERRAT;
	public static CustomFont BATUPHAT;
	public static CustomFont SPACE_GROTESK;

	public static void load() {
		DemoFont.MONTSERRAT = FontLoader.load(DemoFont.get("/assets/demo/fonts/Montserrat-Regular/")).join();
		DemoFont.BATUPHAT = FontLoader.load(DemoFont.get("/assets/demo/fonts/Batuphat-Script/")).join();
		DemoFont.SPACE_GROTESK = FontLoader.load(DemoFont.get("/assets/demo/fonts/Space-Grotesk/")).join();
	}

	private static InputStream get(String path) {
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}

		return JOID.class.getResourceAsStream(path + "/font.msdf");
	}

	public static boolean isLoaded() {
		return DemoFont.MONTSERRAT != null && DemoFont.BATUPHAT != null && DemoFont.SPACE_GROTESK != null;
	}

}