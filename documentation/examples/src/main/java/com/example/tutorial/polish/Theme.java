package com.example.tutorial.polish;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.font.impl.msdf.MsdfFontLoader;

public final class Theme {

	public static final MsdfFont FONT = MsdfFontLoader.load(
		"fonts/Montserrat/Montserrat-Regular.ttf",
		"fonts/Montserrat/Montserrat-Bold.ttf"
	).join();

	public static final Color HOVER   = new Color(54, 61, 77).lock();
	public static final Color TEXT    = new Color(231, 233, 238).lock();
	public static final Color ELEMENT = new Color(38, 43, 53).lock();
	public static final Color SURFACE = new Color(23, 26, 33).lock();
	public static final Color SHADOW  = new Color(0, 0, 0, 153).lock();

	private Theme() {}

}