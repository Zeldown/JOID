package dev.joid.lib.bridge.render.texture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextureSampling {

	private static final List<TextureSampling> VALUES;

	static {
		final List<TextureSampling> valueList = new ArrayList<>();
		for (final boolean mipmapped : new boolean[] {false, true}) {
			for (final TextureFilter filter : TextureFilter.values()) {
				for (final TextureWrap wrap : TextureWrap.values()) {
					valueList.add(new TextureSampling(valueList.size(), wrap, mipmapped, filter));
				}
			}
		}
		VALUES = Collections.unmodifiableList(valueList);
	}

	private final int           index;
	private final TextureWrap   wrap;
	private final boolean       mipmapped;
	private final TextureFilter filter;

	public static @NonNull TextureSampling of(final @NonNull TextureFilter filter, final @NonNull TextureWrap wrap, final boolean mipmapped) {
		return TextureSampling.VALUES.get((mipmapped ? TextureFilter.values().length * TextureWrap.values().length : 0) + filter.ordinal() * TextureWrap.values().length + wrap.ordinal());
	}

	public static @NonNull List<@NonNull TextureSampling> values() {
		return TextureSampling.VALUES;
	}

	public boolean isMipmapFiltered() {
		return this.mipmapped && this.filter == TextureFilter.LINEAR;
	}

}