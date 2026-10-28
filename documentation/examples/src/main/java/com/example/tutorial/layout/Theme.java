package com.example.tutorial.layout;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.font.impl.msdf.MsdfFontLoader;

public final class Theme {

	public static final MsdfFont FONT = MsdfFontLoader.load(
		"fonts/Montserrat/Montserrat-Regular.ttf",
		"fonts/Montserrat/Montserrat-Bold.ttf"
	).join();

	public static final Color TEXT    = new Color(231, 233, 238).lock();
	public static final Color ELEMENT = new Color(38, 43, 53).lock();
	public static final Color SURFACE = new Color(23, 26, 33).lock();

	private Theme() {}

}