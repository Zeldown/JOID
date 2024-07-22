package be.zeldown.joid.lib.font.dto.font;

import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import be.zeldown.joid.lib.texture.VolatileTexture;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Font {

	@NonNull private final FontInfo fontInfo;
	@NonNull private final InputStream atlas;

	private VolatileTexture texture;

	public VolatileTexture getTexture() {
		if (this.texture != null) {
			return this.texture;
		}

		try {
			this.texture = new VolatileTexture(ImageIO.read(this.atlas));
		} catch (final IOException e) {
			e.printStackTrace();
		}

		return this.texture;
	}

}