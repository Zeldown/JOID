package com.example.further;

import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.font.impl.msdf.MsdfFontLoader;
import dev.joid.lib.font.impl.msdf.source.OpenTypeMsdfSource;

public final class FontsSources {

	// #region override
	public static final MsdfFont LIGHT = MsdfFontLoader.load(
		OpenTypeMsdfSource
		.of(FontsSources.class.getResourceAsStream("/fonts/Montserrat/Montserrat-Regular.ttf"))
		.weight(FontWeight.LIGHT)
	).join();
	// #endregion

	// #region atlas
	public static final MsdfFont MONTSERRAT = MsdfFontLoader.load(
		FontsSources.class.getResourceAsStream("/fonts/Montserrat-Regular/font.msdf"),
		FontsSources.class.getResourceAsStream("/fonts/Montserrat-Bold/font.msdf")
	).join();
	// #endregion

	private FontsSources() {}

}