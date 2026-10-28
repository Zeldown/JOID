package com.example.guides;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.font.impl.msdf.MsdfFontLoader;

public final class FontGuideStyle {

	// #region font
	public static final MsdfFont FONT = MsdfFontLoader.load(
		"/fonts/Montserrat/Montserrat-Regular.ttf",
		"/fonts/Montserrat/Montserrat-Bold.ttf"
	).join();
	// #endregion

	// #region styles
	private static final Color TEXT = new Color(231, 233, 238).lock();

	public static final TextInfo BODY    = TextInfo.create(FontGuideStyle.FONT, 24F).color(FontGuideStyle.TEXT).lock();
	public static final TextInfo TITLE   = FontGuideStyle.BODY.withFontSize(40F).withWeight(FontWeight.BOLD).lock();
	public static final TextInfo TOOLTIP = FontGuideStyle.BODY.withFontSize(20F).withColor(FontGuideStyle.TEXT).lock();
	// #endregion

	private FontGuideStyle() {}

}