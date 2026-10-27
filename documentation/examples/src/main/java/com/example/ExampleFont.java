package com.example;

import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.font.impl.msdf.MsdfFontLoader;

public final class ExampleFont {

	public static final MsdfFont REGULAR = MsdfFontLoader.load(
		"fonts/Montserrat/Montserrat-Regular.ttf",
		"fonts/Montserrat/Montserrat-Bold.ttf"
	).join();

	private ExampleFont() {}

}