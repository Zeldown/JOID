package dev.joid.lib.font;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FontBounds {

	private final double width;
	private final double height;

	public static @NonNull FontBounds empty() {
		return new FontBounds(0D, 0D);
	}

	public static @NonNull FontBounds create(final double width, final double height) {
		return new FontBounds(width, height);
	}

}