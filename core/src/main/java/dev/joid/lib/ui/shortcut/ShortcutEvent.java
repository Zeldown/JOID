package dev.joid.lib.ui.shortcut;

import dev.joid.lib.input.key.Key;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ShortcutEvent {

	@NonNull private final Key      key;
	@NonNull private final Shortcut shortcut;

}