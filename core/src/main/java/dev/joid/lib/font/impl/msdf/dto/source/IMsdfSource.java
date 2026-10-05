package dev.joid.lib.font.impl.msdf.dto.source;

import java.io.IOException;

import dev.joid.lib.font.impl.msdf.dto.MsdfFontFace;
import lombok.NonNull;

public interface IMsdfSource {

	public @NonNull MsdfFontFace read() throws IOException;

	public default @NonNull String describe() {
		return "read from " + this.getClass().getSimpleName();
	}

}