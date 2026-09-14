package be.zeldown.joid.lib.font.dto.font;

import java.io.InputStream;

import be.zeldown.joid.lib.resource.Resource;
import be.zeldown.joid.lib.resource.ResourceBuilder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Font {

	private static final ResourceBuilder BUILDER = ResourceBuilder.create().cache(null).blocking().linear();

	@NonNull private final FontInfo fontInfo;
	@NonNull private final InputStream atlas;

	private Resource texture;

	public Resource getTexture() {
		if (this.texture != null) {
			return this.texture;
		}

		this.texture = Font.BUILDER.of(this.atlas);
		return this.texture;
	}

}