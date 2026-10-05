package be.zeldown.joid.lib.font.impl.msdf.dto.source;

import java.io.IOException;

import be.zeldown.joid.lib.font.impl.msdf.dto.MsdfFontFace;
import lombok.NonNull;

public interface IMsdfSource {

	public @NonNull MsdfFontFace read() throws IOException;

}