package dev.joid.lib.ui.shortcut;

import dev.joid.lib.input.key.Key;
import dev.joid.lib.ui.event.Event;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShortcutEvent extends Event {

	private final Key      key;
	private final Shortcut shortcut;

	public static @NonNull ShortcutEvent create(final @NonNull Key key, final @NonNull Shortcut shortcut) {
		return new ShortcutEvent(key, shortcut);
	}

}