package com.example.tutorial.state;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.font.impl.msdf.MsdfFontLoader;

public final class Theme {

	public static final MsdfFont FONT = MsdfFontLoader.load(
		"fonts/Montserrat/Montserrat-Regular.ttf",
		"fonts/Montserrat/Montserrat-Bold.ttf"
	).join();

	public static final Color INK   = new Color(153, 153, 153).lock();
	public static final Color DARK  = new Color(85, 85, 85).lock();
	public static final Color PAPER = new Color(221, 221, 221).lock();

	private Theme() {}

}