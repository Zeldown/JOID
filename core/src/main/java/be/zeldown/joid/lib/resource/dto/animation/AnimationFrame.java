package be.zeldown.joid.lib.resource.dto.animation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class AnimationFrame {

	private final int[] pixels;
	private final long  duration;

	public static @NonNull AnimationFrame create(final @NonNull int[] pixels, final long duration) {
		return new AnimationFrame(pixels, duration);
	}

}