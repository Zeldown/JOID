package be.zeldown.joid.demo;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.font.dto.font.FontInputStream;
import be.zeldown.joid.lib.font.impl.custom.CustomFont;
import be.zeldown.joid.lib.font.impl.custom.CustomFontLoader;

public class DemoFont {

	public static CustomFont MONTSERRAT;
	public static CustomFont BATUPHAT;
	public static CustomFont SPACE_GROTESK;

	public static void load() {
		DemoFont.MONTSERRAT = CustomFontLoader.load(DemoFont.get("/assets/test/fonts/Montserrat-Regular/")).join();
		DemoFont.BATUPHAT = CustomFontLoader.load(DemoFont.get("/assets/test/fonts/Batuphat-Script/")).join();
		DemoFont.SPACE_GROTESK = CustomFontLoader.load(DemoFont.get("/assets/test/fonts/Space-Grotesk/")).join();
	}

	private static FontInputStream get(String path) {
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}

		return new FontInputStream(JOID.class.getResourceAsStream(path + "/font.json"), JOID.class.getResourceAsStream(path + "/font.png"));
	}

	public static boolean isLoaded() {
		return DemoFont.MONTSERRAT != null && DemoFont.BATUPHAT != null && DemoFont.SPACE_GROTESK != null;
	}

}