package com.example.tutorial.setup;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.font.impl.msdf.MsdfFontLoader;

public final class Theme {

	public static final MsdfFont FONT = MsdfFontLoader.load(
		Theme.class.getResourceAsStream("/fonts/Montserrat/Montserrat-Regular.ttf"),
		Theme.class.getResourceAsStream("/fonts/Montserrat/Montserrat-Bold.ttf")
	).join();

	public static final Color INK = new Color(153, 153, 153).lock();

	private Theme() {}

}