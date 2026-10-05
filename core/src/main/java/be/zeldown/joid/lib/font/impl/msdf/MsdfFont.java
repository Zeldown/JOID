package be.zeldown.joid.lib.font.impl.msdf;

import be.zeldown.joid.lib.font.IFont;
import be.zeldown.joid.lib.font.IFontProvider;
import be.zeldown.joid.lib.font.dto.TextInfo;
import be.zeldown.joid.lib.font.impl.msdf.dto.MsdfFace;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public final class MsdfFont implements IFont {

	@NonNull private final MsdfFace regular;
	@NonNull private final MsdfFace bold;

	public @NonNull TextInfo info(final int fontSize) {
		return TextInfo.create(this, fontSize);
	}

	@Override
	public @NonNull IFontProvider getFontProvider() {
		return MsdfFontProvider.inst();
	}

}