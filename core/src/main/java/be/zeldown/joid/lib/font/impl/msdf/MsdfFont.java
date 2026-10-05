package be.zeldown.joid.lib.font.impl.msdf;

import be.zeldown.joid.lib.font.IFontProvider;
import be.zeldown.joid.lib.font.impl.glyph.GlyphFont;
import be.zeldown.joid.lib.font.impl.glyph.dto.FontFamily;
import be.zeldown.joid.lib.font.impl.msdf.dto.MsdfFace;
import lombok.NonNull;

public final class MsdfFont extends GlyphFont<MsdfFace> {

	private MsdfFont(final FontFamily<MsdfFace> family) {
		super(family);
	}

	public static @NonNull MsdfFont create(final @NonNull MsdfFace @NonNull... faces) {
		return new MsdfFont(FontFamily.of(faces));
	}

	@Override
	public @NonNull IFontProvider getFontProvider() {
		return MsdfFontProvider.inst();
	}

}