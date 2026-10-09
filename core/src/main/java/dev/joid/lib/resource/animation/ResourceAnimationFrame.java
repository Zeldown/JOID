package dev.joid.lib.resource.animation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceAnimationFrame {

	private final int[] pixels;
	private final long  duration;

	public static @NonNull ResourceAnimationFrame create(final @NonNull int[] pixels, final long duration) {
		return new ResourceAnimationFrame(pixels, duration);
	}

}