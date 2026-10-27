package com.example.tutorial.menu;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.font.impl.msdf.MsdfFontLoader;

public final class Theme {

	public static final MsdfFont FONT = MsdfFontLoader.load(
		"fonts/Montserrat/Montserrat-Regular.ttf",
		"fonts/Montserrat/Montserrat-Bold.ttf"
	).join();

	public static final Color INK = new Color(153, 153, 153).lock();

	private Theme() {}

}