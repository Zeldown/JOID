package be.zeldown.joid.internal.font;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.font.dto.font.FontInputStream;
import be.zeldown.joid.lib.font.impl.custom.CustomFont;
import be.zeldown.joid.lib.font.impl.custom.CustomFontLoader;

public class InternalFont {

	public static CustomFont MONTSERRAT_THIN;
	public static CustomFont MONTSERRAT_EXTRA_LIGHT;
	public static CustomFont MONTSERRAT_LIGHT;
	public static CustomFont MONTSERRAT_REGULAR;
	public static CustomFont MONTSERRAT_MEDIUM;
	public static CustomFont MONTSERRAT_SEMI_BOLD;
	public static CustomFont MONTSERRAT_BOLD;
	public static CustomFont MONTSERRAT_EXTRA_BOLD;
	public static CustomFont MONTSERRAT_BLACK;

	public static void load() {
		InternalFont.MONTSERRAT_THIN = CustomFontLoader.load(InternalFont.get("/assets/dev/fonts/Montserrat-Thin/")).join();
		InternalFont.MONTSERRAT_EXTRA_LIGHT = CustomFontLoader.load(InternalFont.get("/assets/dev/fonts/Montserrat-ExtraLight/")).join();
		InternalFont.MONTSERRAT_LIGHT = CustomFontLoader.load(InternalFont.get("/assets/dev/fonts/Montserrat-Light/")).join();
		InternalFont.MONTSERRAT_REGULAR = CustomFontLoader.load(InternalFont.get("/assets/dev/fonts/Montserrat-Regular/")).join();
		InternalFont.MONTSERRAT_MEDIUM = CustomFontLoader.load(InternalFont.get("/assets/dev/fonts/Montserrat-Medium/")).join();
		InternalFont.MONTSERRAT_SEMI_BOLD = CustomFontLoader.load(InternalFont.get("/assets/dev/fonts/Montserrat-SemiBold/")).join();
		InternalFont.MONTSERRAT_BOLD = CustomFontLoader.load(InternalFont.get("/assets/dev/fonts/Montserrat-Bold/")).join();
		InternalFont.MONTSERRAT_EXTRA_BOLD = CustomFontLoader.load(InternalFont.get("/assets/dev/fonts/Montserrat-ExtraBold/")).join();
		InternalFont.MONTSERRAT_BLACK = CustomFontLoader.load(InternalFont.get("/assets/dev/fonts/Montserrat-Black/")).join();
	}

	private static FontInputStream get(String path) {
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}

		return new FontInputStream(JOID.class.getResourceAsStream(path + "/font.json"), JOID.class.getResourceAsStream(path + "/font.png"));
	}

}