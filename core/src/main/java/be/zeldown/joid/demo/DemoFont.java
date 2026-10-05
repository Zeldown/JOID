package be.zeldown.joid.demo;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.asset.Asset;
import be.zeldown.joid.lib.font.impl.custom.CustomFont;
import be.zeldown.joid.lib.font.impl.custom.CustomFontLoader;

public class DemoFont {

	public static CustomFont MONTSERRAT;
	public static CustomFont BATUPHAT;
	public static CustomFont SPACE_GROTESK;

	public static void load() {
		DemoFont.MONTSERRAT = CustomFontLoader.load(DemoFont.get("/assets/demo/fonts/Montserrat-Regular/")).join();
		DemoFont.BATUPHAT = CustomFontLoader.load(DemoFont.get("/assets/demo/fonts/Batuphat-Script/")).join();
		DemoFont.SPACE_GROTESK = CustomFontLoader.load(DemoFont.get("/assets/demo/fonts/Space-Grotesk/")).join();
	}

	private static Asset get(String path) {
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}

		return Asset.of(JOID.class.getResourceAsStream(path + "/font.msdf"));
	}

	public static boolean isLoaded() {
		return DemoFont.MONTSERRAT != null && DemoFont.BATUPHAT != null && DemoFont.SPACE_GROTESK != null;
	}

}