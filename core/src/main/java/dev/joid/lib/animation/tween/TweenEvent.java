package dev.joid.lib.animation.tween;

import dev.joid.lib.ui.event.Event;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TweenEvent extends Event {

	private final BaseTween<?> tween;

	public static @NonNull TweenEvent create(final @NonNull BaseTween<?> tween) {
		return new TweenEvent(tween);
	}

}