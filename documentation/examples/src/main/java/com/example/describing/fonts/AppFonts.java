package com.example.describing.fonts;

import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.font.impl.msdf.MsdfFontLoader;

// #region class
public final class AppFonts {

	public static final MsdfFont MONTSERRAT = MsdfFontLoader.load(
		"fonts/Montserrat/Montserrat-Regular.ttf",
		"fonts/Montserrat/Montserrat-Bold.ttf"
	).join();

	private AppFonts() {}

}
// #endregion