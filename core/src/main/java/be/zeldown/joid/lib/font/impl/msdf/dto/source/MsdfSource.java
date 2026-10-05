package be.zeldown.joid.lib.font.impl.msdf.dto.source;

import java.io.IOException;

import be.zeldown.joid.lib.font.FontWeight;
import be.zeldown.joid.lib.font.impl.msdf.dto.MsdfFace;
import lombok.NonNull;

@SuppressWarnings("unchecked")
public abstract class MsdfSource implements IMsdfSource {

	private Boolean    italic;
	private FontWeight weight;

	@Override
	public final @NonNull MsdfFace read() throws IOException {
		final MsdfFace face = this.parse();
		if (this.weight == null && this.italic == null) {
			return face;
		}

		return face.style(this.weight == null ? face.getWeight() : this.weight, this.italic == null ? face.isItalic() : this.italic);
	}

	protected abstract @NonNull MsdfFace parse() throws IOException;

	public final <T extends MsdfSource> @NonNull T italic(final boolean italic) {
		this.italic = italic;
		return (T) this;
	}

	public final <T extends MsdfSource> @NonNull T weight(final @NonNull FontWeight weight) {
		this.weight = weight;
		return (T) this;
	}

}