package be.zeldown.joid.lib.font.impl.msdf.dto.source;

import java.io.IOException;

import be.zeldown.joid.lib.font.impl.msdf.dto.MsdfFace;
import lombok.NonNull;

public interface IMsdfSource {

	public @NonNull MsdfFace read() throws IOException;

}