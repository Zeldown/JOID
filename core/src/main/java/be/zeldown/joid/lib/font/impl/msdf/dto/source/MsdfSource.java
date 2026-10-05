package be.zeldown.joid.lib.font.impl.msdf.dto.source;

import java.io.IOException;

import be.zeldown.joid.lib.font.FontWeight;
import be.zeldown.joid.lib.font.impl.msdf.dto.MsdfFontFace;
import lombok.NonNull;

@SuppressWarnings("unchecked")
public abstract class MsdfSource implements IMsdfSource {

	private Boolean    italic;
	private FontWeight weight;

	@Override
	public final @NonNull MsdfFontFace read() throws IOException {
		final MsdfFontFace face = this.parse();
		if (this.weight == null && this.italic == null) {
			return face;
		}

		return face.style(this.weight == null ? face.getWeight() : this.weight, this.italic == null ? face.isItalic() : this.italic);
	}

	public final <T extends MsdfSource> @NonNull T italic(final boolean italic) {
		this.italic = italic;
		return (T) this;
	}

	public final <T extends MsdfSource> @NonNull T weight(final @NonNull FontWeight weight) {
		this.weight = weight;
		return (T) this;
	}

	protected abstract @NonNull MsdfFontFace parse() throws IOException;

}