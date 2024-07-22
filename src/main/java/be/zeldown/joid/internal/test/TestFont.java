package be.zeldown.joid.internal.test;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.font.FontLoader;
import be.zeldown.joid.lib.font.dto.font.FontInputStream;
import be.zeldown.joid.lib.font.dto.font.impl.CustomFont;

public class TestFont {

	public static CustomFont MONTSERRAT;
	public static CustomFont BATUPHAT;
	public static CustomFont SPACE_GROTESK;

	public static void load() {
		{
			final FontInputStream regular = new FontInputStream(JOID.class.getResourceAsStream("/test/fonts/Montserrat-Regular/font.json"), JOID.class.getResourceAsStream("/test/fonts/Montserrat-Regular/font.png"));
			final FontInputStream bold = new FontInputStream(JOID.class.getResourceAsStream("/test/fonts/Montserrat-Bold/font.json"), JOID.class.getResourceAsStream("/test/fonts/Montserrat-Bold/font.png"));
			FontLoader.load(regular, bold, font -> TestFont.MONTSERRAT = font);
		}

		{
			final FontInputStream regular = new FontInputStream(JOID.class.getResourceAsStream("/test/fonts/Batuphat-Script/font.json"), JOID.class.getResourceAsStream("/test/fonts/Batuphat-Script/font.png"));
			FontLoader.load(regular, font -> TestFont.BATUPHAT = font);
		}

		{
			final FontInputStream regular = new FontInputStream(JOID.class.getResourceAsStream("/test/fonts/Space-Grotesk/font.json"), JOID.class.getResourceAsStream("/test/fonts/Space-Grotesk/font.png"));
			FontLoader.load(regular, font -> TestFont.SPACE_GROTESK = font);
		}
	}

	public static boolean isLoaded() {
		return TestFont.MONTSERRAT != null && TestFont.BATUPHAT != null && TestFont.SPACE_GROTESK != null;
	}

}