package be.zeldown.joid.internal.font;

import java.io.InputStream;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.font.impl.msdf.MsdfFont;
import be.zeldown.joid.lib.font.impl.msdf.MsdfFontLoader;

public class InternalFont {

	public static MsdfFont MONTSERRAT_THIN;
	public static MsdfFont MONTSERRAT_EXTRA_LIGHT;
	public static MsdfFont MONTSERRAT_LIGHT;
	public static MsdfFont MONTSERRAT_REGULAR;
	public static MsdfFont MONTSERRAT_MEDIUM;
	public static MsdfFont MONTSERRAT_SEMI_BOLD;
	public static MsdfFont MONTSERRAT_BOLD;
	public static MsdfFont MONTSERRAT_EXTRA_BOLD;
	public static MsdfFont MONTSERRAT_BLACK;

	public static void load() {
		InternalFont.MONTSERRAT_THIN = MsdfFontLoader.load(InternalFont.get("/assets/dev/fonts/Montserrat-Thin/")).join();
		InternalFont.MONTSERRAT_EXTRA_LIGHT = MsdfFontLoader.load(InternalFont.get("/assets/dev/fonts/Montserrat-ExtraLight/")).join();
		InternalFont.MONTSERRAT_LIGHT = MsdfFontLoader.load(InternalFont.get("/assets/dev/fonts/Montserrat-Light/")).join();
		InternalFont.MONTSERRAT_REGULAR = MsdfFontLoader.load(InternalFont.get("/assets/dev/fonts/Montserrat-Regular/")).join();
		InternalFont.MONTSERRAT_MEDIUM = MsdfFontLoader.load(InternalFont.get("/assets/dev/fonts/Montserrat-Medium/")).join();
		InternalFont.MONTSERRAT_SEMI_BOLD = MsdfFontLoader.load(InternalFont.get("/assets/dev/fonts/Montserrat-SemiBold/")).join();
		InternalFont.MONTSERRAT_BOLD = MsdfFontLoader.load(InternalFont.get("/assets/dev/fonts/Montserrat-Bold/")).join();
		InternalFont.MONTSERRAT_EXTRA_BOLD = MsdfFontLoader.load(InternalFont.get("/assets/dev/fonts/Montserrat-ExtraBold/")).join();
		InternalFont.MONTSERRAT_BLACK = MsdfFontLoader.load(InternalFont.get("/assets/dev/fonts/Montserrat-Black/")).join();
	}

	private static InputStream get(String path) {
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}

		return JOID.class.getResourceAsStream(path + "/font.msdf");
	}

}