package be.zeldown.joid.internal.font;

import java.io.InputStream;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.font.FontLoader;
import be.zeldown.joid.lib.font.impl.custom.CustomFont;

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
		InternalFont.MONTSERRAT_THIN = FontLoader.load(InternalFont.get("/assets/dev/fonts/Montserrat-Thin/")).join();
		InternalFont.MONTSERRAT_EXTRA_LIGHT = FontLoader.load(InternalFont.get("/assets/dev/fonts/Montserrat-ExtraLight/")).join();
		InternalFont.MONTSERRAT_LIGHT = FontLoader.load(InternalFont.get("/assets/dev/fonts/Montserrat-Light/")).join();
		InternalFont.MONTSERRAT_REGULAR = FontLoader.load(InternalFont.get("/assets/dev/fonts/Montserrat-Regular/")).join();
		InternalFont.MONTSERRAT_MEDIUM = FontLoader.load(InternalFont.get("/assets/dev/fonts/Montserrat-Medium/")).join();
		InternalFont.MONTSERRAT_SEMI_BOLD = FontLoader.load(InternalFont.get("/assets/dev/fonts/Montserrat-SemiBold/")).join();
		InternalFont.MONTSERRAT_BOLD = FontLoader.load(InternalFont.get("/assets/dev/fonts/Montserrat-Bold/")).join();
		InternalFont.MONTSERRAT_EXTRA_BOLD = FontLoader.load(InternalFont.get("/assets/dev/fonts/Montserrat-ExtraBold/")).join();
		InternalFont.MONTSERRAT_BLACK = FontLoader.load(InternalFont.get("/assets/dev/fonts/Montserrat-Black/")).join();
	}

	private static InputStream get(String path) {
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}

		return JOID.class.getResourceAsStream(path + "/font.msdf");
	}

}