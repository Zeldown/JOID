package dev.joid.lib.ui.node.impl.control.toggle;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ToggleState<T, B> {

	private final T toggle;
	private final B back;

	public static <T, B> @NonNull ToggleState<T, B> create(final T toggle, final B back) {
		return new ToggleState<>(toggle, back);
	}

}