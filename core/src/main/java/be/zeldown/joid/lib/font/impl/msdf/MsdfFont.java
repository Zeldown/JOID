package be.zeldown.joid.lib.font.impl.msdf;

import be.zeldown.joid.lib.font.IFontProvider;
import be.zeldown.joid.lib.font.impl.glyph.GlyphFont;
import be.zeldown.joid.lib.font.impl.glyph.dto.FontFamily;
import be.zeldown.joid.lib.font.impl.msdf.dto.MsdfFontFace;
import lombok.NonNull;

public final class MsdfFont extends GlyphFont<MsdfFontFace> {

	private MsdfFont(final FontFamily<MsdfFontFace> family) {
		super(family);
	}

	public static @NonNull MsdfFont create(final @NonNull MsdfFontFace @NonNull... faces) {
		return new MsdfFont(FontFamily.of(faces));
	}

	@Override
	public @NonNull IFontProvider getFontProvider() {
		return MsdfFontProvider.inst();
	}

}