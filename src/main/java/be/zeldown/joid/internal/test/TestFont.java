package be.zeldown.joid.internal.test;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.font.dto.font.FontInputStream;
import be.zeldown.joid.lib.font.dto.font.impl.CustomFont;
import be.zeldown.joid.lib.font.impl.CustomFontLoader;

public class TestFont {

	public static CustomFont MONTSERRAT;
	public static CustomFont BATUPHAT;
	public static CustomFont SPACE_GROTESK;

	public static void load() {
		TestFont.MONTSERRAT = CustomFontLoader.load(TestFont.get("/test/fonts/Montserrat-Regular/"), TestFont.get("/test/fonts/Montserrat-Bold/")).join();
		TestFont.BATUPHAT = CustomFontLoader.load(TestFont.get("/test/fonts/Batuphat-Script/")).join();
		TestFont.SPACE_GROTESK = CustomFontLoader.load(TestFont.get("/test/fonts/Space-Grotesk/")).join();
	}

	private static FontInputStream get(String path) {
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}

		return new FontInputStream(JOID.get(path + "/font.json"), JOID.get(path + "/font.png"));
	}

	public static boolean isLoaded() {
		return TestFont.MONTSERRAT != null && TestFont.BATUPHAT != null && TestFont.SPACE_GROTESK != null;
	}

}