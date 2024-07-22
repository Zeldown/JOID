package be.zeldown.joid.lib.font.dto.font.impl;

import be.zeldown.joid.lib.font.FontProvider;
import be.zeldown.joid.lib.font.dto.font.Font;
import be.zeldown.joid.lib.font.dto.font.IFont;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import be.zeldown.joid.lib.font.impl.CustomFontRenderer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public final class CustomFont implements IFont {

	@NonNull private final Font regular;
	@NonNull private final Font bold;

	public @NonNull TextInfo info(final int fontSize) {
		return TextInfo.create(this, fontSize);
	}

	@Override
	public @NonNull FontProvider getFontProvider() {
		return CustomFontRenderer.inst();
	}

}